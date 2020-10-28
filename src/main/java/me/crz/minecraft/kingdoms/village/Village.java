package me.crz.minecraft.kingdoms.village;

import co.aikar.timings.lib.MCTiming;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.ObjectArrays;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.land.TaxDebtAction;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.perk.LevelPerk;
import me.crz.minecraft.kingdoms.rank.RankManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static me.crz.minecraft.kingdoms.utils.MessageUtils.yesOrNo;

public class Village extends LandOwner<VillageRank> implements Comparable<Village>, Targetable {
    private int level;
    private UUID chief;
    private Empire empire;
    private VillageState villageState;
    private Map<String, NamedLocation> outpostHomes = new LinkedHashMap<>();
    private Set<UUID> residents = new LinkedHashSet<>();
    private boolean allowAllyClaim = false;

    protected Village() {
        super();
    }

    public Village(String name, UUID chief, int level, VillageState state) {
        super(name);
        residents.add(chief);
        this.chief = chief;
        this.level = level;
        this.villageState = state;
    }

    public void doUpkeep() {
        if (isAbandoned()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " is abandoned. No upkeep.");
            return;
        }
        int inactiveDays = Config.Settlement.Upkeep.INACTIVE_TIME;
        if (inactiveDays > 0 && getLevel() == 1 && getState() == VillageState.VILLAGE && !hasEmpire() && getPerksMap().isEmpty()
                && LandManager.getInstance().getClaimedLandSize(this) == 0
                && getChief().getLastLogin() + TimeUnit.DAYS.toMillis(inactiveDays) < System.currentTimeMillis()) {
            OfflinePlayer chiefPlayer = KingdomsPlugin.getInstance().getServer().getOfflinePlayer(getChiefId());
            if (!chiefPlayer.hasPlayedBefore() || chiefPlayer.getLastPlayed() + TimeUnit.DAYS.toMillis(inactiveDays) < System.currentTimeMillis()) {
                KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " is inactive " +
                        "(chief offline for more than " + inactiveDays + ", Village level 1, no claims, no perks unlocked), removing it!");
                VillageManager.getInstance().deleteVillage(this);
                return;
            }
        }
        Map<Resident, Long> taxes = new LinkedHashMap<>();
        if (getTax() > 0) {
            long tax = getTax();
            for (Resident resident : getResidents()) { // residents should already be cached!
                if (isChief(resident)) {
                    continue;
                }
                if (resident.hasRankPermission("village.tax.exempt")) {
                    continue;
                }
                taxes.put(resident, tax);
            }
        }

        for (Map.Entry<Resident, Long> entry : getLandTaxes().entrySet()) {
            taxes.merge(entry.getKey(), entry.getValue(), (l1, l2) -> l1 + l2);
        }

        long totalTax = 0;
        for (Map.Entry<Resident, Long> entry : taxes.entrySet()) {
            Resident resident = entry.getKey();
            long tax = entry.getValue();
            OfflinePlayer player = resident.getPlayer();
            if (player == null) {
                player = Bukkit.getOfflinePlayer(resident.getUUID());
            }
            if (KingdomsPlugin.getInstance().econ.withdrawPlayer(player, tax).transactionSuccess()) {
                addToBank(tax);
                totalTax += tax;
                if (player instanceof Player) {
                    Message.get("tax.removed").tell((Player) player,
                            "amount", String.valueOf(tax),
                            "state", getState().getTag(),
                            "name", getName());
                }
            } else {
                Message.get("tax.debt-action." + getTaxDebtAction().getKey() + ".couldnt-pay").tell(getOnlineResidents(),
                        "player", resident.getName(),
                        "amount", String.valueOf(tax),
                        "state", getState().getTag(),
                        "name", getName()
                );
                if (getTaxDebtAction() == TaxDebtAction.KICK) {
                    if (isMember(resident)) {
                        addBanished(resident);
                        removeMember(resident);
                    }
                    for (OwnedLand land : getLand()) {
                        if (land.isOwnedBy(resident)) {
                            LandManager.getInstance().setPlotOwner(land, null);
                        }
                    }
                }
            }
        }

        if (totalTax > 0) {
            long finalTotalTax = totalTax;
            Utils.runAsync(() -> DatabaseManager.getInstance().log(this, null, "tax", "cash", finalTotalTax));
        }
        payUpkeep(totalTax);
    }

    private Map<Resident, Long> getLandTaxes() {
        Map<Resident, Long> taxes = new LinkedHashMap<>();
        for (OwnedLand land : getLand()) {
            if (land.isPlot() && land.getTax() > 0) {
                Resident resident = ResidentManager.getInstance().getResident(land.getPlotOwner());
                if (isChief(resident)) {
                    continue;
                }
                if (isMember(resident) && resident.hasRankPermission(getKey() + ".tax.exempt")) {
                    continue;
                }
                taxes.put(resident, taxes.computeIfAbsent(resident, r -> 0L) + land.getTax());
            }
        }
        return taxes;
    }

    private void payUpkeep(long lastTax) {
        if (hasEmpire() && getEmpire().isCapital(this)) {
            // no upkeep for empire capitals
            if (lastTax > 0) {
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(this));
            }
            return;
        }
        int total = getUpkeep();
        if (removeFromBank(total)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") was charged an upkeep of " + total + "!");
            MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.charged"), "charged", String.valueOf(total));
            if (!has(total - lastTax)) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.cant-pay"), "amount", String.valueOf(total));
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(this);
                DatabaseManager.getInstance().log(this, null, "upkeep", "cash", total);
            });
        } else {
            if (getLevel() > 1 || getState() == VillageState.KINGDOM) {
                if (getLevel() == 1 && getState() == VillageState.KINGDOM) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and has been downgraded!");
                    setState(VillageState.VILLAGE);
                    setLevel(Levels.VILLAGE_TO_KINGDOM_LEVEL);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.downgraded"), "state", getState().getTag(), "level", String.valueOf(getLevel()));
                } else {
                    KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and has had a level removed!");
                    setLevel(getLevel() - 1);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.level-removed"), "level", String.valueOf(getLevel()));
                }
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(this));
            } else {
                setBank(0);
                if (Config.Settlement.Upkeep.ABANDON_TIME > 0) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and was abandoned!");
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.abandoned"));
                    VillageManager.getInstance().abandonVillage(this);
                } else {
                    KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and was deleted!");
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.deleted"));
                    VillageManager.getInstance().deleteVillage(this);
                }
            }
        }
    }

    @Override
    public int getUpkeep() {
        if (hasEmpire() && getEmpire().isCapital(this)) {
            return 0;
        }
        int amount = 0;
        for (OwnedLand land : LandManager.getInstance().getClaimedLand(this)) {
            amount += Utils.getUpkeepCost(getState(), land.getWorldName(), getLevel());
        }
        return amount;
    }

    public boolean isAbandoned() {
        return residents.isEmpty();
    }

    public void setOutpostHome(String name, Location loc) {
        setOutpostHome(new NamedLocation(name, loc));
    }

    public void setOutpostHome(NamedLocation location) {
        outpostHomes.put(location.getName().toLowerCase(), location);
    }

    public Map<String, NamedLocation> getOutpostHomes() {
        return outpostHomes;
    }

    public boolean isAllowAllyClaim() {
        return allowAllyClaim;
    }

    public void setAllowAllyClaim(boolean allowAllyClaim) {
        this.allowAllyClaim = allowAllyClaim;
    }

    @Override
    public boolean isTrusted(LandOwner landOwner) {
        return (landOwner instanceof Empire && landOwner.equals(getEmpire()))
                || (landOwner instanceof Village && hasEmpire() && getEmpire().isMember((Village) landOwner));
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        if (!ConfigManager.getInstance().getDataConfig().getConfig().contains(villageState.getKey() + ".level" + level)) {
            ConfigManager.getInstance().getDataConfig().getConfig().set(villageState.getKey() + ".level" + level, getName());
            ConfigManager.getInstance().getDataConfig().saveConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, Message.get("first-to-reach-level"),
                        "name", this.getName(),
                        "state", villageState.name().toLowerCase(),
                        "level", String.valueOf(level));
            }
        }
    }

    @Override
    public boolean isChief(UUID playerId) {
        return chief.equals(playerId);
    }

    public UUID getChiefId() {
        return chief;
    }

    public void setChief(UUID chief) {
        this.chief = chief;
    }

    /**
     * Get the chief, might run database operation!
     * @return The chief's Resident object
     */
    public Resident getChief() {
        return ResidentManager.getInstance().getResident(getChiefId());
    }

    /**
     * Might run database operation!
     */
    @Override
    public List<Resident> getResidents() {
        return residents.stream()
                .map(id -> ResidentManager.getInstance().getResident(id)).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return residents.stream()
                .map(Bukkit::getPlayer).filter(Objects::nonNull)
                .map(p -> ResidentManager.getInstance().getResident(p))
                .collect(Collectors.toList());
    }

    @Override
    public long getOnlineCount() {
        return residents.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).count();
    }

    @Override
    public boolean hasPermission(Resident resident, String perm) {
        return isMember(resident) && resident.hasPermission("village." + perm);
    }

    @Override
    public boolean isOfficer(Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        return isMember(res) && res.hasPermission("kingdoms.command.village.officer");
    }

    @Override
    public boolean isMember(Resident resident) {
        return equals(resident.getVillage());
    }

    @Override
    public String getStateTag() {
        return getState().getTag();
    }

    @Override
    public String getStateKey() {
        return getState().getKey();
    }

    @Override
    public Resident getResident(String name) {
        for (Resident resident : getResidents()) {
            if (resident.getName().equalsIgnoreCase(name)) {
                return resident;
            }
        }
        return null;
    }

    public Set<UUID> getResidentIds() {
        return residents;
    }

    public boolean isFull() {
        return Config.Settlement.MAX_SIZE > 0 && getResidentIds().size() >= Config.Settlement.MAX_SIZE;
    }

    public VillageState getState() {
        return villageState;
    }

    public void setState(VillageState state) {
        this.villageState = state;
    }

    public int getMaxClaimableChunks() {
        return Utils.getLevelChunks(getState(), getLevel()) + getBonusClaimAmount();
    }

    public int getBonusClaimAmount() {
        return getBonusClaims() + getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims()).sum()
                + (hasEmpire() ? getEmpire().getBonusClaimAmount() : 0);
    }

    public int getMaxOutposts() {
        return Utils.getLevelOutposts(getState(), getLevel()) + getBonusOutposts() + (hasEmpire() ? getEmpire().getBonusOutposts() : 0);
    }

    public int getBonusOutposts() {
        return getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusOutposts()).sum();
    }

    public Empire getEmpire() {
        return empire;
    }

    public void setEmpire(Empire empire) {
        if (getEmpire() != null) {
            getEmpire().getMemberIds().remove(getId());
        }
        this.empire = empire;
        if (empire != null) {
            empire.getMemberIds().add(getId());
        }
    }

    public boolean hasEmpire() {
        return empire != null;
    }

    public boolean canLevelUp() {
        if (isMaxLevel()) {
            return false;
        }
        return has(getNextLevelRequirements());
    }

    public Map<String, Long> getNextLevelRequirements() {
        int nextLevel = getLevel() + 1;
        VillageState nextState = getState();
        if (nextState == VillageState.VILLAGE && nextLevel > Levels.VILLAGE_TO_KINGDOM_LEVEL) {
            nextState = VillageState.KINGDOM;
            nextLevel = 1;
        }
        return Levels.getLevelRequirements(nextState, nextLevel);
    }

    public boolean isMaxLevel() {
        return (getLevel() == Levels.KINGDOMS_MAX_LEVEL && getState() == VillageState.KINGDOM) || getNextLevelRequirements() == null;
    }

    @Override
    public boolean setMode(PlotMode mode, boolean value) {
        if (super.setMode(mode, value)) {
            if (KingdomsPlugin.getInstance().isRunning() && mode == PlotMode.mobSpawning && !value) {
                try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Village mob spawning mode toggle")) {
                    for (OwnedLand land : LandManager.getInstance().getClaimedLand(this)) {
                        World world = Bukkit.getWorld(land.getWorldName());
                        if (world != null && world.isChunkLoaded(land.getX(), land.getZ())) {
                            for (Entity entity : land.getChunk().getEntities()) {
                                if (entity instanceof LivingEntity && Utils.isHostile((LivingEntity) entity)) {
                                    entity.remove();
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public LevelPerk getPerk(String name) {
        return VillageManager.getInstance().getPerk(name);
    }

    @Override
    protected Set<String> getPermissions() {
        return RankManager.getInstance().getVillageChiefRank().getInheritedPermissions();
    }

    @Override
    public BuildState getDefaultBuildState() {
        return BuildState.RESIDENTS;
    }

    @Override
    public BuildState.AppliesTo getBuildStateType() {
        return BuildState.AppliesTo.SETTLEMENT;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("chief", getChiefId());
        map.put("level", getLevel());
        map.put("empireId", hasEmpire() ? getEmpire().getId().value() : null);
        map.put("allowAllyClaim", isAllowAllyClaim());
        map.put("settlementState", getState().name());
        map.put("residents", getResidentIds().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("outposts", getOutpostHomes().values().stream().collect(Collectors.toMap(NamedLocation::getName, NamedLocation::serialize)));
        return map;
    }

    @Override
    public void removeMember(Targetable target) {
        if (target instanceof Resident && isMember((Resident) target)) {
            residents.remove(((Resident) target).getUUID());
            VillageManager.getInstance().leaveVillage((Resident) target);
        }
    }

    @Override
    public Collection<OwnedLand> getLand() {
        return LandManager.getInstance().getClaimedLand(this);
    }

    @Override
    public long getTaxIncome() {
        long income = 0;
        for (Resident resident : getResidents()) {
            if (!resident.hasRankPermission("village.tax.exempt")) {
                income += getTax();
            }
        }
        for (OwnedLand land : getLand()) {
            if (land.isPlot()) {
                Resident owner = ResidentManager.getInstance().getResident(land.getPlotOwner());
                if (owner != null && (!isMember(owner) || !owner.hasRankPermission("village.tax.exempt"))) {
                    income += land.getTax();
                }
            }
        }
        return income;
    }

    @Override
    public long getTax() {
        if (Config.Settlement.MAX_TAX > -1 && super.getTax() > Config.Settlement.MAX_TAX) {
            return Config.Settlement.MAX_TAX;
        }
        return super.getTax();
    }

    public boolean leaveEmpire() {
        if (hasEmpire()) {
            getEmpire().removeMember(this);
            return true;
        }
        return false;
    }

    public boolean isCapital() {
        return hasEmpire() && getEmpire().isCapital(this);
    }

    public static Village deserialize(Map<String, Object> map) {
        KingdomsPlugin.logDebug("Creating village from " + map);
        Village village = LandOwner.deserialize(new Village(), map);
        if (village == null) {
            return null;
        }
        if (map == null || map.get("chief") == null || map.get("level") == null || map.get("settlementState") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Village! (" + map + ")");
            return null;
        }
        village.setChief(UUID.fromString((String) map.get("chief")));
        village.setState(VillageState.valueOf((String) map.get("settlementState")));
        village.setLevel((Integer) map.get("level"));
        village.setAllowAllyClaim((Boolean) map.get("allowAllyClaim"));
        village.setEmpire(EmpireManager.getInstance().getEmpireFromId(IdObject.get(map.get("empireId"))));
        ((Collection<String>) map.get("residents")).stream().map(UUID::fromString).forEach(u -> village.getResidentIds().add(u));
        ((Map<String, Map<String, Object>>) map.get("outposts")).values().stream()
                .map(NamedLocation::deserialize)
                .filter(Objects::nonNull)
                .forEach(village::setOutpostHome);
        KingdomsPlugin.logDebug("Created village " + village.serialize());
        return village;
    }

    @Override
    public int compareTo(Village v) {
        if (v.getState() != getState())
            return getState().compareTo(v.getState());
        if (v.getLevel() != getLevel())
            return Integer.compare(v.getLevel(), getLevel());
        int otherClaimed = LandManager.getInstance().getClaimedLandSize(v);
        int claimed = LandManager.getInstance().getClaimedLandSize(this);
        if (otherClaimed != claimed)
            return Integer.compare(otherClaimed, claimed);
        if (v.getResidentIds().size() != getResidentIds().size())
            return Long.compare(v.getResidentIds().size(), getResidentIds().size());
        if (v.getBank() != getBank())
            return Long.compare(v.getBank(), getBank());
        return 0;
    }

    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        Message rankMessage = null;
        Message entry = null;
        String online = null;
        String offline = null;
        String separator = null;
        Message more = null;
        if (configMessages != null) {
            rankMessage = configMessages.getMessage("residents.rank");
            entry = configMessages.getMessage("residents.entry");
            online = configMessages.getMessage("residents.online").getText();
            offline = configMessages.getMessage("residents.offline").getText();
            separator = configMessages.getMessage("residents.separator").getText();
            more = configMessages.getMessage("residents.more");
        }
        Resident chief = getChief();
        String[] repl = new String[] {
                "name", getName(),
                "description", getDescription(),
                "empire", hasEmpire() ? getEmpire().getName() : "",
                "state", getState().getTag(),
                "abandoned", yesOrNo(isAbandoned()),
                "abandonedcountdown", isAbandoned() ? UtilTime.format(getCreated() + TimeUnit.MINUTES.toMillis(Config.Settlement.Upkeep.ABANDON_TIME) - System.currentTimeMillis()) : "",
                "level", String.valueOf(getLevel()),
                "land", String.valueOf(LandManager.getInstance().getClaimedLandSize(this)),
                "maxland", String.valueOf(getMaxClaimableChunks()),
                "outposts", String.valueOf(getOutpostHomes().size()),
                "maxoutposts", String.valueOf(getMaxOutposts()),
                "balance", Utils.withSuffix(getBank()),
                "bank", Utils.CURRENCY_FORMAT.format(getBank()),
                "tax", Utils.CURRENCY_FORMAT.format(getTax()),
                "taxdebtaction", getTaxDebtAction().getTag(),
                "joinfee", Utils.CURRENCY_FORMAT.format(getJoinFee()),
                "upkeep", Utils.CURRENCY_FORMAT.format(getUpkeep()),
                "allowallyclaim", yesOrNo(isAllowAllyClaim()),
                "hostilespawning", String.valueOf(getMode(PlotMode.mobSpawning)),
                "passivespawning", String.valueOf(getMode(PlotMode.passiveSpawning)),
                "firespread", String.valueOf(getMode(PlotMode.fireSpread)),
                "explosions", String.valueOf(getMode(PlotMode.explosions)),
                "pvp", String.valueOf(getMode(PlotMode.pvp)),
                "buff", String.valueOf(getMode(PlotMode.buff)),
                "bonusclaims", configMessages != null
                        ? getBonusClaimAmount() > 0 ? configMessages.getMessage("bonus-claims").getReplacedText("amount", String.valueOf(getBonusClaimAmount())) : ""
                        : String.valueOf(getBonusClaimAmount()),
                "bonusoutposts", configMessages != null
                        ? getBonusOutposts() > 0 ? configMessages.getMessage("bonus-outposts").getReplacedText("amount", String.valueOf(getBonusOutposts())) : ""
                        : String.valueOf(getBonusOutposts()),
                "access", getAccess().getTag(),
                "perks", getPerksMap().entrySet().stream()
                        .map(e -> (e.getValue().getTimes() > 1 ? e.getValue().getTimes() + "x " : "") + getPerk(e.getKey()).getTag())
                        .collect(Collectors.joining(", ")),
                "chiefrank", VillageRank.getChief().getTag(this),
                "chieftitle", chief.getVillageTitle(),
                "chiefdisplaytitle", chief.getDisplayTitle(),
                "chief", chief.getName(),
                "chiefonline", configMessages != null ? chief.getPlayer() != null ? online : offline : "",
                "residents", configMessages != null ? getResidentsToString(rankMessage, entry, more, online, offline, separator) : String.valueOf(getResidentIds().size()),
                "size", String.valueOf(getResidentIds().size()),
                "resonlinecount", String.valueOf(getOnlineCount()),
                "resotheronlinecount", String.valueOf(getOnlineCount() - 1),
                "officerranks", Arrays.stream(VillageRank.values())
                        .filter(r -> !r.isChief())
                        .filter(r -> getEffectivePermissionValue(r, "village.officer").orElse(false))
                        .map(r -> r.getTag(this))
                        .collect(Collectors.joining(", "))
        };
        return initial.length > 1 ? ObjectArrays.concat(repl, initial, String.class) : repl;
    }

    private String getResidentsToString(Message rankMessage, Message entry, Message more, String online, String offline, CharSequence separator) {
        Multimap<VillageRank, Resident> rankMap = MultimapBuilder.treeKeys().arrayListValues().build();
        for (Resident resident : getResidents()) {
            rankMap.put(resident.getVillageRank(), resident);
        }
        List<String> lines = new ArrayList<>();
        for (VillageRank rank : rankMap.keySet()) {
            if (rank == VillageRank.getChief()) {
                // should be listed extra
                continue;
            }
            String line = rankMessage.getReplacedText(
                    "rank", rank.getTag(this),
                    "residents", rankMap.get(rank).stream()
                            .sorted((r1, r2) -> {
                                Player p1 = r1.getPlayer();
                                Player p2 = r2.getPlayer();
                                if (p1 != null && p2 == null) {
                                    return -1;
                                } else if (p1 == null && p2 != null) {
                                    return 1;
                                }
                                return Long.compare(r1.getVillageJoin(), r2.getVillageJoin());
                            })
                            .limit(Config.PLAYERS_IN_INFO)
                            .map(res -> entry.getReplacedText(
                                    "online", res.getPlayer() != null ? online : offline,
                                    "name", res.getName(),
                                    "rank", rank.getTag(this),
                                    "title", res.getVillageTitle()
                            ))
                            .collect(Collectors.joining(separator))
            );
            if (rankMap.get(rank).size() > Config.PLAYERS_IN_INFO) {
                line += more.getReplacedText(
                        "amount", String.valueOf(rankMap.get(rank).size()),
                        "more", String.valueOf(rankMap.get(rank).size() - Config.PLAYERS_IN_INFO)
                );
            }
            lines.add(line);
        }
        return String.join("\n", lines);
    }

    @Override
    public String getDbName() {
        return "settlement";
    }

    @Override
    public String name() {
        return "Village";
    }
}

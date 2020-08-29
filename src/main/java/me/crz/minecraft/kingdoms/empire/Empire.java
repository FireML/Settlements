package me.crz.minecraft.kingdoms.empire;

import co.aikar.timings.Timing;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.ObjectArrays;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.land.TaxDebtAction;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.RankManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static me.crz.minecraft.kingdoms.utils.MessageUtils.yesOrNo;

public class Empire extends LandOwner<EmpireRank> implements Comparable<Empire> {
    private IdObject capital;
    private String symbol = "";
    private Set<IdObject> members = new LinkedHashSet<>();
    private boolean inRegen = false;
    private long lastRename = 0;

    protected Empire() {
        super();
    }

    public Empire(String name, IdObject capital) {
        super(name);
        this.capital = capital;
        members.add(capital);
    }

    public void doUpkeep() {
        if (isAbandoned()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " is abandoned. No upkeep.");
            return;
        }

        long paidTaxes = 0;
        if (getTax() > 0) {
            long tax = getTax();
            for (Village member : getMembers()) {
                if (member.getId().equals(getCapitalId())) {
                    continue;
                }
                if (member.removeFromBank(tax)) {
                    addToBank(tax);
                    paidTaxes += tax;
                    Message.get("empire-tax.removed").tell(member.getOnlineResidents(),
                            "amount", String.valueOf(tax),
                            "state", member.getState().getTag(),
                            "settlement", member.getName(),
                            "name", getName());
                } else {
                    Message.get("empire-tax.debt-action." + getTaxDebtAction().getKey() + ".couldnt-pay").tell(getOnlineResidents(),
                            "amount", String.valueOf(tax),
                            "state", member.getState().getTag(),
                            "settlement", member.getName(),
                            "name", getName()
                    );
                    if (getTaxDebtAction() == TaxDebtAction.KICK) {
                        removeMember(member);
                    }
                }
            }
        }
        paidTaxes += doLandTaxes();
        payUpkeep(paidTaxes);

        if (paidTaxes > 0) {
            long finalPaidTaxes = paidTaxes;
            Utils.runAsync(() -> {
                if (getTax() > 0) {
                    DatabaseManager.getInstance().saveVillages(getMembers());
                }
                DatabaseManager.getInstance().log(this, null, null, "tax", "cash", finalPaidTaxes);
            });
        }
    }

    private long doLandTaxes() {
        long totalTax = 0;
        Set<OwnedLand> toUnclaim = new HashSet<>();
        for (OwnedLand land : getLand()) {
            if (land.isPlot() && land.getTax() > 0) {
                Resident resident = ResidentManager.getInstance().getResident(land.getPlotOwner());
                if (isChief(resident)) {
                    continue;
                }
                if (isMember(resident) && resident.hasRankPermission("empire.tax.exempt")) {
                    continue;
                }
                OfflinePlayer player = resident.getPlayer();
                if (player == null) {
                    player = Bukkit.getOfflinePlayer(resident.getUUID());
                }
                if (KingdomsPlugin.getInstance().econ.withdrawPlayer(player, land.getTax()).transactionSuccess()) {
                    addToBank(land.getTax());
                    totalTax += land.getTax();
                    if (player instanceof Player) {
                        Message.get("plot-tax.removed").tell((Player) player,
                                "amount", String.valueOf(land.getTax()),
                                "state", getStateTag(),
                                "name", getName(),
                                "x", String.valueOf(land.getX()),
                                "z", String.valueOf(land.getZ()),
                                "world", land.getWorldName()
                        );
                    }
                } else {
                    Message.get("plot-tax.debt-action." + getTaxDebtAction().getKey() + ".couldnt-pay").tell(getOnlineResidents(),
                            "player", resident.getName(),
                            "amount", String.valueOf(land.getTax()),
                            "state", getStateTag(),
                            "name", getName(),
                            "x", String.valueOf(land.getX()),
                            "z", String.valueOf(land.getZ()),
                            "world", land.getWorldName()
                    );
                    if (getTaxDebtAction() == TaxDebtAction.KICK && !land.containsHome() && !land.isOutpost()) {
                        toUnclaim.add(land);
                    }
                }
            }
        }
        LandManager.getInstance().unclaimLand(toUnclaim);
        return totalTax;
    }

    private void payUpkeep(long lastTax) {
        int total = getUpkeep();
        if (removeFromBank(total)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " was charged an upkeep of " + total + "!");
            MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.empire-alert.charged"), "charged", String.valueOf(total));
            if (!has(total - lastTax)) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.cant-pay"), "amount", String.valueOf(total));
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveEmpire(this);
                DatabaseManager.getInstance().log(this, null, "upkeep", "cash", total);
            });
        } else {
            KingdomsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " wasn't able to pay the upkeep of " + total + " and was abandoned!");
            setBank(0);
            if (Utils.getAbandonTime(getStateKey()) > 0) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.empire-alert.abandoned"));
                EmpireManager.getInstance().abandonEmpire(this);
            } else {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.empire-alert.deleted"));
                EmpireManager.getInstance().deleteEmpire(this);
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("capitalId", getCapitalId().value());
        map.put("lastRename", getLastRename());
        map.put("symbol", getSymbol());
        map.put("members", getMemberIds().stream().map(IdObject::value).filter(Objects::nonNull).collect(Collectors.toList()));
        return map;
    }

    @Override
    public void removeMember(Targetable target) {
        if (target instanceof Village && isMember((Village) target)) {
            members.remove(((Village) target).getId());
            EmpireManager.getInstance().leaveEmpire((Village) target);
        }
    }

    @Override
    public boolean removeAlly(LandOwner<?> landOwner) {
        if (super.removeAlly(landOwner)) {
            for (Resident resident : landOwner.getResidents()) {
                Player player = resident.getPlayer();
                if (player != null) {
                    Empire e = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
                    if (e != null && !e.canAccess(resident)) {
                        Utils.teleportToSpawn(player);
                    }
                }
            }
            for (Resident resident : getResidents()) {
                Player player = resident.getPlayer();
                if (player != null) {
                    Empire e = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
                    if (e != null && !e.canAccess(resident)) {
                        Utils.teleportToSpawn(player);
                    }
                }
            }
            return true;
        }
        return false;
    }


    @Override
    public Collection<OwnedLand> getLand() {
        return LandManager.getInstance().getClaimedLand(this);
    }

    @Override
    public long getTaxIncome() {
        long income = 0;
        for (Village member : getMembers()) {
            if (!member.getId().equals(getCapitalId()) && member.has(getTax())) {
                income += getTax();
            }
        }
        for (OwnedLand land : getLand()) {
            if (land.isPlot()) {
                Resident owner = ResidentManager.getInstance().getResident(land.getPlotOwner());
                if (owner != null && (!isMember(owner) || !owner.hasPermission("empire.tax.exempt")) && !isCapital(owner.getVillage())) {
                    income += land.getTax();
                }
            }
        }
        return income;
    }

    @Override
    public long getTax() {
        int maxTax = Utils.getEmpireMaxTax();
        if (maxTax > -1 && super.getTax() > maxTax) {
            return maxTax;
        }
        return super.getTax();
    }

    public static Empire deserialize(Map<String, Object> map) {
        KingdomsPlugin.logDebug("Creating empire from " + map);
        Empire empire = LandOwner.deserialize(new Empire(), map);
        if (empire == null) {
            return null;
        }
        if (map == null || map.get("capitalId") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Empire! (" + map + ")");
            return null;
        }
        empire.capital = IdObject.get(map.get("capitalId"));
        empire.lastRename = (Long) map.get("lastRename");
        empire.setSymbol((String) map.get("symbol"));
        if (map.get("members") != null && map.get("members") instanceof Collection) {
            for (Object memberId : (Collection) map.get("members")) {
                empire.members.add(IdObject.get(memberId));
            }
        }
        KingdomsPlugin.logDebug("Created empire " + empire.serialize());
        return empire;
    }

    @Override
    public int compareTo(Empire e) {
        if (isAbandoned()) {
            return -1;
        }
        if (e.getMemberIds().size() != getMemberIds().size())
            return Integer.compare(e.getMemberIds().size(), getMemberIds().size());
        return Long.compare(e.getBank(), getBank());
    }

    public int getBonusClaimAmount() {
        return getBonusClaims() + getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims()).sum();
    }

    public int getBonusOutposts() {
        return getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusOutposts()).sum();
    }

    public int getBonusRadius() {
        return getPerksMap().entrySet().stream().mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusRadius()).sum();
    }

    public int getWorldRadius() {
        return Utils.getEmpireWorldRadius() + getBonusRadius();
    }

    public Village getCapital() {
        return VillageManager.getInstance().getVillageFromId(capital);
    }

    public IdObject getCapitalId() {
        return capital;
    }

    public void setCapital(Village capital) {
        this.capital = capital != null ? capital.getId() : IdObject.get(null);
    }

    @Override
    public EmpirePerk getPerk(String name) {
        return EmpireManager.getInstance().getPerk(name);
    }

    @Override
    public void apply(Perk perk) {
        super.apply(perk);
        if (perk instanceof EmpirePerk && ((EmpirePerk) perk).getBonusRadius() > 0) {
            World world = Bukkit.getWorld(getWorldName());
            if (world != null) {
                world.getWorldBorder().setSize(getWorldRadius() * 2, 5);
            }
        }
    }

    @Override
    public Set<String> getPermissions() {
        return RankManager.getInstance().getEmpireLeaderRank().getInheritedPermissions();
    }

    @Override
    public BuildState getDefaultBuildState() {
        return BuildState.PRIVATE;
    }

    @Override
    public BuildState.AppliesTo getBuildStateType() {
        return BuildState.AppliesTo.EMPIRE;
    }

    public Set<IdObject> getMemberIds() {
        return members;
    }

    public Collection<Village> getMembers() {
        return getMemberIds().stream().map(i -> VillageManager.getInstance().getVillageFromId(i)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean isFull() {
        return getMemberIds().size() >= ConfigManager.getInstance().getConfig().getInt("max-empire-size");
    }

    @Override
    public boolean isChief(UUID playerId) {
        return getCapital().isChief(playerId);
    }

    @Override
    public UUID getChiefId() {
        return getCapital().getChiefId();
    }

    @Override
    public boolean isChief(Targetable target) {
        if (super.isChief(target)) {
            return true;
        }
        if (target instanceof Village) {
            return isCapital((Village) target);
        }
        return false;
    }

    @Override
    public boolean isOfficer(Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        return isMember(res) && res.hasPermission("kingdoms.command.empire.officer");
    }

    public boolean isCapital(Village village) {
        return village != null && capital.equals(village.getId());
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public List<Resident> getResidents() {
        return getMembers().stream().flatMap(v -> v.getResidents().stream()).collect(Collectors.toList());
    }

    @Override
    public long getOnlineCount() {
        return getMembers().stream().mapToLong(Village::getOnlineCount).sum();
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return getMembers().stream().flatMap(v -> v.getOnlineResidents().stream()).collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(Resident resident, String perm) {
        return isMember(resident) && resident.hasPermission("empire." + perm);
    }

    @Override
    public boolean isMember(Resident resident) {
        return isMember(resident.getVillage());
    }

    @Override
    public String getStateKey() {
        return State.EMPIRE.getKey();
    }

    @Override
    public String getStateTag() {
        return State.EMPIRE.getTag();
    }

    @Override
    public Resident getResident(String name) {
        for (Village member : getMembers()) {
            for (Resident resident : member.getResidents()) {
                if (resident.getName().equalsIgnoreCase(name)) {
                    return resident;
                }
            }
        }
        return null;
    }

    @Override
    public int getUpkeep() {
        return KingdomsPlugin.getInstance().getConfig().getInt("upkeep.empire");
    }

    public boolean isAbandoned() {
        return members.isEmpty();
    }

    public void setInRegen(boolean inRegen) {
        lastRename = System.currentTimeMillis();
        this.inRegen = inRegen;
    }

    public boolean isInRegen() {
        return inRegen;
    }

    public long getLastRename() {
        return lastRename;
    }

    @Override
    public String name() {
        return "Empire";
    }

    private enum State implements NameTag {
        EMPIRE;
    }

    public String getWorldName() {
        return KingdomsPlugin.getInstance().getEmpiresWorldPrefix() + getName() + KingdomsPlugin.getInstance().getEmpiresWorldSuffix();
    }

    public boolean isMember(Village village) {
        return village != null && equals(village.getEmpire());
    }

    @Override
    public boolean isTrusted(LandOwner landOwner) {
        return landOwner instanceof Village && isMember((Village) landOwner);
    }

    @Override
    public boolean setMode(PlotMode mode, boolean value) {
        if (super.setMode(mode, value)) {
            if (KingdomsPlugin.getInstance().isRunning() && mode == PlotMode.mobSpawning && !value) {
                World world = Bukkit.getWorld(getWorldName());
                if (world != null) {
                    try (Timing ignored = KingdomsPlugin.getInstance().getAndStartTiming("Empire mob spawning mode toggle")) {
                        for (LivingEntity entity : world.getLivingEntities()) {
                            if (Utils.isHostile(entity)) {
                                entity.remove();
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean canAccess(Player player) {
        return canAccess(ResidentManager.getInstance().getResident(player));
    }

    public boolean canAccess(Resident resident) {
        if (getAccess().atLeast(LandAccess.PUBLIC)) {
            return true;
        }
        if (getAccess().atLeast(LandAccess.TRUSTED)) {
            return isTrusted(resident);
        }
        if (getAccess().atLeast(LandAccess.ALLY)) {
            return isTrusted(resident) || isAlly(resident);
        }
        if (getAccess().atLeast(LandAccess.PRIVATE)) {
            return getCapital().isMember(resident);
        }
        return false;
    }

    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        Village capital = getCapital();
        Resident chief = capital != null ? capital.getChief() : null;
        Message rankMessage = null;
        Message entry = null;
        String online = null;
        String offline = null;
        String separator = null;
        if (configMessages != null) {
            rankMessage = configMessages.getMessage("special-residents.rank");
            entry = configMessages.getMessage("special-residents.entry");
            online = configMessages.getMessage("special-residents.online").getText();
            offline = configMessages.getMessage("special-residents.offline").getText();
            separator = configMessages.getMessage("special-residents.separator").getText();
        }
        String[] repl = new String[] {
                "name", getName(),
                "description", getDescription(),
                "symbol", getSymbol(),
                "access", getAccess().getTag(),
                "upkeep", Utils.CURRENCY_FORMAT.format(getUpkeep()),
                "balance", Utils.withSuffix(getBank()),
                "bank", Utils.CURRENCY_FORMAT.format(getBank()),
                "joinfee", Utils.CURRENCY_FORMAT.format(getJoinFee()),
                "tax", Utils.CURRENCY_FORMAT.format(getTax()),
                "taxdebtaction", getTaxDebtAction().getTag(),
                "upkeep", Utils.CURRENCY_FORMAT.format(getUpkeep()),
                "abandoned", yesOrNo(isAbandoned()),
                "abandonedcountdown", isAbandoned() ? UtilTime.format(getCreated() + (Utils.getAbandonTime(getStateKey()) * 60 * 1000) - System.currentTimeMillis()) : "",
                "mobspawning", String.valueOf(getMode(PlotMode.mobSpawning)),
                "firespread", String.valueOf(getMode(PlotMode.fireSpread)),
                "explosions", String.valueOf(getMode(PlotMode.explosions)),
                "pvp", String.valueOf(getMode(PlotMode.pvp)),
                "buff", String.valueOf(getMode(PlotMode.buff)),
                "bonusoutposts", String.valueOf(getBonusOutposts()),
                "bonusclaims", String.valueOf(getBonusClaimAmount()),
                "bonus", String.valueOf(getBonusClaimAmount()),
                "capital", capital != null ? capital.getName() : "-",
                "capitalstate", capital != null ? capital.getState().getTag() : "-",
                "perks", getPerksMap().entrySet().stream()
                        .map(e -> (e.getValue().getTimes() > 1 ? e.getValue().getTimes() + "x " : "") + getPerk(e.getKey()).getTag())
                        .collect(Collectors.joining(", ")),
                "chiefrank", VillageRank.getChief().getTag(capital),
                "chieftitle", chief != null ? chief.getVillageTitle() : "-",
                "chiefvillagetitle", chief != null ? chief.getVillageTitle() : "-",
                "chiefempiretitle", chief != null ? chief.getEmpireTitle() : "-",
                "chiefdisplaytitle", chief != null ? chief.getDisplayTitle() : "-",
                "chief", chief != null ? chief.getName() : "-",
                "chiefonline", configMessages != null ? chief != null && chief.getPlayer() != null ? online : offline : "",
                "specialresidents", configMessages != null ? getResidentsToString(rankMessage, entry, online, offline, separator) : "",
                "size", String.valueOf(getMembers().size()),
                "members", getMembers().stream().map(v -> v.getState().getTag() + " " + v.getName()).collect(Collectors.joining(", ")),
                "resonlinecount", String.valueOf(getOnlineCount()),
                "resotheronlinecount", String.valueOf(getOnlineCount() - 1),
                "officerranks", Arrays.stream(EmpireRank.values())
                        .filter(r -> r != EmpireRank.getLeader())
                        .filter(r -> getEffectivePermissionValue(r, "empire.officer").orElse(false))
                        .map(NameTag::getTag)
                        .collect(Collectors.joining(", "))
        };
        return initial.length > 1 ? ObjectArrays.concat(repl, initial, String.class) : repl;
    }

    private String getResidentsToString(Message rankMessage, Message entry, String online, String offline, CharSequence separator) {
        Multimap<EmpireRank, Resident> rankMap = MultimapBuilder.treeKeys().arrayListValues().build();
        for (Resident resident : getResidents()) {
            rankMap.put(resident.getEmpireRank(), resident);
        }
        List<String> lines = new ArrayList<>();
        for (EmpireRank rank : rankMap.keySet()) {
            if (rank == EmpireRank.getLeader() || rank == EmpireRank.getDefault()) {
                // leader is listed extra, default rank is not displayed
                continue;
            }
            String line = rankMessage.getReplacedText(
                    "rank", rank.getTag(),
                    "residents", rankMap.get(rank).stream()
                            .sorted((r1, r2) -> {
                                Player p1 = r1.getPlayer();
                                Player p2 = r2.getPlayer();
                                if (p1 != null && p2 == null) {
                                    return -1;
                                } else if (p1 == null && p2 != null) {
                                    return 1;
                                }
                                return Long.compare(r1.getLastLogin(), r2.getLastLogin());
                            })
                            .limit(Utils.getPlayersInShow())
                            .map(res -> entry.getReplacedText(
                                    "online", res.getPlayer() != null ? online : offline,
                                    "name", res.getName(),
                                    "rank", rank.getTag(),
                                    "title", res.getVillageTitle(),
                                    "state", res.getVillage().getStateTag(),
                                    "settlement", res.getVillage().getName()
                            ))
                            .collect(Collectors.joining(separator))
            );
            lines.add(line);
        }
        return String.join("\n", lines);
    }
}

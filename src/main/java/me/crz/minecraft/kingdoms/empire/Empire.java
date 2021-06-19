package me.crz.minecraft.kingdoms.empire;

import co.aikar.timings.lib.MCTiming;
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
import me.crz.minecraft.kingdoms.land.BuildState;
import me.crz.minecraft.kingdoms.missions.MissionData;
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
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageLevel;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static me.crz.minecraft.kingdoms.utils.MessageUtils.yesOrNo;

public class Empire extends LandOwner<EmpireRank, EmpireLevel> implements Comparable<Empire> {
    private int level;
    private IdObject capital;
    private String symbol = "";
    private Set<IdObject> members = new LinkedHashSet<>();
    private boolean inRegen = false;
    private long lastRename = 0;
    private String worldName;
    private long finishedMissions = 0;

    protected Empire() {
        super();
    }

    public Empire(String name, IdObject capital, int level) {
        super(name);
        this.capital = capital;
        members.add(capital);
        this.level = level;
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
            Utils.runAsync(() -> DatabaseManager.getInstance().log(this, null, null, "tax", "cash", finalPaidTaxes));
        }

        if (getTax() > 0) {
            DatabaseManager.getInstance().saveVillages(getMembers());
        }

        if (distributeNewMissions()) {
            DatabaseManager.getInstance().saveMissionData(getMembers());
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
                if (KingdomsPlugin.getInstance().getEconomy().withdrawPlayer(player, land.getTax()).transactionSuccess()) {
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
        long total = getUpkeep();
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
            if (Config.Empire.Upkeep.ABANDON_TIME > 0) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.empire-alert.abandoned"));
                EmpireManager.getInstance().abandonEmpire(this);
            } else {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.empire-alert.deleted"));
                EmpireManager.getInstance().deleteEmpire(this);
            }
        }
    }

    private boolean distributeNewMissions() {
        boolean modified = false;
        for (Village member : getMembers()) {
            modified |= distributeNewMissions(member);
        }
        return modified;
    }

    public boolean distributeNewMissions(Village member) {
        EmpireLevel levelInfo = getLevelInfo();
        if (levelInfo != null && levelInfo.getMissions() > 0) {
            List<EmpireMission> missions = new ArrayList<>(EmpireManager.getInstance().getMissions());
            member.getMissionData().removeIf(md -> {
                if (md.isFinished()) {
                    return Config.Empire.Missions.REMOVE_FINISHED_MISSIONS;
                }
                if (md.hasProgress()) {
                    return Config.Empire.Missions.REMOVE_STARTED_MISSIONS;
                }
                return Config.Empire.Missions.REMOVE_NOT_STARTED_MISSIONS;
            });
            Collections.shuffle(missions);
            int toDistribute = levelInfo.getMissions() + member.getExtraEmpireMissions();
            for (int i = 0; member.getMissionData().size() < toDistribute && i < missions.size(); i++) {
                member.addMissionData(new MissionData(missions.get(i)));
            }
            return !member.getMissionData().isEmpty();
        }
        return false;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("capitalId", getCapitalId().value());
        map.put("lastRename", getLastRename());
        map.put("symbol", getSymbol());
        map.put("level", getLevel());
        map.put("finishedMissions", getFinishedMissions());
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
    public boolean removeAlly(LandOwner<?, ?> landOwner) {
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
        if (Config.Empire.MAX_TAX > -1 && super.getTax() > Config.Empire.MAX_TAX) {
            return Config.Empire.MAX_TAX;
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
        if (Config.Empire.World.RENAME) {
            empire.worldName = Config.Empire.World.PREFIX + empire.getName() + Config.Empire.World.SUFFIX;
        } else if (!empire.getId().isNull()) {
            empire.worldName = Config.Empire.World.PREFIX + "id" + empire.getId().value() + Config.Empire.World.SUFFIX;
        }
        empire.capital = IdObject.get(map.get("capitalId"));
        empire.lastRename = Utils.getLong(map.get("lastRename"));
        empire.setSymbol((String) map.get("symbol"));
        empire.setLevel((Integer) map.get("level"));
        empire.setFinishedMissions(Utils.getLong(map.get("finishedMissions")));
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
        return EmpireManager.getInstance().getLevel(level).map(EmpireLevel::getWorldRadius).orElse(500) + getBonusRadius();
    }

    public Long getClaimUpkeepReduction() {
        return EmpireManager.getInstance().getLevel(level).map(EmpireLevel::getUpkeepReduction).orElse(0L);
    }

    @Override
    public boolean isMaxLevel() {
        return (getLevel() >= Config.Empire.Leveling.MAX_LEVEL) || getNextLevelRequirements().isEmpty();
    }

    @Override
    public boolean checkLevelUp(Resident resident) {
        return EmpireManager.getInstance().checkLevelUp(this, resident);
    }

    @Override
    public long getAmount(String key) {
        if (key.equalsIgnoreCase("missions")) {
            return finishedMissions;
        }
        return super.getAmount(key);
    }

    @Override
    public void setAmount(String key, long amount) {
        if ("missions".equalsIgnoreCase(key)) {
            setFinishedMissions(amount);
        } else {
            super.setAmount(key, amount);
        }
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
    public void apply(Player player, Perk perk) {
        super.apply(player, perk);
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
        return Config.Empire.MAX_SIZE > 0 && getMemberIds().size() >= Config.Empire.MAX_SIZE;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
        if (!ConfigManager.getInstance().getDataConfig().getConfig().contains("empire.level" + level)) {
            ConfigManager.getInstance().getDataConfig().getConfig().set("empire.level" + level, getName());
            ConfigManager.getInstance().getDataConfig().saveConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, Message.get("first-to-reach-level"),
                        "name", this.getName(),
                        "state", "empire",
                        "level", String.valueOf(level));
            }
        }
    }

    @Override
    @Nullable
    public EmpireLevel getLevelInfo() {
        return EmpireManager.getInstance().getLevel(getLevel()).orElse(null);
    }

    @Override
    public Optional<EmpireLevel> getNextLevel() {
        return EmpireManager.getInstance().getLevel(getLevel() + 1);
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

    public long getFinishedMissions() {
        return finishedMissions;
    }

    public void setFinishedMissions(long finishedMissions) {
        this.finishedMissions = finishedMissions;
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
    public long getUpkeep() {
        return EmpireManager.getInstance().getLevel(level).map(EmpireLevel::getUpkeep).orElse(0L);
    }

    @Override
    public boolean isAbandoned() {
        return members.isEmpty();
    }

    @Override
    public long getAbandonTimeConfig() {
        return Config.Empire.Upkeep.ABANDON_TIME;
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
        return worldName;
    }

    void setWorldName(String worldName) {
        this.worldName = worldName;
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
                    try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Empire mob spawning mode toggle")) {
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
                "level", String.valueOf(getLevel()),
                "access", getAccess().getTag(),
                "upkeep", Utils.CURRENCY_FORMAT.format(getUpkeep()),
                "balance", Utils.withSuffix(getBank()),
                "bank", Utils.CURRENCY_FORMAT.format(getBank()),
                "joinfee", Utils.CURRENCY_FORMAT.format(getJoinFee()),
                "tax", Utils.CURRENCY_FORMAT.format(getTax()),
                "taxdebtaction", getTaxDebtAction().getTag(),
                "upkeep", Utils.CURRENCY_FORMAT.format(getUpkeep()),
                "dailymissions", String.valueOf(getLevelInfo() != null ? getLevelInfo().getMissions() : 0),
                "missions", String.valueOf(finishedMissions),
                "abandoned", yesOrNo(isAbandoned()),
                "abandonedcountdown", isAbandoned() ? UtilTime.format(getCreated() + TimeUnit.MINUTES.toMillis(Config.Empire.Upkeep.ABANDON_TIME) - System.currentTimeMillis()) : "",
                "hostilespawning", String.valueOf(getMode(PlotMode.mobSpawning)),
                "passivespawning", String.valueOf(getMode(PlotMode.passiveSpawning)),
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
                "members", configMessages != null ? getMembersToString(configMessages) : "",
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

    private String getMembersToString(ConfigMessages configMessages) {
        if (members.isEmpty()) {
            return "";
        }
        Multimap<VillageState, Village> members = MultimapBuilder.enumKeys(VillageState.class).arrayListValues().build();
        for (Village member : getMembers()) {
            members.put(member.getState(), member);
        }
        Message stateMessage = configMessages.getMessage("members.state");
        Message entryMessage = configMessages.getMessage("members.entry");
        String separator = configMessages.getMessage("members.separator").getText();
        List<String> lines = new ArrayList<>();
        for (VillageState state : VillageState.values()) {
            if (!members.containsKey(state)) {
                continue;
            }
            lines.add(stateMessage.getReplacedText(
                    "state", state.getTag(),
                    "members", members.get(state).stream()
                            .sorted()
                            .map(v -> entryMessage.getReplacedText(
                                    "state", v.getStateTag(),
                                    "name", v.getName(),
                                    "level", String.valueOf(v.getLevel()),
                                    "residents", String.valueOf(v.getResidentIds().size())
                            )).collect(Collectors.joining(separator))
            ));
        }

        return String.join("\n", lines);
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
            lines.add(rankMessage.getReplacedText(
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
                            .limit(Config.PLAYERS_IN_INFO)
                            .map(res -> entry.getReplacedText(
                                    "online", res.getPlayer() != null ? online : offline,
                                    "name", res.getName(),
                                    "rank", rank.getTag(),
                                    "title", res.getVillageTitle(),
                                    "state", res.getVillage().getStateTag(),
                                    "settlement", res.getVillage().getName()
                            ))
                            .collect(Collectors.joining(separator))
            ));
        }
        return String.join("\n", lines);
    }
}

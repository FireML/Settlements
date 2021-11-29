package de.themoep.settlements.land;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.rank.Rank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ConfigKey;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.world.SimpleLocation;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.DatabaseSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class LandOwner<R extends Rank, L extends LevelInfo> implements DatabaseSerializable, Levelable<L>, Targetable, ModeAble, FlagAble, ConfigKey {
    private IdObject id = IdObject.get(null);
    private String name;
    private String description = "";
    private String discordLink = "";
    private long created = System.currentTimeMillis();
    private long bank = 0;
    private long joinFee = 0;
    private long tax = 0;
    private TaxDebtAction taxDebtAction = TaxDebtAction.KICK;
    private Map<PlotMode, Boolean> modes = new EnumMap<>(PlotMode.class);
    private int bonusClaims = 0;
    private Map<String, Long> vault = new HashMap<>();
    private Map<String, PerkData> perks = new LinkedHashMap<>();
    private SimpleLocation home = null;
    private BuildState buildState = getDefaultBuildState();
    private LandAccess access = LandAccess.PRIVATE;
    private Map<Use, BuildState> use = new EnumMap<>(Use.class);

    private Map<String, Map<String, Boolean>> ranksPermissions = new HashMap<>();
    protected Multimap<String, String> rankTitles = MultimapBuilder.hashKeys().linkedHashSetValues().build();
    protected Map<String, String> defaultRankTitles = new HashMap<>();

    private Set<IdObject> alliedSettlements = new LinkedHashSet<>();

    private Set<IdObject> alliedEmpires = new LinkedHashSet<>();
    private Set<UUID> banishedPlayers = new LinkedHashSet<>();
    private Set<IdObject> banishedSettlements = new LinkedHashSet<>();
    private Set<IdObject> banishedEmpires = new LinkedHashSet<>();

    protected LandOwner() {}

    public LandOwner(String name) {
        this.name = name;
    }

    public abstract List<Resident> getResidents();

    public abstract long getOnlineCount();

    public boolean canAdminLocks(Resident resident) {
        return hasPermission(resident, "lock.admin");
    }

    public boolean canAdminPlots(Resident resident) {
        return hasPermission(resident, "plot.admin");
    }

    public boolean canAdminPortals(Resident resident) {
        return hasPermission(resident, "portal.admin");
    }

    public abstract boolean hasPermission(Resident resident, String perm);

    public abstract boolean isTrusted(LandOwner landOwner);

    public abstract boolean isChief(UUID playerId);

    public abstract UUID getChiefId();

    public abstract boolean isOfficer(Player player);

    public abstract String getStateKey();

    public abstract Collection<OwnedLand> getLand();

    public IdObject getId() {
        return id;
    }

    public void setId(IdObject id) {
        if (id == null) {
            id = IdObject.get(null);
        }
        this.id = id;
    }

    public UUID getUUID() {
        return UUID.nameUUIDFromBytes(getIdKey().getBytes());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDiscord(String discordLink) {
        this.discordLink = discordLink;
    }

    public String getDiscord() {
        return discordLink;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public Map<String, PerkData> getPerksMap() {
        return perks;
    }

    public abstract Perk getPerk(String name);

    public boolean hasUnlocked(Perk perk) {
        return getPerksMap().containsKey(perk.name().toLowerCase());
    }

    public Collection<Perk> getPerks() {
        return getPerksMap().keySet().stream()
                .map(this::getPerk)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public PerkData getPerkData(Perk perk) {
        return getPerksMap().get(perk.getKey().toLowerCase());
    }

    public boolean canUnlock(Perk perk) {
        return !hasUnlocked(perk) && has(perk.getUnlockCost());
    }

    public boolean canRepeat(Perk perk) {
        return hasUnlocked(perk) && has(perk.getRepeatCost()) && checkPerkCount(perk);
    }

    public boolean checkPerkCount(Perk perk) {
        if (perk.getRepeat() > 0) {
            PerkData p = getPerkData(perk);
            if (p != null) {
                return p.getTimes() <= perk.getRepeat();
            }
            return true;
        }
        return perk.getRepeat() < 0;
    }

    public boolean checkPerkCooldown(Perk perk) {
        if (perk.getCooldown() > 0) {
            PerkData p = getPerkData(perk);
            if (p != null) {
                return p.getLastUsed() + perk.getCooldown() * 1000 < System.currentTimeMillis();
            }
        }
        return true;
    }

    public boolean unlockPerk(Player player, Perk perk) {
        if (!hasUnlocked(perk)) {
            getPerksMap().put(perk.name().toLowerCase(), new PerkData());
            apply(player, perk);
            return true;
        }
        return false;
    }

    public void apply(Player player, Perk perk) {
        if (perk.hasEffects()) {
            applyEffects(player, perk);
        }
        runCommands(perk);
    }

    public boolean repeatPerk(Player player, Perk perk) {
        PerkData p = getPerkData(perk);
        if (p != null) {
            p.addUse();
            apply(player, perk);
            return true;
        }
        return false;
    }

    public boolean togglePerk(Player player, Perk perk) {
        if (perk.hasEffects() || perk.hasActions()) {
            PerkData perkData = getPerkData(perk);
            if (perkData != null) {
                perkData.toggle();
                if (perkData.isActive()) {
                    applyEffects(player, perk);
                } else {
                    removeEffects(player, perk);
                }
                DatabaseManager.getInstance().save(this);
                return true;
            }
        }
        return false;
    }

    public void applyEffects(Player player, Perk perk) {
        SettlementsPlugin.getInstance().getLandPerks().updateEffects(false);
        perk.toggleActions(player, this, true);
    }

    public void removeEffects(Player player, Perk perk) {
        SettlementsPlugin.getInstance().getLandPerks().updateEffects(false);
        perk.toggleActions(player, this, false);
    }

    public Map<String, Map<String, Boolean>> getRanksPermissions() {
        return ranksPermissions;
    }

    protected abstract Set<String> getPermissions();

    public Map<String, Boolean> getPermissions(R rank) {
        return ranksPermissions.getOrDefault(rank.name().toLowerCase(), Collections.emptyMap());
    }

    public Map<String, Optional<Boolean>> getEffectivePermissions(R rank) {
        Map<String, Optional<Boolean>> map = new LinkedHashMap<>();
        for (String perm : getPermissions()) {
            map.put(perm, getEffectivePermissionValue(rank, perm));
        }
        return map;
    }

    public Optional<Boolean> getPermissionValue(R rank, String permission) {
        Map<String, Boolean> map = ranksPermissions.get(rank.name().toLowerCase());
        return Optional.ofNullable(map != null ? map.get(permission.toLowerCase()) : null);
    }

    public Optional<Boolean> getInheritedPermissionValue(R rank, String permission) {
        Optional<Boolean> value = getPermissionValue(rank, permission);
        if (value.isPresent()) {
            return value;
        }
        if (rank.getInherits() != null) {
            return getInheritedPermissionValue((R) rank.getInheritedRank(), permission);
        }
        return Optional.empty();
    }

    public Optional<Boolean> getEffectivePermissionValue(R rank, String permission) {
        if (rank.getParent() != null) {
            Optional<Boolean> inheritValue = getInheritedPermissionValue(rank, permission);
            if (inheritValue.isPresent()) {
                return inheritValue;
            }
        }
        return rank.getInheritedPermissionValue(permission);
    }

    public boolean setPermission(R rank, String permission, boolean value) {
        if (getEffectivePermissionValue(rank, permission).orElse(false) == value) {
            return false;
        }

        if (rank.getPermissionValue(permission) == value) {
            return setDefaultPermission(rank, permission);
        }

        Boolean prev = ranksPermissions.computeIfAbsent(rank.name().toLowerCase(), k -> new LinkedHashMap<>()).put(permission.toLowerCase(), value);
        return prev == null || prev != value;
    }

    public boolean setDefaultPermission(R rank, String permission) {
        return ranksPermissions.getOrDefault(rank.name().toLowerCase(), new LinkedHashMap<>()).remove(permission.toLowerCase()) != null;
    }

    public String getIdKey() {
        return getKey() + ":" + getId();
    }

    public BuildState getUseState(UseFlag useFlag, Use u) {
        return use.getOrDefault(u, useFlag != null
                ? useFlag.getDefault(getDefaultBuildState()) : getDefaultBuildState());
    }

    public Map<Use, BuildState> getUseStates() {
        return use;
    }

    @Override
    public Map<String, Long> getVault() {
        return vault;
    }

    public long getBank() {
        return bank;
    }

    public void setBank(long bank) {
        this.bank = bank;
    }

    public boolean setUseState(Use u, BuildState state) {
        if (state == null) {
            return use.remove(u) != null;
        } else {
            return use.put(u, state) != null;
        }
    }

    public boolean unsetUseState(UseFlag u) {
        boolean r = false;
        for (Use useType : u.getUseTypes()) {
            r |= use.remove(useType) != null;
        }
        return r;
    }

    public boolean hasUseState(Use u) {
        return use.containsKey(u);
    }

    public FlagAble getParent() {
        return new FlagAble() {
            @Override
            public void setBuildState(BuildState buildState) {

            }

            @Override
            public BuildState getBuildState() {
                return getDefaultBuildState();
            }

            @Override
            public BuildState getUseState(UseFlag useFlag, Use u) {
                return useFlag != null ? useFlag.getDefault(getDefaultBuildState()) : getDefaultBuildState();
            }

            @Override
            public Map<Use, BuildState> getUseStates() {
                return Collections.emptyMap();
            }

            @Override
            public boolean setUseState(Use u, BuildState state) {
                return false;
            }

            @Override
            public boolean unsetUseState(UseFlag u) {
                return false;
            }

            @Override
            public boolean hasUseState(Use u) {
                return false;
            }

            @Override
            public BuildState getDefaultBuildState() {
                return LandOwner.this.getDefaultBuildState();
            }

            @Override
            public BuildState.AppliesTo getBuildStateType() {
                return LandOwner.this.getBuildStateType();
            }

            @Override
            public void save() {

            }

            @Override
            public FlagAble getParent() {
                return null;
            }

            @Override
            public String getStateTag() {
                return LandOwner.this.getStateTag();
            }
        };
    }

    public void runCommands(Perk perk) {
        Utils.runAsync(() -> {
            List<String> commands = new ArrayList<>();
            for (String command : perk.getCommands()) {
                for (Resident resident : getResidents()) {
                    commands.add(MessageUtils.replacePlaceholders(command,
                            "player", resident.getName(),
                            "playerid", resident.getUUID().toString(),
                            "settlement", getName(),
                            "state", getStateTag(),
                            "level", LandOwner.this instanceof Settlement ? String.valueOf(((Settlement) LandOwner.this).getLevel()) : "-1",
                            "perk", perk.getTag()
                    ));
                }
            }
            Utils.runSync(() -> {
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            });
        });
    }

    public abstract String getStateTag();

    public abstract Resident getResident(String name);

    @Override
    public int getBonusClaims() {
        return bonusClaims;
    }

    @Override
    public void setBonusClaims(int bonusClaims) {
        this.bonusClaims = bonusClaims;
    }

    public boolean isBanished(UUID playerId) {
        if (banishedPlayers.contains(playerId)) {
            return true;
        }
        Resident resident = ResidentManager.getInstance().getResident(playerId);
        if (resident != null && resident.hasSettlement()) {
            if (banishedSettlements.contains(resident.getSettlement().getId())) {
                return true;
            }
            if (resident.hasEmpire() && banishedEmpires.contains(resident.getSettlement().getEmpire().getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean addBanished(Targetable target) {
        if (target instanceof Resident) {
            return banishedPlayers.add(((Resident) target).getUUID());
        } else if (target instanceof Settlement) {
            return banishedSettlements.add(((Settlement) target).getId());
        } else if (target instanceof Empire) {
            return banishedEmpires.add(((Empire) target).getId());
        }
        return false;
    }

    public boolean removeBanished(Targetable target) {
        if (target instanceof Resident) {
            return banishedPlayers.remove(((Resident) target).getUUID());
        } else if (target instanceof Settlement) {
            return banishedSettlements.remove(((Settlement) target).getId());
        } else if (target instanceof Empire) {
            return banishedEmpires.remove(((Empire) target).getId());
        }
        return false;
    }

    public boolean addAlly(LandOwner<?, ?> landOwner) {
        if (landOwner instanceof Settlement) {
            return alliedSettlements.add(landOwner.getId());
        } else if (landOwner instanceof Empire) {
            return alliedEmpires.add(landOwner.getId());
        }
        return false;
    }

    public boolean removeAlly(LandOwner<?, ?> landOwner) {
        if (landOwner instanceof Settlement) {
            return alliedSettlements.remove(landOwner.getId());
        } else if (landOwner instanceof Empire) {
            return alliedEmpires.remove(landOwner.getId());
        }
        return false;
    }

    public boolean isAlly(LandOwner<?, ?> landOwner) {
        if (landOwner instanceof Settlement) {
            return alliedSettlements.contains(landOwner.getId());
        } else if (landOwner instanceof Empire) {
            return alliedEmpires.contains(landOwner.getId());
        }
        return false;
    }

    public boolean isAlly(Resident resident) {
        return isMember(resident)
                || (resident.hasSettlement() && isAlly(resident.getSettlement()))
                || (resident.hasEmpire() && isAlly(resident.getSettlement().getEmpire()));
    }

    public boolean hasAllies() {
        return !getAlliedEmpireIds().isEmpty() || !getAlliedSettlementIds().isEmpty();
    }

    public boolean isTrusted(Resident resident) {
        return (resident.hasSettlement() && isTrusted(resident.getSettlement()))
                || (resident.hasEmpire() && isTrusted(resident.getSettlement().getEmpire()));
    }

    @Override
    public Map<PlotMode, Boolean> getModes() {
        return modes;
    }

    public SimpleLocation getHome() {
        return home;
    }

    public boolean hasHome() {
        return this.getHome() != null;
    }

    public Set<UUID> getBanishedPlayers() {
        return banishedPlayers;
    }

    public Set<IdObject> getBanishedSettlementIds() {
        return banishedSettlements;
    }

    public Set<Settlement> getBanishedSettlements() {
        return banishedSettlements.stream()
                .map(id -> SettlementManager.getInstance().getFromId(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<IdObject> getBanishedEmpireIds() {
        return banishedEmpires;
    }

    public Set<Empire> getBanishedEmpires() {
        return banishedEmpires.stream()
                .map(id -> EmpireManager.getInstance().getEmpireFromId(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<IdObject> getAlliedSettlementIds() {
        return alliedSettlements;
    }

    public Set<Settlement> getAlliedSettlements() {
        return alliedSettlements.stream()
                .map(id -> SettlementManager.getInstance().getFromId(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<IdObject> getAlliedEmpireIds() {
        return alliedEmpires;
    }

    public Set<Empire> getAlliedEmpires() {
        return alliedEmpires.stream()
                .map(id -> EmpireManager.getInstance().getEmpireFromId(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Collection<LandOwner> getAllies() {
        List<LandOwner> landOwners = new ArrayList<>();
        landOwners.addAll(getAlliedEmpires());
        landOwners.addAll(getAlliedSettlements());
        return landOwners;
    }

    public void setHome(SimpleLocation home) {
        this.home = home;
    }

    public void setHome(Location location) {
        setHome(new SimpleLocation(location));
    }

    public long getJoinFee() {
        return joinFee;
    }

    public void setJoinFee(long joinFee) {
        this.joinFee = joinFee;
    }

    public abstract long getTaxIncome();

    public long getTax() {
        return tax;
    }

    public void setTax(long tax) {
        this.tax = tax;
    }

    public TaxDebtAction getTaxDebtAction() {
        return taxDebtAction;
    }

    public void setTaxDebtAction(TaxDebtAction taxDebtAction) {
        this.taxDebtAction = taxDebtAction;
    }

    public void setBuildState(BuildState buildState) {
        this.buildState = buildState;
    }

    public BuildState getBuildState() {
        return this.buildState;
    }

    public LandAccess getAccess() {
        return access;
    }

    public void setAccess(LandAccess access) {
        this.access = access;
    }

    public abstract void removeMember(Targetable target);

    public boolean isChief(Targetable target) {
        if (target instanceof Resident) {
            return isChief(((Resident) target).getUUID());
        }
        return false;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public Collection<String> getTitles(R rank) {
        Collection<String> titles = rankTitles.get(rank.name().toLowerCase());
        if (titles.isEmpty()) {
            return rank.getTitles(this);
        }
        return titles;
    }

    public boolean addTitle(R rank, String title) {
        if (rankTitles.get(rank.name().toLowerCase()).isEmpty()) {
            if (getTitle(rank, title) != null) {
                return false;
            } else {
                rankTitles.putAll(rank.name().toLowerCase(), rank.getTitles(this));
            }
        }
        if (rankTitles.put(rank.name().toLowerCase(), title)) {
            DatabaseManager.getInstance().save(this);
            return true;
        }
        return false;
    }

    public void setDefaultTitle(R rank, String title) {
        String t = getTitle(rank, title);
        defaultRankTitles.put(rank.name().toLowerCase(), title);
        if (t == null) {
            addTitle(rank, title);
        } else {
            DatabaseManager.getInstance().save(this);
        }
    }

    public String getDefaultTitle(R rank) {
        return defaultRankTitles.getOrDefault(rank.name(), getTitles(rank).iterator().next());
    }

    public boolean removeTitle(R rank, String title) {
        if (rankTitles.get(rank.name().toLowerCase()).isEmpty()) {
            if (getTitle(rank, title) != null) {
                rankTitles.putAll(rank.name().toLowerCase(), rank.getTitles(this));
            } else {
                return false;
            }
        }
        String titleCase = getTitle(rank, title);
        if (rankTitles.remove(rank.name().toLowerCase(), titleCase)) {
            if (getDefaultTitle(rank).equals(title)) {
                defaultRankTitles.remove(rank.name().toLowerCase());
            }
            DatabaseManager.getInstance().save(this);
            return true;
        }
        return false;
    }

    public String getTitle(R rank, String title) {
        for (String rankTitle : getTitles(rank)) {
            if (rankTitle.equalsIgnoreCase(title)) {
                return rankTitle;
            }
        }
        return null;
    }

    public Map<String, Collection<String>> getRankTitles() {
        return rankTitles.asMap();
    }

    public Map<String, String> getDefaultRankTitles() {
        return defaultRankTitles;
    }

    @Override
    public String getDbDescription() {
        return getStateKey() + " " + getName() + "/" + getId();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId().value());
        map.put("name", getName());
        map.put("description", getDescription());
        map.put("discord", getDiscord());
        map.put("created", getCreated());
        for (PlotMode mode : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            map.put(mode.name(), getMode(mode));
        }
        map.put("bank", getBank());
        map.put("joinFee", getJoinFee());
        map.put("tax", getTax());
        map.put("taxDebtAction", getTaxDebtAction().name());
        map.put("bonusClaims", getBonusClaims());
        map.put("buildState", getBuildState().name());
        map.put("access", getAccess().name());
        if (getHome() != null)
            map.put("homeLoc", getHome());
        else
            map.put("homeLoc", new SimpleLocation(null, 0, 0, 0));
        List<String> allies = new ArrayList<>();
        getAlliedSettlementIds().stream().map(id -> "settlement:" + id).forEach(allies::add);
        getAlliedEmpireIds().stream().map(id -> "empire:" + id).forEach(allies::add);
        map.put("allies", allies);
        List<String> banished = new ArrayList<>();
        getBanishedPlayers().stream().map(UUID::toString).forEach(banished::add);
        getBanishedSettlementIds().stream().map(id -> "settlement:" + id).forEach(banished::add);
        getBanishedEmpireIds().stream().map(id -> "empire:" + id).forEach(banished::add);
        map.put("banished", banished);
        map.put("uses", getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
        map.put("vault", getVault());
        map.put("perks", getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
        map.put("permissions", getRanksPermissions());
        map.put("titles", getRankTitles());
        map.put("defaulttiles", getDefaultRankTitles());
        return map;
    }

    public static <T extends LandOwner> T deserialize(T landOwner, Map<String, Object> map) {
        if (map == null || map.get("name") == null || map.get("buildState") == null) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize LandOwner! (" + map + ")");
            return null;
        }
        landOwner.setName((String) map.get("name"));
        landOwner.setDescription((String) map.get("description"));
        landOwner.setDiscord((String) map.get("discord"));
        landOwner.setCreated(Utils.getLong(map.get("created")));
        landOwner.setId(IdObject.get(map));
        landOwner.setBank(Utils.getLong(map.get("bank")));
        landOwner.setJoinFee(Utils.getLong(map.get("joinFee")));
        landOwner.setTax(Utils.getLong(map.get("tax")));
        try {
            landOwner.setTaxDebtAction(TaxDebtAction.valueOf((String) map.get("taxDebtAction")));
        } catch (IllegalArgumentException ignored) {}
        for (PlotMode mode : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            landOwner.setMode(mode, Utils.getBoolean(map.getOrDefault(mode.name(), mode.getDefault())));
        }
        landOwner.setBonusClaims((Integer) map.get("bonusClaims"));
        try {
            landOwner.setBuildState(BuildState.valueOf((String) map.get("buildState")));
        } catch (IllegalArgumentException ignored) {}
        try {
            landOwner.setAccess(LandAccess.valueOf((String) map.get("access")));
        } catch (IllegalArgumentException ignored) {}
        if (map.get("homeLoc") != null) {
            landOwner.setHome(SimpleLocation.deserialize((Map<String, Object>) map.get("homeLoc")));
        }
        Map<String, Object> uses = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            UseFlag useFlag = UseFlagManager.getInstance().get(u);
            if (uses.containsKey(u.name()) && useFlag != null) {
                landOwner.setUseState(u, Utils.parseEnum(BuildState.class, (String) uses.get(u.name()), useFlag.getDefault(landOwner.getDefaultBuildState())));
            }
        }
        for (String s : ((Collection<String>) map.get("allies"))) {
            String[] split = s.split(":", 2);
            try {
                if (split.length > 1) {
                    IdObject id = IdObject.get(Integer.parseInt(split[1]));
                    switch (split[0]) {
                        case "settlement":
                            landOwner.getAlliedSettlementIds().add(id);
                            break;
                        case "empire":
                            landOwner.getAlliedEmpireIds().add(id);
                            break;
                    }
                }
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Ally entry " + s + " could not be parsed for " + landOwner.getStateKey() + " " + landOwner.getName() + "/" + landOwner.getId() + "! " + e.getMessage());
            }
        }
        for (String s : ((Collection<String>) map.get("banished"))) {
            String[] split = s.split(":", 2);
            try {
                if (split.length == 1) {
                    landOwner.getBanishedPlayers().add(UUID.fromString(split[0]));
                } else {
                    IdObject id = IdObject.get(Integer.parseInt(split[1]));
                    switch (split[0]) {
                        case "settlement":
                            landOwner.getBanishedSettlementIds().add(id);
                            break;
                        case "empire":
                            landOwner.getBanishedEmpireIds().add(id);
                            break;
                    }
                }
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Banish entry " + s + " could not be parsed for " + landOwner.getStateKey() + " " + landOwner.getName() + "/" + landOwner.getId() + "! " + e.getMessage());
            }
        }
        ((Map<String, Object>) map.get("vault")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Number)
                .forEach(e -> landOwner.getVault().put(e.getKey(), Long.parseLong(String.valueOf(e.getValue()))));
        ((Map<String, Object>) map.get("perks")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Map)
                .forEach(e -> landOwner.getPerksMap().put(e.getKey().toLowerCase(), PerkData.deserialize((Map<String, Object>) e.getValue())));
        ((Map<String, Object>) map.get("permissions")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Map)
                .forEach(e -> landOwner.getRanksPermissions().put(e.getKey().toLowerCase(), (Map<String, Boolean>) e.getValue()));
        for (Map.Entry<String, Collection<String>> entry : ((Map<String, Collection<String>>) map.get("titles")).entrySet()) {
            landOwner.rankTitles.putAll(entry.getKey().toLowerCase(), entry.getValue());
        }
        ((Map<String, Object>) map.get("defaulttitles")).entrySet().stream()
                .forEach(e -> landOwner.defaultRankTitles.put(e.getKey().toLowerCase(), (String) e.getValue()));
        return landOwner;
    }

    public abstract long getUpkeep();

    public abstract boolean isAbandoned();

    public abstract long getAbandonTimeConfig();

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass().equals(getClass()) && ((LandOwner) o).getId().equals(getId());
    }
}

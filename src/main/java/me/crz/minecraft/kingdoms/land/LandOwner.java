package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.DatabaseSerializable;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.perk.Vaultable;
import me.crz.minecraft.kingdoms.rank.Rank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
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

public abstract class LandOwner implements DatabaseSerializable, Vaultable, Targetable, ModeAble {
    private IdObject id = IdObject.get(null);
    private String name;
    private String description = "";
    private long created = System.currentTimeMillis();
    private long bank = 0;
    private long joinFee = 0;
    private long tax = 0;
    private Map<PlotMode, Boolean> modes = new EnumMap<>(PlotMode.class);
    private int bonusClaims = 0;
    private Map<String, Long> vault = new HashMap<>();
    private Map<String, PerkData> perks = new LinkedHashMap<>();
    private SimpleLocation home = null;
    private BuildState buildState = getDefaultBuildState();
    private LandAccess access = LandAccess.PRIVATE;
    private Map<Use, BuildState> use = new EnumMap<>(Use.class);

    private Map<String, Map<String, Boolean>> ranksPermissions = new HashMap<>();

    private Set<IdObject> alliedVillages = new LinkedHashSet<>();

    private Set<IdObject> alliedEmpires = new LinkedHashSet<>();
    private Set<UUID> banishedPlayers = new LinkedHashSet<>();
    private Set<IdObject> banishedVillages = new LinkedHashSet<>();
    private Set<IdObject> banishedEmpires = new LinkedHashSet<>();
    protected LandOwner() {}

    public LandOwner(String name) {
        this.name = name;
    }

    public abstract List<Resident> getResidents();

    public abstract long getOnlineCount();

    public abstract boolean canAdminLocks(Resident resident);

    public abstract boolean canAdminPlots(Resident resident);

    public abstract boolean isTrusted(LandOwner landOwner);

    public abstract boolean isChief(UUID playerId);

    public abstract UUID getChiefId();

    public abstract boolean isOfficer(Player player);

    public abstract String getStateKey();

    public IdObject getId() {
        return id;
    }

    public void setId(IdObject id) {
        if (id == null) {
            id = IdObject.get(null);
        }
        this.id = id;
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

    public boolean unlockPerk(Perk perk) {
        if (!hasUnlocked(perk)) {
            getPerksMap().put(perk.name().toLowerCase(), new PerkData());
            apply( perk);
            return true;
        }
        return false;
    }

    public void apply(Perk perk) {
        if (perk.hasEffects()) {
            applyEffects(perk);
        }
        runCommands(perk);
    }

    public boolean repeatPerk(Perk perk) {
        PerkData p = getPerkData(perk);
        if (p != null) {
            p.addUse();
            apply(perk);
            return true;
        }
        return false;
    }

    public boolean togglePerk(Perk perk) {
        if (perk.hasEffects()) {
            PerkData perkData = getPerkData(perk);
            if (perkData != null) {
                perkData.toggle();
                if (perkData.isActive()) {
                    applyEffects(perk);
                } else {
                    removeEffects(perk);
                }
                Utils.runAsync(() -> DatabaseManager.getInstance().save(this));
                return true;
            }
        }
        return false;
    }

    public void applyEffects(Perk perk) {
        Base.getLandPerks().updateEffects(false);
        perk.toggleActions(this, false);
    }

    public void removeEffects(Perk perk) {
        Base.getLandPerks().updateEffects(false);
        perk.toggleActions(this, true);
    }

    public Map<String, Map<String, Boolean>> getRanksPermissions() {
        return ranksPermissions;
    }

    protected abstract Set<String> getPermissions();

    public Map<String, Boolean> getPermissions(Rank rank) {
        return ranksPermissions.getOrDefault(rank.name().toLowerCase(), Collections.emptyMap());
    }

    public Map<String, Optional<Boolean>> getEffectivePermissions(Rank rank) {
        Map<String, Optional<Boolean>> map = new LinkedHashMap<>();
        for (String perm : getPermissions()) {
            map.put(perm, getEffectivePermissionValue(rank, perm));
        }
        return map;
    }

    public Optional<Boolean> getPermissionValue(Rank rank, String permission) {
        Map<String, Boolean> map = ranksPermissions.get(rank.name().toLowerCase());
        return Optional.ofNullable(map != null ? map.get(permission.toLowerCase()) : null);
    }

    public Optional<Boolean> getInheritedPermissionValue(Rank rank, String permission) {
        Optional<Boolean> value = getPermissionValue(rank, permission);
        if (value.isPresent()) {
            return value;
        }
        if (rank.getInherits() != null) {
            return getInheritedPermissionValue(rank.getInheritedRank(), permission);
        }
        return Optional.empty();
    }

    public Optional<Boolean> getEffectivePermissionValue(Rank rank, String permission) {
        if (rank.getParent() != null) {
            Optional<Boolean> inheritValue = getInheritedPermissionValue(rank, permission);
            if (inheritValue.isPresent()) {
                return inheritValue;
            }
        }
        return rank.getInheritedPermissionValue(permission);
    }

    public boolean setPermission(Rank rank, String permission, boolean value) {
        if (getEffectivePermissionValue(rank, permission).orElse(false) == value) {
            return false;
        }

        if (rank.getPermissionValue(permission) == value) {
            return setDefaultPermission(rank, permission);
        }

        Boolean prev = ranksPermissions.computeIfAbsent(rank.name().toLowerCase(), k -> new LinkedHashMap<>()).put(permission.toLowerCase(), value);
        return prev == null || prev != value;
    }

    public boolean setDefaultPermission(Rank rank, String permission) {
        return ranksPermissions.getOrDefault(rank.name().toLowerCase(), new LinkedHashMap<>()).remove(permission.toLowerCase()) != null;
    }

    public String getIdKey() {
        return getClass().getSimpleName().toLowerCase() + ":" + getId();
    }

    public BuildState getUseState(Use u) {
        return use.getOrDefault(u, u.getDefault(getDefaultBuildState()));
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

    public void setUseState(Use u, BuildState state) {
        if (state == u.getDefault(getDefaultBuildState())) {
            use.remove(u);
        } else {
            use.put(u, state);
        }
    }

    public abstract BuildState getDefaultBuildState();

    public abstract BuildState.AppliesTo getBuildStateType();

    public void runCommands(me.crz.minecraft.kingdoms.perk.Perk perk) {
        Utils.runAsync(() -> {
            List<String> commands = new ArrayList<>();
            for (String command : perk.getCommands()) {
                for (Resident resident : getResidents()) {
                    commands.add(MessageUtils.replacePlaceholders(command,
                            "player", resident.getName(),
                            "playerid", resident.getUUID().toString(),
                            "settlement", getName(),
                            "state", getStateTag(),
                            "level", LandOwner.this instanceof Village ? String.valueOf(((Village) LandOwner.this).getLevel()) : "-1",
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
        if (resident != null && resident.hasVillage()) {
            if (banishedVillages.contains(resident.getVillage().getId())) {
                return true;
            }
            if (resident.hasEmpire() && banishedEmpires.contains(resident.getVillage().getEmpire().getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean addBanished(Targetable target) {
        if (target instanceof Resident) {
            return banishedPlayers.add(((Resident) target).getUUID());
        } else if (target instanceof Village) {
            return banishedVillages.add(((Village) target).getId());
        } else if (target instanceof Empire) {
            return banishedEmpires.add(((Empire) target).getId());
        }
        return false;
    }

    public boolean removeBanished(Targetable target) {
        if (target instanceof Resident) {
            return banishedPlayers.remove(((Resident) target).getUUID());
        } else if (target instanceof Village) {
            return banishedVillages.remove(((Village) target).getId());
        } else if (target instanceof Empire) {
            return banishedEmpires.remove(((Empire) target).getId());
        }
        return false;
    }

    public boolean addAlly(LandOwner landOwner) {
        if (landOwner instanceof Village) {
            return alliedVillages.add(landOwner.getId());
        } else if (landOwner instanceof Empire) {
            return alliedEmpires.add(landOwner.getId());
        }
        return false;
    }

    public boolean removeAlly(LandOwner landOwner) {
        if (landOwner instanceof Village) {
            return alliedVillages.remove(landOwner.getId());
        } else if (landOwner instanceof Empire) {
            return alliedEmpires.remove(landOwner.getId());
        }
        return false;
    }

    public boolean isAlly(LandOwner landOwner) {
        if (landOwner instanceof Village) {
            return alliedVillages.contains(landOwner.getId());
        } else if (landOwner instanceof Empire) {
            return alliedEmpires.contains(landOwner.getId());
        }
        return false;
    }

    public boolean isAlly(Resident resident) {
        return (resident.hasVillage() && isAlly(resident.getVillage()))
                || (resident.hasEmpire() && isAlly(resident.getVillage().getEmpire()));
    }

    public boolean hasAllies() {
        return !getAlliedEmpireIds().isEmpty() || !getAlliedVillageIds().isEmpty();
    }

    public boolean isTrusted(Resident resident) {
        return (resident.hasVillage() && isTrusted(resident.getVillage()))
                || (resident.hasEmpire() && isTrusted(resident.getVillage().getEmpire()));
    }

    @Override
    public Map<PlotMode, Boolean> getModes() {
        return modes;
    }

    public SimpleLocation getHome() {
        return home;
    }

    public Set<UUID> getBanishedPlayers() {
        return banishedPlayers;
    }

    public Set<IdObject> getBanishedVillageIds() {
        return banishedVillages;
    }

    public Set<Village> getBanishedVillages() {
        return banishedVillages.stream()
                .map(id -> VillageManager.getInstance().getVillageFromId(id))
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

    public Set<IdObject> getAlliedVillageIds() {
        return alliedVillages;
    }

    public Set<Village> getAlliedVillages() {
        return alliedVillages.stream()
                .map(id -> VillageManager.getInstance().getVillageFromId(id))
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
        landOwners.addAll(getAlliedVillages());
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
        map.put("created", getCreated());
        for (PlotMode mode : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            map.put(mode.name(), getMode(mode));
        }
        map.put("bank", getBank());
        map.put("joinFee", getJoinFee());
        map.put("tax", getTax());
        map.put("bonusClaims", getBonusClaims());
        map.put("buildState", getBuildState().name());
        map.put("access", getAccess().name());
        if (getHome() != null)
            map.put("homeLoc", getHome());
        else
            map.put("homeLoc", new SimpleLocation(null, 0, 0, 0));
        List<String> allies = new ArrayList<>();
        getAlliedVillageIds().stream().map(id -> "village:" + id).forEach(allies::add);
        getAlliedEmpireIds().stream().map(id -> "empire:" + id).forEach(allies::add);
        map.put("allies", allies);
        List<String> banished = new ArrayList<>();
        getBanishedPlayers().stream().map(UUID::toString).forEach(banished::add);
        getBanishedVillageIds().stream().map(id -> "village:" + id).forEach(banished::add);
        getBanishedEmpireIds().stream().map(id -> "empire:" + id).forEach(banished::add);
        map.put("banished", banished);
        map.put("uses", getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
        map.put("vault", getVault());
        map.put("perks", getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
        map.put("permissions", getRanksPermissions());
        return map;
    }

    public static <T extends LandOwner> T deserialize(T landOwner, Map<String, Object> map) {
        if (map == null || map.get("name") == null || map.get("buildState") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize LandOwner! (" + map + ")");
            return null;
        }
        landOwner.setName((String) map.get("name"));
        landOwner.setDescription((String) map.get("description"));
        landOwner.setCreated((Long) map.get("created"));
        landOwner.setId(IdObject.get(map));
        landOwner.setBank((Long) map.get("bank"));
        landOwner.setJoinFee((Long) map.get("joinFee"));
        landOwner.setTax((Long) map.get("tax"));
        for (PlotMode mode : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            landOwner.setMode(mode, (Boolean) map.getOrDefault(mode.name(), mode.getDefault()));
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
            if (uses.containsKey(u.name())) {
                landOwner.setUseState(u, Utils.parseEnum((String) uses.get(u.name()), u.getDefault(u.getDefault(landOwner.getDefaultBuildState()))));
            }
        }
        for (String s : ((Collection<String>) map.get("allies"))) {
            String[] split = s.split(":", 2);
            try {
                if (split.length > 1) {
                    IdObject id = IdObject.get(Integer.parseInt(split[1]));
                    switch (split[0]) {
                        case "village": landOwner.getAlliedVillageIds().add(id);
                        case "empire": landOwner.getAlliedEmpireIds().add(id);
                    }
                }
            } catch (IllegalArgumentException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Ally entry " + s + " could not be parsed for " + landOwner.getStateKey() + " " + landOwner.getName() + "/" + landOwner.getId() + "! " + e.getMessage());
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
                        case "village": landOwner.getBanishedVillageIds().add(id);
                        case "empire": landOwner.getBanishedEmpireIds().add(id);
                    }
                }
            } catch (IllegalArgumentException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Banish entry " + s + " could not be parsed for " + landOwner.getStateKey() + " " + landOwner.getName() + "/" + landOwner.getId() + "! " + e.getMessage());
            }
        }
        ((Map<String, Object>) map.get("vault")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Number)
                .forEach(e -> landOwner.getVault().put(e.getKey(), (Long) e.getValue()));
        ((Map<String, Object>) map.get("perks")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Map)
                .forEach(e -> landOwner.getPerksMap().put(e.getKey(), PerkData.deserialize((Map<String, Object>) e.getValue())));
        ((Map<String, Object>) map.get("permissions")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Map)
                .forEach(e -> landOwner.getRanksPermissions().put(e.getKey(), (Map<String, Boolean>) e.getValue()));
        return landOwner;
    }
}

package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.perk.Vaultable;
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
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class LandOwner implements ConfigurationSerializable, Vaultable, Targetable {
    private IdObject id = IdObject.get(null);
    private String name;
    private String description = "";
    private long bank = 0;
    private long joinFee = 0;
    private long tax = 0;
    private boolean pvp = false;
    private boolean fireSpread = false;
    private boolean explosions = false;
    private boolean mobSpawning = false;
    private boolean buff = true;
    private int bonusClaims = 0;
    private Map<String, Long> vault = new HashMap<>();
    private Map<String, PerkData> perks = new LinkedHashMap<>();
    private SimpleLocation home = null;
    private BuildState buildState = BuildState.PRIVATE;
    private LandAccess access = LandAccess.PRIVATE;
    private Map<Use, BuildState> use = new EnumMap<>(Use.class);

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
            apply(perk);
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
    }

    public void removeEffects(Perk perk) {
        Base.getLandPerks().updateEffects(false);
    }

    public String getIdKey() {
        return getClass().getSimpleName().toLowerCase() + ":" + getId();
    }

    public BuildState getUseState(Use u) {
        return use.getOrDefault(u, u.getDefault(BuildState.PRIVATE));
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
        if (state == u.getDefault(BuildState.PRIVATE)) {
            use.remove(u);
        } else {
            use.put(u, state);
        }
    }

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
            return false;
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

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }

    public boolean isMobSpawning() {
        return mobSpawning;
    }

    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }

    public boolean isBuff() {
        return buff;
    }

    public void setBuff(boolean buff) {
        this.buff = buff;
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

    public void removeMember(Targetable target) {
        if (target instanceof LandOwner) {
            removeAlly((LandOwner) target);
        }
    }

    public boolean isChief(Targetable target) {
        if (target instanceof Resident) {
            return isChief(((Resident) target).getUUID());
        }
        return false;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId().value());
        map.put("name", getName());
        map.put("description", getDescription());
        map.put("pvp", isPvp());
        map.put("fireSpread", isFireSpread());
        map.put("explosions", isExplosions());
        map.put("mobSpawning", isMobSpawning());
        map.put("buff", isBuff());
        map.put("bank", getBank());
        map.put("joinFee", getJoinFee());
        map.put("tax", getTax());
        map.put("bonusClaims", getBonusClaims());
        map.put("buildState", getBuildState().name());
        map.put("access", getAccess().name());
        if (getHome() != null)
            map.put("homeLoc", getHome().serialize());
        List<String> banished = new ArrayList<>();
        getBanishedPlayers().stream().map(UUID::toString).forEach(banished::add);
        getBanishedVillageIds().stream().map(id -> "village:" + id).forEach(banished::add);
        getBanishedEmpireIds().stream().map(id -> "empire:" + id).forEach(banished::add);
        map.put("banished", banished);
        map.put("uses", getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
        map.put("vault", getVault());
        map.put("perks", getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
        return map;
    }

    public static <T extends LandOwner> T deserialize(T landOwner, Map<String, Object> map) {
        if (map == null || map.get("name") == null || map.get("buildState") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize LandOwner! (" + map + ")");
            return null;
        }
        landOwner.setName((String) map.get("name"));
        landOwner.setDescription((String) map.get("description"));
        landOwner.setId(IdObject.get(map));
        landOwner.setBank((Long) map.get("bank"));
        landOwner.setJoinFee((Long) map.get("joinFee"));
        landOwner.setTax((Long) map.get("tax"));
        landOwner.setPvp((Boolean) map.get("pvp"));
        landOwner.setFireSpread((Boolean) map.get("fireSpread"));
        landOwner.setExplosions((Boolean) map.get("explosions"));
        landOwner.setMobSpawning((Boolean) map.get("mobSpawning"));
        landOwner.setBuff((Boolean) map.get("buff"));
        landOwner.setBonusClaims((Integer) map.get("bonusClaims"));
        try {
            landOwner.setBuildState(BuildState.valueOf((String) map.get("buildState")));
        } catch (IllegalArgumentException ignored) {}
        try {
            landOwner.setAccess(LandAccess.valueOf((String) map.get("access")));
        } catch (IllegalArgumentException ignored) {}
        if (map.get("homeLoc") != null) {
            landOwner.setHome(SimpleLocation.deserialize((Map<String, Object>) map.get("homeLoc")));
        } else if (map.get("homeLoc_x") != null && map.get("homeLoc_y") != null && map.get("homeLoc_Z") != null
                && map.get("homeLoc_world") != null && map.get("homeLoc_yaw") != null && map.get("homeLoc_pitch") != null){
            landOwner.setHome(new SimpleLocation(
                    (String) map.get("homeLoc_world"),
                    (double) map.get("homeLoc_x"),
                    (double) map.get("homeLoc_y"),
                    (double) map.get("homeLoc_z"),
                    (float) map.get("homeLoc_yaw"),
                    (float) map.get("homeLoc_pitch")
            ));
        }
        Map<String, Object> uses = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            if (uses.containsKey(u.name())) {
                landOwner.setUseState(u, Utils.parseEnum((String) uses.get(u.name()), u.getDefault(BuildState.PRIVATE)));
            }
        }
        ((Collection<String>) map.get("banished")).stream().filter(s -> !s.contains(":")).map(UUID::fromString).forEach(landOwner.getBanishedPlayers()::add);
        ((Collection<String>) map.get("banished")).stream()
                .filter(s -> s.startsWith("village:"))
                .map(s -> {
                    String sub = s.split(":")[1];
                    try {
                        return IdObject.get(Integer.parseInt(sub));
                    } catch (NumberFormatException ignored) {}
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(landOwner.getBanishedVillageIds()::add);
        ((Collection<String>) map.get("banished")).stream()
                .filter(s -> s.startsWith("empire:"))
                .map(s -> {
                    String sub = s.split(":")[1];
                    try {
                        return IdObject.get(Integer.parseInt(sub));
                    } catch (NumberFormatException ignored) {}
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(landOwner.getBanishedEmpireIds()::add);
        ((Map<String, Object>) map.get("vault")).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof Integer))
                .forEach(e -> landOwner.getVault().put(e.getKey(), (Long) e.getValue()));
        ((Map<String, Object>) map.get("perks")).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof Map))
                .forEach(e -> landOwner.getPerksMap().put(e.getKey(), PerkData.deserialize((Map<String, Object>) e.getValue())));

        return landOwner;
    }
}

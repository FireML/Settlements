package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.BonusClaimsHolder;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.bukkit.Location;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.land.LandManager;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.crz.minecraft.kingdoms.landflags.Use;

import java.util.List;

import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Village implements ConfigurationSerializable, Comparable<Village>, BonusClaimsHolder {
    private IdObject id = IdObject.get(null);
    private String name;
    private String description;
    private int level;
    private UUID chief;
    private VillageState villageState;
    private BuildState buildState;
    private boolean pvp = false;
    private boolean fireSpread = false;
    private boolean explosions = false;
    private boolean mobSpawning = false;
    private boolean buff = false;
    private VillageAccess access = VillageAccess.ALLY;
    private long bank = 0;
    private Set<IdObject> allies = new LinkedHashSet<>();
    private SimpleLocation villageHomeLoc;
    private Map<String, NamedLocation> outpostHomes = new LinkedHashMap<>();
    private Set<UUID> residents = new LinkedHashSet<>();
    private Set<UUID> banished = new LinkedHashSet<>();
    private Map<Use, BuildState> use = new EnumMap<>(Use.class);
    private long lastUpkeep = System.currentTimeMillis();
    private Map<String, Integer> vault = new HashMap<>();
    private int bonusClaims = 0;

    public Village(String name, UUID chief, int level, VillageState state, BuildState buildState) {
        this.name = name;
        residents.add(chief);
        this.chief = chief;
        this.level = level;
        this.villageState = state;
        this.buildState = buildState;
    }

    public void doUpkeep() {
        if (System.currentTimeMillis() > lastUpkeep + TimeUnit.DAYS.toMillis(1L)) {
            this.payUpkeep();
        }
    }

    public void payUpkeep() {
        int total = getUpkeep();
        if (!canPay(total)) {
            if (getLevel() > 1 || getState() == VillageState.KINGDOM) {
                if (getLevel() == 1 && getState() == VillageState.KINGDOM) {
                    setState(VillageState.VILLAGE);
                    setLevel(Levels.VILLAGE_TO_KINGDOM_LEVEL);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.downgraded"), "state", getState().getTag(), "level", String.valueOf(getLevel()));
                } else {
                    setLevel(getLevel() - 1);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.level-removed"), "level", String.valueOf(getLevel()));
                }
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(this));
            } else {
                setBank(0);
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.deleted"));
                VillageManager.getInstance().deleteVillage(this);
            }
        } else {
            setBank(getBank() - total);
            MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.charged"), "charged", String.valueOf(total));
            setLastUpkeep(System.currentTimeMillis());
            if (!canPay(total)) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.cant-pay"), "cost", String.valueOf(total));
            }
        }
    }

    public int getUpkeep() {
        return LandManager.getInstance().getClaimedLandSize(this) * KingdomsPlugin.UPKEEP;
    }

    public boolean canPay(int total) {
        return this.getBank() - total >= 0;
    }

    public long getLastUpkeep() {
        return lastUpkeep;
    }

    public void setLastUpkeep(long lastUpkeep) {
        this.lastUpkeep = lastUpkeep;
    }

    public BuildState getUseState(Use u) {
        return use.getOrDefault(u, BuildState.RESIDENTS);
    }

    public void setUseState(Use u, BuildState state) {
        use.put(u, state);
    }

    public Map<Use, BuildState> getUseStates() {
        return use;
    }

    public Set<UUID> getBanishedResidents() {
        return banished;
    }

    public void addBanishedResident(Resident res) {
        if (!this.isBanished(res.getUUID())) {
            banished.add(res.getUUID());
        }
    }

    public void removeBanishedResident(Resident res) {
        if (this.isBanished(res.getUUID())) {
            banished.remove(res.getUUID());
        }
    }

    public boolean isBanished(UUID uuid) {
        return banished.contains(uuid);
    }

    public void addOutpostHome(String name, Location loc) {
        addOutpostHome(new NamedLocation(name, loc));
    }

    public void addOutpostHome(NamedLocation location) {
        outpostHomes.values().removeIf(location::inSameChunk);
        outpostHomes.put(location.getName().toLowerCase(), location);
    }

    public Map<String, NamedLocation> getOutpostHomes() {
        return outpostHomes;
    }

    public SimpleLocation getVillageHome() {
        return villageHomeLoc;
    }

    public boolean hasVillageHome() {
        return this.getVillageHome() != null;
    }

    public void setVillageHome(Location location) {
        setVillageHome(new SimpleLocation(location));
    }

    public void setVillageHome(SimpleLocation loc) {
        villageHomeLoc = loc;
    }

    public Set<IdObject> getAllies() {
        return allies;
    }

    public boolean isAlly(Village village) {
        return allies.contains(village.getId());
    }

    public void addAlly(Village village) {
        allies.add(village.getId());
    }

    public void removeAlly(Village village) {
        allies.remove(village.getId());
    }

    public VillageAccess getAccess() {
        return access;
    }

    public void setAccess(VillageAccess access) {
        this.access = access;
    }

    public long getBank() {
        return bank;
    }

    public void setBank(long bank) {
        this.bank = bank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        if (Data.first.getString(villageState.getKey() + "-" + level).equals("")) {
            Data.first.put(villageState.getKey() + "-" + level, this.getName());
            KingdomsPlugin.data.save(KingdomsPlugin.getInstance().getDataFolder());
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, Message.get("first-to-reach-level"),
                        "name", this.getName(),
                        "state", villageState.name().toLowerCase(),
                        "level", String.valueOf(level));
            }
        }
    }

    public boolean isChief(Player player) {
        return player.getUniqueId().equals(chief);
    }

    public UUID getChief() {
        return chief;
    }

    public void setChief(UUID chief) {
        this.chief = chief;
    }

    /**
     * Might run database operation!
     */
    public List<Resident> getResidents() {
        return residents.stream()
                .map(id -> ResidentManager.getInstance().getResident(id)).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Resident> getOnlineResidents() {
        return residents.stream()
                .map(Bukkit::getPlayer).filter(Objects::nonNull)
                .map(p -> ResidentManager.getInstance().getResident(p))
                .collect(Collectors.toList());
    }

    public Set<UUID> getResidentIds() {
        return residents;
    }

    public boolean isFull() {
        return this.getResidentIds().size() >= ConfigManager.getInstance().getConfig().getInt("max-village-size");
    }

    public VillageState getState() {
        return villageState;
    }

    public void setState(VillageState state) {
        this.villageState = state;
    }

    public BuildState getBuildState() {
        return buildState;
    }

    public void setBuildState(BuildState buildState) {
        this.buildState = buildState;
    }

    public boolean isMobSpawning() {
        return mobSpawning;
    }

    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
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

    public boolean isBuff() {
        return buff;
    }

    public void setBuff(boolean buff) {
        this.buff = buff;
    }

    public Map<String, Integer> getVault() {
        return vault;
    }

    public IdObject getId() {
        return id;
    }

    public void setId(IdObject id) {
        this.id = id;
    }

    public int getMaxClaimableChunks() {
        return Utils.getLevelChunks(getState(), getLevel()) + bonusClaims;
    }

    public int getBonusClaims() {
        return bonusClaims;
    }

    public void setBonusClaims(int bonusClaims) {
        this.bonusClaims = bonusClaims;
    }

    public int getMaxOutposts() {
        if (getState() == VillageState.VILLAGE) {
            if (getLevel() <= 3) {
                return 1;
            }
            if (getLevel() <= 5) {
                return 2;
            }
        }
        if (getState() == VillageState.KINGDOM) {
            if (getLevel() <= 2) {
                return 4;
            }
            if (getLevel() <= 5) {
                return 6;
            }
            if (getLevel() <= 8) {
                return 8;
            }
            if (getLevel() <= 10) {
                return 12;
            }
        }
        return 4;
    }

    public boolean canLevelUp() {
        int currentLevel = getLevel();
        if (isMaxLevel()) {
            return false;
        }
        Map<String, Integer> nextLevelReqs = Levels.getLevelMapFromInt(currentLevel + 1, getState());
        if (nextLevelReqs == null) {
            return false;
        }
        Iterator<Map.Entry<String, Integer>> iterator = nextLevelReqs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> pair = iterator.next();
            if (!getVault().containsKey(pair.getKey())) {
                return false;
            }
            int amountRequired = nextLevelReqs.get(pair.getKey());
            int amountInVault = getVault().get(pair.getKey());
            if (amountInVault < amountRequired) {
                return false;
            }
            if (!iterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    public boolean isMaxLevel() {
        return (getLevel() == 10 && getState() == VillageState.KINGDOM) || Levels.getLevelMapFromInt(getLevel() + 1, getState()) == null;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", getName());
        map.put("chief", getChief());
        map.put("level", getLevel());
        map.put("settlementState", getState().name());
        map.put("access", getAccess().toString());
        map.put("buildState", getBuildState().name());
        map.put("residents", getResidentIds().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("pvp", isPvp());
        map.put("fireSpread", isFireSpread());
        map.put("explosions", isExplosions());
        map.put("mobSpawning", isMobSpawning());
        map.put("buff", isBuff());
        map.put("bank", getBank());
        map.put("bonusClaims", getBonusClaims());
        if (getVillageHome() != null)
            map.put("homeLoc", getVillageHome().serialize());
        map.put("outposts", getOutpostHomes().values().stream().collect(Collectors.toMap(NamedLocation::getName, NamedLocation::serialize)));
        map.put("banished", getBanishedResidents().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("uses", getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
        map.put("lastUpkeep", getLastUpkeep());
        map.put("vault", getVault().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return map;
    }
    
    public static Village deserialize(Map<String, Object> map) {
        if (map == null || map.get("name") == null || map.get("chief") == null || map.get("level") == null
                || map.get("settlementState") == null || map.get("enterMessage") == null || map.get("leaveMessage") == null
                || map.get("buildState") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Village! (" + map + ")");
            return null;
        }
        Village village = new Village(
                (String) map.get("name"),
                UUID.fromString((String) map.get("chief")),
                (Integer) map.get("level"),
                VillageState.valueOf((String) map.get("settlementState")),
                BuildState.valueOf((String) map.get("buildState"))
        );
        village.setId(IdObject.get(map));
        village.setAccess(VillageAccess.valueOf((String) map.get("access")));
        village.getResidentIds().addAll(((Collection<String>) map.get("residents")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        village.setPvp((Boolean) map.get("pvp"));
        village.setFireSpread((Boolean) map.get("fireSpread"));
        village.setExplosions((Boolean) map.get("explosions"));
        village.setMobSpawning((Boolean) map.get("mobSpawning"));
        village.setBuff((Boolean) map.get("buff"));
        village.setBank((Long) map.get("bank"));
        village.setBonusClaims((Integer) map.get("bonusClaims"));
        village.getAllies().addAll(((Collection<Object>) map.get("allies")).stream().map(IdObject::get).collect(Collectors.toSet()));
        if (map.get("homeLoc") != null) {
            village.setVillageHome(SimpleLocation.deserialize((Map<String, Object>) map.get("homeLoc")));
        } else if (map.get("homeLoc_x") != null && map.get("homeLoc_y") != null && map.get("homeLoc_Z") != null
                && map.get("homeLoc_world") != null && map.get("homeLoc_yaw") != null && map.get("homeLoc_pitch") != null){
            village.setVillageHome(new SimpleLocation(
                    (String) map.get("homeLoc_world"),
                    (double) map.get("homeLoc_x"),
                    (double) map.get("homeLoc_y"),
                    (double) map.get("homeLoc_z"),
                    (float) map.get("homeLoc_yaw"),
                    (float) map.get("homeLoc_pitch")
            ));
        }
        ((Map<String, Map<String, Object>>) map.get("outposts")).values().stream()
                .map(NamedLocation::deserialize)
                .filter(Objects::nonNull)
                .forEach(village::addOutpostHome);
        village.getBanishedResidents().addAll(((Collection<String>) map.get("banished")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        Map<String, Object> uses = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            if (uses.containsKey(u.name())) {
                village.setUseState(u, Utils.parseEnum((String) uses.get(u.name()), BuildState.RESIDENTS));
            }
        }
        village.setLastUpkeep((Long) map.get("lastUpkeep"));
        ((Map<String, Object>) map.get("vault")).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof Integer))
                .forEach(e -> village.getVault().put(e.getKey(), (Integer) e.getValue()));
        return village;
    }

    @Override
    public int compareTo(Village v) {
        if (v.getState() != getState())
            return getState().compareTo(v.getState());
        if (v.getLevel() != getLevel())
            return Integer.compare(v.getLevel(), getLevel());
        if (v.getResidentIds().size() != getResidentIds().size())
            return Integer.compare(v.getResidentIds().size(), getResidentIds().size());
        return Long.compare(v.getBank(), getBank());
    }
}

package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.bukkit.Location;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.UtilTime;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.crz.minecraft.kingdoms.landflags.Use;

import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;

public class Village implements ConfigurationSerializable {
    private IdObject id = IdObject.get(null);
    private String name;
    private String description;
    private int level;
    private UUID chief;
    private VillageState villageState;
    private String enterMessage;
    private String leaveMessage;
    private BuildState buildState;
    private boolean pvp = false;
    private boolean fireSpread = false;
    private boolean explosions = false;
    private boolean mobSpawning = false;
    private boolean buff = false;
    private SimpleLocation teleporterLoc = null;
    private boolean teleporter = false;
    private int bank = 0;
    private Set<IdObject> allies = new LinkedHashSet<>();
    private int teleportCost = 0;
    private SimpleLocation villageHomeLoc;
    private Map<String, NamedLocation> outpostHomes = new LinkedHashMap<>();
    private Set<UUID> residents = new LinkedHashSet<>();
    private Set<UUID> banished = new LinkedHashSet<>();
    private Map<Use, BuildState> use = new EnumMap<>(Use.class);
    private long lastUpkeep = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7L);
    private UpkeepStage upkeepStage = UpkeepStage.PAID;
    private Map<String, Integer> vault = new HashMap<>();

    public Village(String name, UUID chief, int level, VillageState state, String enterMessage, String leaveMessage, BuildState buildState) {
        super();
        this.resetTeleporter();
        this.name = name;
        residents.add(chief);
        this.chief = chief;
        this.level = level;
        this.villageState = state;
        this.enterMessage = enterMessage;
        this.leaveMessage = leaveMessage;
        this.buildState = buildState;
    }

    public void checkThenPay() {
        if (UtilTime.elapsedNano(lastUpkeep, TimeUnit.DAYS.toNanos(1L))) {
            this.payUpkeep();
        }
    }

    public void payUpkeep() {
        int total = LandManager.getInstance().getClaimedLandSize(this) * 500;
        if (!this.canPayUpkeep(total)) {
            this.setBank(0);
            if (upkeepStage == UpkeepStage.PAID) {
                this.upkeepStage = UpkeepStage.LAND;
                LandManager.getInstance().unclaimLand(this);
                MessageUtils.tellResidents(this.getOnlineResidents(), MessageUtils.Message.ALERT_UPKEEP_UNCLAIMED);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(this));
            } else if (upkeepStage == UpkeepStage.LAND) {
                MessageUtils.tellResidents(this.getOnlineResidents(), MessageUtils.Message.ALERT_UPKEEP_DELETED);
                VillageManager.getInstance().deleteVillage(this);
            }
        } else {
            this.setBank(this.getBank() - total);
            MessageUtils.tellResidents(this.getOnlineResidents(), MessageUtils.Message.ALERT_UPKEEP_CHARGED, "charged", String.valueOf(total));
            this.upkeepStage = UpkeepStage.PAID;
            this.lastUpkeep = System.currentTimeMillis();
            if (!this.canPayUpkeep(total)) {
                MessageUtils.tellResidents(this.getOnlineResidents(), MessageUtils.Message.ALERT_UPKEEP_CANT_PAY, "cost", String.valueOf(total));
            }
        }
    }

    public boolean canPayUpkeep(int total) {
        return this.getBank() - total >= 0;
    }

    public long getLastUpkeep() {
        return lastUpkeep;
    }

    public void setLastUpkeep(long lastUpkeep) {
        this.lastUpkeep = lastUpkeep;
    }

    public UpkeepStage getUpkeepStage() {
        return upkeepStage;
    }

    public void setUpkeepStage(UpkeepStage upkeepStage) {
        this.upkeepStage = upkeepStage;
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

    public int getTeleportCost() {
        return teleportCost;
    }

    public void setTeleporterCost(int cost) {
        this.teleportCost = cost;
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

    public SimpleLocation getTeleporter() {
        return teleporterLoc;
    }

    public void resetTeleporter() {
        teleporterLoc = null;
    }

    public OwnedLand getTeleporterClaim() {
        if (teleporterLoc == null) {
            return null;
        }
        return LandManager.getInstance().getLand(new SimpleChunkLocation(teleporterLoc));
    }

    public void setTeleporter(Location location) {
        setTeleporter(new SimpleLocation(location));
    }

    public void setTeleporter(SimpleLocation location) {
        teleporterLoc = location;
    }

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
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
        if (Data.first.getString(String.valueOf(villageState.name().toLowerCase()) + "-" + level).equals("")) {
            Data.first.put(String.valueOf(villageState.name().toLowerCase()) + "-" + level, this.getName());
            KingdomsPlugin.data.save(KingdomsPlugin.getInstance().getDataFolder());
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, "&6&l" + this.getName() + " &7&l is the first to reach &6&l" + villageState.name().toLowerCase() + " &7&llevel &6&l" + level);
            }
        }
    }

    public void levelUp() {
        this.setLevel(level + 1);
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
                .map(p -> ResidentManager.getInstance().getResident(p)).filter(Objects::nonNull)
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

    public String getEnterMessage() {
        return enterMessage;
    }

    public void setEnterMessage(String enterMessage) {
        this.enterMessage = enterMessage;
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(String leaveMessage) {
        this.leaveMessage = leaveMessage;
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

    public boolean isTeleporter() {
        return teleporter;
    }

    public void setTeleporter(boolean teleporter) {
        this.teleporter = teleporter;
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", getName());
        map.put("chief", getChief().toString());
        map.put("level", getLevel());
        map.put("settlementState", getState().name());
        map.put("enterMessage", getEnterMessage());
        map.put("leaveMessage", getLeaveMessage());
        map.put("buildState", getBuildState().name());
        map.put("residents", getResidentIds().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("pvp", isPvp());
        map.put("fireSpread", isFireSpread());
        map.put("explosions", isExplosions());
        map.put("mobSpawning", isMobSpawning());
        map.put("buff", isBuff());
        map.put("teleporter", isTeleporter());
        map.put("teleporterLoc", getTeleporter().serialize());
        map.put("bank", getBank());
        map.put("homeLoc", getVillageHome().serialize());
        map.put("outpostHomes", getOutpostHomes().values().stream().collect(Collectors.toMap(NamedLocation::getName, NamedLocation::serialize)));
        map.put("banished", getBanishedResidents().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("uses", getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
        map.put("lastUpkeep", getLastUpkeep());
        map.put("upkeepStage", getUpkeepStage().name());
        map.put("vault", getVault().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return map;
    }
    
    public static Village deserialize(Map<String, Object> map) {
        if (map == null || map.get("name") == null || map.get("chief") == null || map.get("level") == null
                || map.get("settlementState") == null || map.get("enterMessage") == null || map.get("leaveMessage") == null
                || map.get("buildState") == null) {
            return null;
        }
        Village village = new Village(
                (String) map.get("name"),
                UUID.fromString((String) map.get("chief")),
                (Integer) map.get("level"),
                VillageState.valueOf((String) map.get("settlementState")),
                (String) map.get("enterMessage"),
                (String) map.get("leaveMessage"),
                BuildState.valueOf((String) map.get("buildState"))
        );
        village.setId(IdObject.get(map));
        village.getResidentIds().addAll(((Collection<String>) map.get("residents")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        village.setPvp((Boolean) map.get("pvp"));
        village.setFireSpread((Boolean) map.get("fireSpread"));
        village.setExplosions((Boolean) map.get("explosions"));
        village.setMobSpawning((Boolean) map.get("mobSpawning"));
        village.setBuff((Boolean) map.get("buff"));
        village.setTeleporter((Boolean) map.get("teleporter"));
        if (map.get("teleporterLoc") != null) {
            village.setTeleporter(SimpleLocation.deserialize((Map<String, Object>) map.get("teleporterLoc")));
        } else if (map.get("teleporterLoc_x") != null && map.get("teleporterLoc_y") != null && map.get("teleporterLoc_Z") != null
                && map.get("teleporterLoc_world") != null && map.get("teleporterLoc_yaw") != null && map.get("teleporterLoc_pitch") != null){
            village.setTeleporter(new SimpleLocation(
                    (String) map.get("teleporterLoc_world"),
                    (double) map.get("teleporterLoc_x"),
                    (double) map.get("teleporterLoc_y"),
                    (double) map.get("teleporterLoc_z"),
                    (float) map.get("teleporterLoc_yaw"),
                    (float) map.get("teleporterLoc_pitch")
            ));
        }
        village.setBank((Integer) map.get("bank"));
        village.getAllies().addAll(((Collection<Object>) map.get("allies")).stream().map(IdObject::get).collect(Collectors.toSet()));
        village.setTeleporterCost((Integer) map.get("teleportCost"));
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
        ((Map<String, Map<String, Object>>) map.get("outpostHomes")).values().stream()
                .map(NamedLocation::deserialize)
                .filter(Objects::nonNull)
                .forEach(village::addOutpostHome);
        village.getBanishedResidents().addAll(((List<String>) map.get("banished")).stream().map(UUID::fromString).collect(Collectors.toSet()));
        Map<String, Object> use = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            if (use.containsKey(u.name())) {
                village.setUseState(u, Utils.parseEnum((String) use.get(u.name()), BuildState.RESIDENTS));
            }
        }
        village.setLastUpkeep((Long) map.get("lastUpkeep"));
        village.setUpkeepStage(Utils.parseEnum((String) map.get("upkeepStage"), UpkeepStage.PAID));
        ((Map<String, Object>) map.get("vault")).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof Integer))
                .forEach(e -> village.getVault().put(e.getKey(), (Integer) e.getValue()));
        return village;
    }
}

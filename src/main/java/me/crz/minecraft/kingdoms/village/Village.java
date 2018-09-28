package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bukkit.Bukkit;

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

import org.bson.Document;

import java.util.UUID;
import java.util.stream.Collectors;

public class Village {
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
    private Set<String> allies = new LinkedHashSet<>();
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
                MessageUtils.tellResidents(this.getResidents(), "&a&lALERT &7Your settlement land has been &cunclaimed&7 because ");
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(this));
            } else if (upkeepStage == UpkeepStage.LAND) {
                MessageUtils.tellResidents(this.getResidents(), "&a&lALERT &7Your settlement has been &cdeleted&7 because it was &cunable&7 to pay its upkeep!");
                VillageManager.getInstance().deleteVillage(this);
            }
        } else {
            this.setBank(this.getBank() - total);
            MessageUtils.tellResidents(this.getResidents(), "&a&lALERT &7Your settlement has been charged &6" + total + "&7 for upkeep!");
            this.upkeepStage = UpkeepStage.PAID;
            this.lastUpkeep = System.currentTimeMillis();
            if (!this.canPayUpkeep(total)) {
                MessageUtils.tellResidents(this.getResidents(), "&a&lALERT &7Your settlement is &cunable&7 to pay tommorrows upkeep, it will cost &6" + total + "&7!");
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

    public boolean check(Document doc, String key) {
        return doc.containsKey(key) && doc.get(key) != null;
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

    public Set<String> getAllies() {
        return allies;
    }

    public boolean isAlly(String name) {
        return this.getAllies().contains(name);
    }

    public boolean isAlly(Village village) {
        return this.isAlly(village.getName());
    }

    public void addAlly(String name) {
        if (!this.isAlly(name)) {
            allies.add(name);
        }
    }

    public void removeAlly(Village village) {
        if (this.isAlly(village.getName())) {
            allies.remove(village.getName());
        }
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
        if (Config.first.getString(String.valueOf(villageState.name().toLowerCase()) + "-" + level).equals("")) {
            Config.first.put(String.valueOf(villageState.name().toLowerCase()) + "-" + level, this.getName());
            KingdomsPlugin.config.save(KingdomsPlugin.getInstance().getDataFolder());
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
        return ConfigManager.getInstance().getConfig().getInt("max-village-size") - this.getResidents().size() <= 0;
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
}

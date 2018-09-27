package me.crz.minecraft.kingdoms.village;

import java.lang.reflect.Field;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.LinkedHashMap;
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

import java.util.ArrayList;
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
    private boolean pvp;
    private boolean fireSpread;
    private boolean explosions;
    private boolean mobSpawning;
    private boolean buff;
    private Document teleporterDoc;
    private SimpleLocation teleporterLoc;
    private boolean teleporter;
    private int bank;
    private List<String> allies;
    private int teleportCost;
    private Document villageHomeDoc;
    private SimpleLocation villageHomeLoc;
    private Document outpostHomes;
    private Set<UUID> residents;
    private Set<UUID> banished;
    private Document use;
    private long lastUpkeep;
    private UpkeepStage upkeepStage;
    private Document document;

    public Village(final String name, final UUID chief, final int level, final VillageState state, final String enterMessage, final String leaveMessage, final BuildState buildState) {
        super();
        document = new Document();
        this.resetTeleporter();
        this.set("name", name);
        residents = new HashSet<>();
        residents.add(chief);
        this.set("chief", chief);
        this.set("level", level);
        this.set("villageState", state);
        this.set("enterMessage", enterMessage);
        this.set("leaveMessage", leaveMessage);
        this.set("buildState", buildState);
        this.set("vault", new Document());
        this.set("pvp", false);
        this.set("fireSpread", false);
        this.set("explosions", false);
        this.set("mobSpawning", false);
        this.set("buff", false);
        this.set("teleporter", false);
        this.set("teleporterLoc", new Document());
        this.set("bank", 0);
        this.set("allies", new ArrayList());
        this.set("teleportCost", 0);
        this.set("villageHome", new Document());
        this.set("outpostHomes", new Document());
        this.set("banished", new ArrayList());
        final Document useDoc = new Document();
        for (Use u : Use.values()) {
            useDoc.put(u.name(), BuildState.RESIDENTS.name());
        }
        this.set("use", useDoc);
        this.set("lastUpkeep", System.nanoTime() + TimeUnit.DAYS.toNanos(7L));
        this.set("upkeepStage", UpkeepStage.PAID);
    }

    public Village(final Document doc) {
        super();
        document = doc;
        name = doc.getString("name");
        level = doc.getInteger("level");
        villageState = VillageState.valueOf(doc.getString("villageState"));
        chief = UUID.fromString(doc.getString("chief"));
        residents = doc.get("residents", new ArrayList<String>()).stream().map(UUID::fromString).collect(Collectors.toSet());
        enterMessage = doc.getString("enterMessage");
        leaveMessage = doc.getString("leaveMessage");
        buildState = BuildState.valueOf(doc.getString("buildState"));
        teleporterDoc = (Document) doc.get("teleporterLoc");
        pvp = doc.getBoolean("pvp");
        fireSpread = doc.getBoolean("fireSpread");
        explosions = doc.getBoolean("explosions");
        mobSpawning = doc.getBoolean("mobSpawning");
        buff = doc.getBoolean("buff");
        teleporter = doc.getBoolean("teleporter");
        teleporterLoc = SimpleLocation.load(teleporterDoc);
        bank = doc.getInteger("bank");
        allies = doc.get("allies", new ArrayList<>());
        teleportCost = doc.getInteger("teleportCost");
        villageHomeDoc = (Document) doc.get("villageHome");
        villageHomeLoc = SimpleLocation.load(villageHomeDoc);
        if (doc.containsKey("outpostHomes")) {
            outpostHomes = (Document) doc.get("outpostHomes");
        } else {
            this.set("outpostHomes", new Document());
        }
        if (doc.containsKey("banished")) {
            banished = doc.get("banished", new ArrayList<String>()).stream().map(UUID::fromString).collect(Collectors.toSet());
        } else {
            this.set("banished", new ArrayList());
        }
        if (doc.containsKey("use")) {
            use = (Document) doc.get("use");
        } else {
            final Document useDoc = new Document();
            for (Use u : Use.values()) {
                useDoc.put(u.name(), BuildState.RESIDENTS.name());
            }
            this.set("use", useDoc);
        }
        if (doc.containsKey("lastUpkeep")) {
            lastUpkeep = doc.getLong("lastUpkeep");
        } else {
            this.set("lastUpkeep", System.nanoTime() + TimeUnit.DAYS.toNanos(7L));
        }
        if (doc.containsKey("upkeepStage")) {
            upkeepStage = UpkeepStage.valueOf(doc.getString("upkeepStage"));
        } else {
            this.set("upkeepStage", UpkeepStage.PAID);
        }
    }

    public void checkThenPay() {
        if (UtilTime.elapsedNano(lastUpkeep, TimeUnit.DAYS.toNanos(1L))) {
            this.payUpkeep();
        }
    }

    public void payUpkeep() {
        final int total = LandManager.getInstance().getClaimedLandSize(this) * 500;
        if (!this.canPayUpkeep(total)) {
            this.setBank(0);
            if (upkeepStage == UpkeepStage.PAID) {
                this.set("upkeepStage", UpkeepStage.LAND);
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
            this.set("upkeepStage", UpkeepStage.PAID);
            this.set("lastUpkeep", System.nanoTime());
            if (!this.canPayUpkeep(total)) {
                MessageUtils.tellResidents(this.getResidents(), "&a&lALERT &7Your settlement is &cunable&7 to pay tommorrows upkeep, it will cost &6" + total + "&7!");
            }
        }
    }

    public boolean canPayUpkeep(final int total) {
        return this.getBank() - total >= 0;
    }

    public long getLastUpkeep() {
        return lastUpkeep;
    }

    public UpkeepStage getUpkeepStage() {
        return upkeepStage;
    }

    public BuildState getUseState(final Use u) {
        if (use.containsKey(u.name())) {
            return BuildState.valueOf(use.getString(u.name()));
        }
        return BuildState.RESIDENTS;
    }

    public void setUseState(final Use u, final BuildState state) {
        use.put(u.name(), state.name());
        this.set("use", use);
    }

    public Set<UUID> getBanishedResidents() {
        return banished;
    }

    public void addBanishedResident(final Resident res) {
        if (!this.isBanished(res.getUUID())) {
            banished.add(res.getUUID());
        }
    }

    public void removeBanishedResident(final Resident res) {
        if (this.isBanished(res.getUUID())) {
            banished.remove(res.getUUID());
        }
    }

    public boolean isBanished(final UUID uuid) {
        return banished.contains(uuid);
    }

    public void addOutpostHome(final Location loc, final OwnedLand land, final String name) {
        final Document doc = new Document();
        doc.put("name", name);
        doc.put("worldName", loc.getWorld().getName());
        doc.put("x", loc.getX());
        doc.put("y", loc.getY());
        doc.put("z", loc.getZ());
        doc.put("yaw", loc.getYaw());
        doc.put("piutch", loc.getPitch());
        outpostHomes.put(land.getX() + "-" + land.getWorldName() + "-" + land.getZ(), doc);
    }

    public Map<String, NamedLocation> getOutpostHomes() {
        final Map<String, NamedLocation> homes = new LinkedHashMap<>();
        try {
            for (final Map.Entry<String, Object> name : outpostHomes.entrySet()) {
                if (((Document) name.getValue()).containsKey("name")) {
                    homes.put(name.getKey(), NamedLocation.load((Document) name.getValue()));
                }
            }
        } catch (Exception ex) {
        }
        return homes;
    }

    public SimpleLocation getVillageHome() {
        return villageHomeLoc;
    }

    public boolean hasVillageHome() {
        return this.getVillageHome() != null;
    }

    public boolean check(final Document doc, final String key) {
        return doc.containsKey(key) && doc.get(key) != null;
    }

    public void setVillageHome(final Location loc) {
        villageHomeLoc = new SimpleLocation(loc);
        villageHomeDoc = SimpleLocation.save(villageHomeLoc, villageHomeDoc);
        this.set("villageHome", villageHomeDoc);
    }

    public int getTeleportCost() {
        return teleportCost;
    }

    public void setTeleporterCost(final int cost) {
        this.set("teleportCost", cost);
    }

    public List<String> getAllies() {
        return allies;
    }

    public boolean isAlly(final String name) {
        return this.getAllies().contains(name);
    }

    public boolean isAlly(final Village village) {
        return this.isAlly(village.getName());
    }

    public void addAlly(final String name) {
        if (!this.isAlly(name)) {
            allies.add(name);
            this.set("allies", allies);
        }
    }

    public void removeAlly(final Village village) {
        if (this.isAlly(village.getName())) {
            allies.remove(village.getName());
            this.set("allies", allies);
        }
    }

    public SimpleLocation getTeleporter() {
        return teleporterLoc;
    }

    public void resetTeleporter() {
        teleporterLoc = null;
        teleporterDoc = SimpleLocation.save(null, new Document());
        this.set("teleporter", teleporterDoc);
    }

    public OwnedLand getTeleporterClaim() {
        if (teleporterLoc == null) {
            return null;
        }
        return LandManager.getInstance().getLand(new SimpleChunkLocation(teleporterLoc));
    }

    public void setTeleporter(final Location location) {
        teleporterLoc = new SimpleLocation(location);
        teleporterDoc = SimpleLocation.save(teleporterLoc, teleporterDoc);
        this.set("teleporterLoc", teleporterDoc);
    }

    public int getBank() {
        return bank;
    }

    public void setBank(final int bank) {
        this.set("bank", bank);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.set("name", name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.set("description", description);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(final int level) {
        this.set("level", level);
        if (Config.first.getString(String.valueOf(villageState.name().toLowerCase()) + "-" + level).equals("")) {
            Config.first.put(String.valueOf(villageState.name().toLowerCase()) + "-" + level, this.getName());
            KingdomsPlugin.config.save(KingdomsPlugin.getInstance().getDataFolder());
            for (final Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, "&6&l" + this.getName() + " &7&l is the first to reach &6&l" + villageState.name().toLowerCase() + " &7&llevel &6&l" + level);
            }
        }
    }

    public void levelUp() {
        this.setLevel(level + 1);
    }

    public boolean isChief(final Player player) {
        return player.getUniqueId().equals(chief);
    }

    public UUID getChief() {
        return chief;
    }

    public void setChief(final UUID chief) {
        this.set("chief", chief);
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

    public void setState(final VillageState state) {
        this.set("villageState", state);
    }

    public String getEnterMessage() {
        return enterMessage;
    }

    public void setEnterMessage(final String enterMessage) {
        this.set("enterMessage", enterMessage);
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(final String leaveMessage) {
        this.set("leaveMessage", leaveMessage);
    }

    public BuildState getBuildState() {
        return buildState;
    }

    public void setBuildState(final BuildState buildState) {
        this.set("buildState", buildState);
    }

    public boolean isMobSpawning() {
        return mobSpawning;
    }

    public void setMobSpawning(final boolean mobSpawning) {
        this.set("mobSpawning", mobSpawning);
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(final boolean pvp) {
        this.set("pvp", pvp);
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public void setFireSpread(final boolean fireSpread) {
        this.set("fireSpread", fireSpread);
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(final boolean explosions) {
        this.set("explosions", explosions);
    }

    public boolean isBuff() {
        return buff;
    }

    public void setBuff(final boolean buff) {
        this.set("buff", buff);
    }

    public boolean isTeleporter() {
        return teleporter;
    }

    public void setTeleporter(final boolean teleporter) {
        this.set("teleporter", teleporter);
    }

    public Document getDocument() {
        return document;
    }

    public void set(final String key, final Object o) {
        Field[] declaredFields;
        for (int length = (declaredFields = this.getClass().getDeclaredFields()).length, i = 0; i < length; ++i) {
            final Field field = declaredFields[i];
            if (key.equalsIgnoreCase(field.getName())) {
                try {
                    if (o instanceof Enum) {
                        field.set(this, o);
                        document.put(key, ((Enum) o).name());
                    } else if (o instanceof UUID) {
                        field.set(this, o);
                        document.put(key, ((UUID) o).toString());
                    } else {
                        field.set(this, o);
                        document.put(key, o);
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

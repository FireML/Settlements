package me.crz.minecraft.kingdoms.village;

import java.lang.reflect.Field;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import java.util.Iterator;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import java.util.concurrent.TimeUnit;
import me.crz.minecraft.kingdoms.landflags.Use;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import java.util.UUID;

public class Village
{
    private String name;
    private String description;
    private int level;
    private UUID chief;
    private VillageState villageState;
    private String enterMessage;
    private String leaveMessage;
    private BuildState buildState;
    private Document teleporterClaim;
    private boolean pvp;
    private boolean fireSpread;
    private boolean explosions;
    private boolean mobSpawning;
    private boolean buff;
    private boolean teleporter;
    private int bank;
    private List<String> allies;
    private int teleportCost;
    private Document villageHome;
    private Document outpostHomes;
    private List<String> banished;
    private Document use;
    private long lastUpkeep;
    private UpkeepStage upkeepStage;
    private Document document;
    
    public Village(final String name, final UUID chief, final int level, final VillageState state, final String enterMessage, final String leaveMessage, final BuildState buildState) {
        super();
        document = new Document();
        this.resetTeleporterClaim();
        this.set("name", name);
        this.set("chief", chief);
        this.set("level", level);
        this.set("villageState", state);
        this.set("enterMessage", enterMessage);
        this.set("leaveMessage", leaveMessage);
        this.set("buildState", buildState);
        document.put("vault", (Object)new Document());
        this.set("pvp", false);
        this.set("fireSpread", false);
        this.set("explosions", false);
        this.set("mobSpawning", false);
        this.set("buff", false);
        this.set("teleporter", false);
        this.set("bank", 0);
        this.set("allies", new ArrayList());
        this.set("teleportCost", 0);
        this.set("villageHome", new Document());
        this.set("outpostHomes", new Document());
        this.set("banished", new ArrayList());
        final Document useDoc = new Document();
        Use[] values;
        for (int length = (values = Use.values()).length, i = 0; i < length; ++i) {
            final Use u = values[i];
            useDoc.put(u.name(), (Object)BuildState.RESIDENTS.name());
        }
        this.set("use", useDoc);
        this.set("lastUpkeep", System.nanoTime() + TimeUnit.DAYS.toNanos(7L));
        this.set("upkeepStage", UpkeepStage.PAID);
    }
    
    public Village(final Document doc) {
        super();
        document = doc;
        name = doc.getString((Object)"name");
        level = doc.getInteger((Object)"level");
        villageState = VillageState.valueOf(doc.getString((Object)"villageState"));
        chief = UUID.fromString(doc.getString((Object)"chief"));
        enterMessage = doc.getString((Object)"enterMessage");
        leaveMessage = doc.getString((Object)"leaveMessage");
        buildState = BuildState.valueOf(doc.getString((Object)"buildState"));
        teleporterClaim = (Document)doc.get((Object)"teleporterClaim");
        pvp = doc.getBoolean((Object)"pvp");
        fireSpread = doc.getBoolean((Object)"fireSpread");
        explosions = doc.getBoolean((Object)"explosions");
        mobSpawning = doc.getBoolean((Object)"mobSpawning");
        buff = doc.getBoolean((Object)"buff");
        teleporter = doc.getBoolean((Object)"teleporter");
        bank = doc.getInteger((Object)"bank");
        allies = (List<String>)doc.get((Object)"allies");
        teleportCost = doc.getInteger((Object)"teleportCost");
        villageHome = (Document)doc.get((Object)"villageHome");
        if (doc.containsKey((Object)"outpostHomes")) {
            outpostHomes = (Document)doc.get((Object)"outpostHomes");
        }
        else {
            this.set("outpostHomes", new Document());
        }
        if (doc.containsKey((Object)"banished")) {
            banished = (List<String>)doc.get((Object)"banished");
        }
        else {
            this.set("banished", new ArrayList());
        }
        if (doc.containsKey((Object)"use")) {
            use = (Document)doc.get((Object)"use");
        }
        else {
            final Document useDoc = new Document();
            Use[] values;
            for (int length = (values = Use.values()).length, i = 0; i < length; ++i) {
                final Use u = values[i];
                useDoc.put(u.name(), (Object)BuildState.RESIDENTS.name());
            }
            this.set("use", useDoc);
        }
        if (doc.containsKey((Object)"lastUpkeep")) {
            lastUpkeep = doc.getLong((Object)"lastUpkeep");
        }
        else {
            this.set("lastUpkeep", System.nanoTime() + TimeUnit.DAYS.toNanos(7L));
        }
        if (doc.containsKey((Object)"upkeepStage")) {
            upkeepStage = UpkeepStage.valueOf(doc.getString((Object)"upkeepStage"));
        }
        else {
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
            if (upkeepStage.equals(UpkeepStage.PAID)) {
                this.set("upkeepStage", UpkeepStage.LAND);
                for (final OwnedLand land : LandManager.getInstance().getClaimedLand(this)) {
                    LandManager.getInstance().removeLand(land);
                }
                MessageUtils.tellResidents(this.getResidents(), "&a&lALERT &7Your settlement land has been &cunclaimed&7 because ");
                VillageManager.getInstance().saveVillage(this);
                return;
            }
            if (upkeepStage.equals(UpkeepStage.LAND)) {
                MessageUtils.tellResidents(this.getResidents(), "&a&lALERT &7Your settlement has been &cdeleted&7 because it was &cunable&7 to pay its upkeep!");
                VillageManager.getInstance().deleteVillage(this);
            }
        }
        else {
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
        if (use.containsKey((Object)u.name())) {
            return BuildState.valueOf(use.getString((Object)u.name()));
        }
        return BuildState.RESIDENTS;
    }
    
    public void setUseState(final Use u, final BuildState state) {
        use.put(u.name(), (Object)state.name());
        this.set("use", use);
    }
    
    public List<String> getBanishedResidents() {
        try {
            return banished;
        }
        catch (Exception e) {
            return new ArrayList<String>();
        }
    }
    
    public void addBanishedResident(final Resident res) {
        if (!this.isBanished(res.getUUID())) {
            banished.add(res.getUUID().toString());
        }
    }
    
    public void removeBanishedResident(final Resident res) {
        if (this.isBanished(res.getUUID())) {
            banished.remove(res.getUUID().toString());
        }
    }
    
    public boolean isBanished(final UUID uuid) {
        return banished.contains(uuid.toString());
    }
    
    public void addOutpostHome(final Location loc, final OwnedLand land, final String name) {
        final Document doc = new Document();
        doc.put("name", (Object)name);
        doc.put("worldName", (Object)loc.getWorld().getName());
        doc.put("x", (Object)loc.getX());
        doc.put("y", (Object)loc.getY());
        doc.put("z", (Object)loc.getZ());
        outpostHomes.put(String.valueOf(land.getX()) + "-" + land.getWorldName() + "-" + land.getZ(), (Object)doc);
    }
    
    public Map<String, Location> getOutpostHomes() {
        final Map<String, Location> homes = new HashMap<String, Location>();
        try {
            for (final Map.Entry<String, Object> name : outpostHomes.entrySet()) {
                if (((Document)name.getValue()).containsKey((Object)"name")) {
                    homes.put(name.getKey(), new Location(Bukkit.getWorld(((Document)name.getValue()).getString((Object)"worldName")), (double)((Document)name.getValue()).getDouble((Object)"x"), (double)((Document)name.getValue()).getDouble((Object)"y"), (double)((Document)name.getValue()).getDouble((Object)"z")));
                }
            }
        }
        catch (Exception ex) {}
        return homes;
    }
    
    public Map<String, Document> getOutpostHomesList() {
        final Map<String, Document> homes = new HashMap<String, Document>();
        for (final Map.Entry<String, Object> name : outpostHomes.entrySet()) {
            if (name.getValue() instanceof Document && ((Document)name.getValue()).containsKey((Object)"name")) {
                homes.put(name.getKey(), name.getValue());
            }
        }
        return homes;
    }
    
    public Location getVillageHome() {
        try {
            return new Location(Bukkit.getWorld(villageHome.getString((Object)"worldName")), villageHome.getInteger((Object)"x"), villageHome.getInteger((Object)"y"), (double)villageHome.getInteger((Object)"z"));
        }
        catch (Exception e) {
            return null;
        }
    }
    
    public boolean hasVillageHome() {
        return this.getVillageHome() != null;
    }
    
    public boolean check(final Document doc, final String key) {
        return doc.containsKey((Object)key) && doc.get((Object)key) != null;
    }
    
    public void setVillageHome(final Location loc) {
        if (villageHome == null) {
            villageHome = new Document();
        }
        villageHome.put("worldName", (Object)loc.getWorld().getName());
        villageHome.put("x", (Object)loc.getBlockX());
        villageHome.put("y", (Object)loc.getBlockY());
        villageHome.put("z", (Object)loc.getBlockZ());
        this.set("villageHome", villageHome);
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
    
    public void resetTeleporterClaim() {
        (teleporterClaim = new Document()).put("x", (Object)null);
        teleporterClaim.put("z", (Object)null);
        teleporterClaim.put("z", (Object)null);
        teleporterClaim.put("worldName", (Object)null);
        this.set("teleporterClaim", teleporterClaim);
    }
    
    public OwnedLand getTeleporterClaim() {
        if (teleporterClaim.get((Object)"x") == null && teleporterClaim.get((Object)"z") == null && teleporterClaim.get((Object)"worldName") == null) {
            return null;
        }
        return LandManager.getInstance().getLand(teleporterClaim.getInteger((Object)"x"), teleporterClaim.getInteger((Object)"z"), teleporterClaim.getString((Object)"worldName"));
    }
    
    public void setTeleporterClaim(final Location location) {
        teleporterClaim.put("x", (Object)location.getBlockX());
        teleporterClaim.put("y", (Object)location.getBlockY());
        teleporterClaim.put("z", (Object)location.getBlockZ());
        teleporterClaim.put("worldName", (Object)location.getWorld().getName());
        this.set("teleporterClaim", teleporterClaim);
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
            Config.first.put(String.valueOf(villageState.name().toLowerCase()) + "-" + level, (Object)this.getName());
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
    
    public List<Resident> getResidents() {
        return ResidentManager.getInstance().getResidentsForVillage(this);
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
                        document.put(key, (Object)((Enum)o).name());
                    }
                    else if (o instanceof UUID) {
                        field.set(this, o);
                        document.put(key, (Object)((UUID)o).toString());
                    }
                    else {
                        field.set(this, o);
                        document.put(key, o);
                    }
                }
                catch (IllegalArgumentException | IllegalAccessException ex2) {
                    final Exception ex;
                    final Exception e = ex;
                    e.printStackTrace();
                }
            }
        }
    }
}

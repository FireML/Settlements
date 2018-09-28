package me.crz.minecraft.kingdoms.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.UpkeepStage;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MongoDatabaseManager extends DatabaseManager {
    private MongoDatabase database;
    private MongoClient mongo;
    private MongoCollection<Document> kingdomCollection;
    private MongoCollection<Document> residentCollection;
    private MongoCollection<Document> landCollection;
    private MongoCollection<Document> otherData;

    public void connect() {
        if (Config.host.isEmpty() && Config.port == 0) {
            throw new IllegalArgumentException("Mongo not connecting due to bad data.json file!");
        }
        mongo = new MongoClient(
                new ServerAddress(Config.host, Config.port),
                MongoCredential.createCredential(Config.databaseUser, Config.database, Config.databasePassword.toCharArray()),
                MongoClientOptions.builder().applicationName("Kingdoms").build()
        );
        database = mongo.getDatabase(Config.database);
        kingdomCollection = database.getCollection("Kingdoms");
        residentCollection = database.getCollection("resident");
        landCollection = database.getCollection("land");
        otherData = database.getCollection("otherData");
    }

    @Override
    public void saveVillage(Village village) {
        saveVillage(village, false);
    }

    public void saveVillage(Village village, boolean exists) {
        if (!exists && kingdomCollection.find(new Document("name", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE))).first() == null) {
            kingdomCollection.insertOne(createDocument(village));
        } else {
            kingdomCollection.updateOne(new Document("name", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE)), new Document("$set", createDocument(village)));
        }
    }

    @Override
    public void deleteVillage(Village village) {
        kingdomCollection.deleteOne(new Document("name", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE)));
    }

    @Override
    public void saveAllVillages(Collection<Village> villages) {
        for (Village v : villages) {
            saveVillage(v, false);
        }
    }

    @Override
    public void unclaimLand(OwnedLand... lands) {
        for (OwnedLand land : lands) {
            Document filter = new Document();
            filter.put("x", land.getX());
            filter.put("z", land.getZ());
            filter.put("worldName", land.getWorldName());
            landCollection.deleteMany(filter);
        }
    }

    @Override
    public void unclaimLand(Village village) {
        landCollection.deleteMany(new Document("ownerVillage", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE)));
    }

    @Override
    public void saveLand(OwnedLand... lands) {
        for (OwnedLand land : lands) {
            Document filter = new Document();
            filter.put("x", land.getX());
            filter.put("z", land.getZ());
            filter.put("worldName", land.getWorldName());
            Document first = landCollection.find(filter).first();
            if (first == null || first.isEmpty()) {
                landCollection.insertOne(createDocument(land));
            } else {
                landCollection.updateOne(filter, new Document("$set", createDocument(land)));
            }
        }
    }

    @Override
    public Collection<OwnedLand> getAllLand() {
        List<OwnedLand> lands = new ArrayList<>();
        for (Document doc : landCollection.find()) {
            lands.add(createOwnedLand(doc));
        }
        return lands;
    }

    @Override
    public Collection<Village> getAllVillages() {
        List<Village> villages = new ArrayList<>();
        for (Document doc : kingdomCollection.find()) {
            Village village = createVillage(doc);
            if (village != null) {
                villages.add(village);
            }
        }
        return villages;
    }

    @Override
    public Resident getResident(UUID uuid) {
        return createResident(residentCollection.find(new Document("uuid", uuid.toString())).first());
    }

    @Override
    public Resident getResident(String name) {
        return createResident(residentCollection.find(new Document("name", Pattern.compile(name, Pattern.CASE_INSENSITIVE))).first());
    }

    @Override
    public void saveResident(Resident... residents) {
        for (Resident resident : residents) {
            if (residentCollection.find(new Document("uuid", resident.getUUID().toString())).first().isEmpty()) {
                residentCollection.insertOne(createDocument(resident));
            } else {
                residentCollection.updateOne(new Document("uuid", resident.getUUID().toString()), new Document("$set", createDocument(resident)));
            }
        }
    }

    private Document createDocument(Resident resident) {
        return new Document()
                .append("uuid", resident.getUUID().toString())
                .append("name", resident.getName())
                .append("villageName", resident.getVillageName())
                .append("lastLogin", resident.getLastLogin())
                .append("channels", resident.getChannels().stream().map(ChatChannel::name).collect(Collectors.toList()))
                .append("activeChannel", resident.getActiveChannel().name())
                .append("swear", resident.getSwear())
                .append("ignore", resident.getIgnore().stream().map(UUID::toString).collect(Collectors.toList()))
                .append("friends", resident.getFriends().stream().map(UUID::toString).collect(Collectors.toList()));
    }

    private Resident createResident(Document document) {
        if (document.isEmpty()) {
            return null;
        }
        Resident resident = new Resident(UUID.fromString(document.getString("uuid")), document.getString("name"));
        resident.setVillageRank(VillageRank.valueOf(document.getString("rank")));
        resident.setVillageName(document.getString("villageName"));
        resident.setLastLogin(document.getLong("lastLogin"));
        resident.getChannels().clear();
        document.get("channels", new ArrayList<String>()).stream()
                .map(s -> Utils.parseEnum(ChatChannel.class, s.toUpperCase()))
                .filter(Objects::nonNull)
                .forEach(resident::addChannel);
        try {
            resident.setActiveChannel(ChatChannel.valueOf(document.get("activeChannel", "GENERAL").toUpperCase()));
        } catch (Exception e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Invalid active channel in databaser for resident " + resident.getName() + "/" + resident.getUUID() + ": " + document.getString("activeChannel"));
        }
        if (document.containsKey("swear")) {
            resident.setSwear(document.getBoolean("swear"));
        }
        if (document.containsKey("ignore")) {
            document.get("ignore", new ArrayList<String>()).stream().map(Utils::parseUuid).filter(Objects::nonNull).forEach(resident::addIgnore);
        }
        if (document.containsKey("friends")) {
            document.get("friends", new ArrayList<String>()).stream().map(Utils::parseUuid).filter(Objects::nonNull).forEach(resident::addFriend);
        }
        return resident;
    }

    private Document createDocument(Village village) {
        return new Document()
                .append("name", village.getName())
                .append("chief", village.getChief().toString())
                .append("level", village.getLevel())
                .append("villageState", village.getState().name())
                .append("enterMessage", village.getEnterMessage())
                .append("leaveMessage", village.getLeaveMessage())
                .append("buildState", village.getBuildState().name())
                .append("residents", village.getResidentIds().stream().map(UUID::toString).collect(Collectors.toList()))
                .append("pvp", village.isPvp())
                .append("fireSpread", village.isFireSpread())
                .append("explosions", village.isExplosions())
                .append("mobSpawning", village.isMobSpawning())
                .append("buff", village.isBuff())
                .append("teleporter", village.isTeleporter())
                .append("teleporterLoc", createDocument(village.getTeleporter()))
                .append("bank", village.getBank())
                .append("villageHome", createDocument(village.getVillageHome()))
                .append("outpostHomes", new Document(village.getOutpostHomes().values().stream()
                        .collect(Collectors.toMap(NamedLocation::getName, this::createDocument))))
                .append("banished", village.getBanishedResidents().stream().map(UUID::toString).collect(Collectors.toList()))
                .append("use", new Document(Arrays.stream(Use.values())
                        .collect(Collectors.toMap(Use::name, u -> village.getUseState(u).name()))))
                .append("lastUpkeep", village.getLastUpkeep())
                .append("upkeepStage", village.getUpkeepStage().name())
                .append("vault", new Document(village.getVault().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

    private Village createVillage(Document document) {
        if (document.isEmpty()) {
            return null;
        }
        Village village = new Village(
                document.getString("name"),
                UUID.fromString(document.getString("chief")),
                document.getInteger("level"),
                VillageState.valueOf(document.getString("villageState")),
                document.getString("enterMessage"),
                document.getString("leaveMessage"),
                BuildState.valueOf(document.getString("buildState"))
        );
        village.getResidentIds().addAll(document.get("residents", new ArrayList<String>()).stream().map(UUID::fromString).collect(Collectors.toSet()));
        village.setPvp(document.getBoolean("pvp"));
        village.setFireSpread(document.getBoolean("fireSpread"));
        village.setExplosions(document.getBoolean("explosions"));
        village.setMobSpawning(document.getBoolean("mobSpawning"));
        village.setBuff(document.getBoolean("buff"));
        village.setTeleporter(document.getBoolean("teleporter"));
        village.setTeleporter(createLocation((Document) document.get("teleporterLoc")));
        village.setBank(document.getInteger("bank"));
        village.getAllies().addAll(document.get("allies", new ArrayList<>()));
        village.setTeleporterCost(document.getInteger("teleportCost"));
        village.setVillageHome(createLocation((Document) document.get("villageHome")));
        ((Document) document.get("outpostHomes")).values().stream()
                .map(d -> createNamedLocation((Document) d))
                .filter(Objects::nonNull)
                .forEach(village::addOutpostHome);
        village.getBanishedResidents().addAll(document.get("banished", new ArrayList<String>()).stream().map(UUID::fromString).collect(Collectors.toSet()));
        Document use = (Document) document.get("use");
        for (Use u : Use.values()) {
            if (use.containsKey(u.name())) {
                village.setUseState(u, Utils.parseEnum(use.getString(u.name()), BuildState.RESIDENTS));
            }
        }
        village.setLastUpkeep(document.getLong("lastUpkeep"));
        village.setUpkeepStage(Utils.parseEnum(document.getString("upkeepStage"), UpkeepStage.PAID));
        ((Document) document.get("vault")).entrySet().stream()
                .filter(e -> !(e.getValue() instanceof Integer))
                .forEach(e -> village.getVault().put(e.getKey(), (Integer) e.getValue()));
        return village;
    }

    private Document createDocument(SimpleLocation location) {
        return new Document()
                .append("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("yaw", location.getYaw())
                .append("pitch", location.getPitch())
                .append("worldName", location.getWorld());
    }

    private SimpleLocation createLocation(Document document) {
        if (document != null
                && !document.isEmpty()
                && document.get("x") != null && document.get("y") != null  && document.get("y") != null
                && document.get("yaw") != null && document.get("pitch") != null && document.get("worldName") != null) {
            return new SimpleLocation(
                    document.getString("worldName"),
                    document.getDouble("x"),
                    document.getDouble("y"),
                    document.getDouble("z"),
                    (float) document.get("yaw"),
                    (float) document.get("pitch")
            );
        }
        return null;
    }

    private Document createDocument(NamedLocation location) {
        return createDocument((SimpleLocation) location).append("name", location.getName());
    }

    private NamedLocation createNamedLocation(Document document) {
        if (document.containsKey("name")) {
            SimpleLocation location = createLocation(document);
            return new NamedLocation(document.getString("name"), location);
        }
        return null;
    }

    private Document createDocument(OwnedLand land) {
        return new Document()
                .append("ownerVillage", land.getOwnerVillage())
                .append("x", land.getX())
                .append("z", land.getZ())
                .append("worldName", land.getWorldName())
                .append("plotOwner", land.getPlotOwner() != null ? land.getPlotOwner().toString() : null)
                .append("price", land.getPrice())
                .append("forSale", land.isForSale())
                .append("pvp", land.isPvp())
                .append("mobSpawning", land.isMobSpawning())
                .append("fireSpread", land.isFireSpread())
                .append("explosions", land.isExplosions())
                .append("build", land.getBuild())
                .append("isOutpost", land.getIsOutpost())
                .append("buildState", land.getBuildState().name())
                .append("use", new Document(Arrays.stream(Use.values())
                        .collect(Collectors.toMap(Use::name, u -> land.getUseState(u).name()))));
    }

    private OwnedLand createOwnedLand(Document document) {
        OwnedLand land = new OwnedLand(
                document.getString("ownerVillage"),
                new SimpleChunkLocation(document.getString("worldName"), document.getInteger("x"), document.getInteger("z"))
        );
        String plotOwnerStr = document.getString("plotOwner");
        if (plotOwnerStr != null && !plotOwnerStr.isEmpty()) {
            land.setPlotOwner(UUID.fromString(plotOwnerStr));
        }
        land.setPrice(document.getInteger("price"));
        land.setForSale(document.getBoolean("forSale"));
        land.setMobSpawning(document.getBoolean("mobSpawning"));
        land.setPvp(document.getBoolean("pvp"));
        land.setFireSpread(document.getBoolean("fireSpread"));
        land.setExplosions(document.getBoolean("explosions"));
        land.setBuild(document.getBoolean("build"));
        land.setIsOutpost(document.getBoolean("isOutpost"));
        land.setBuildState(Utils.parseEnum(document.getString("buildState"), PlotBuildState.RESIDENTS));
        Document use = (Document) document.get("use");
        for (Use u : Use.values()) {
            if (use.containsKey(u.name())) {
                land.setUseState(u, Utils.parseEnum(use.getString(u.name()), PlotBuildState.RESIDENTS));
            }
        }
        return land;
    }
}

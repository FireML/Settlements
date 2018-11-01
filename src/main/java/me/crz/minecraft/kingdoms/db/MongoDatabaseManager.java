package me.crz.minecraft.kingdoms.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MongoDatabaseManager extends DatabaseManager {
    private MongoDatabase database;
    private MongoClient mongo;
    private MongoCollection<Document> empiresCollection;
    private MongoCollection<Document> settlementsCollection;
    private MongoCollection<Document> residentCollection;
    private MongoCollection<Document> landCollection;
    private MongoCollection<Document> logsCollection;
    private MongoCollection<Document> otherData;

    @Override
    public void connect() throws Exception {
        ConfigurationSection config = KingdomsPlugin.getInstance().getConfig().getConfigurationSection("storage");
        if (config.getString("host", "").isEmpty() && config.getInt("port", 0) == 0) {
            throw new IllegalArgumentException("Mongo not connecting due to bad storage data!");
        }
        mongo = new MongoClient(
                new ServerAddress(config.getString("host"), config.getInt("port")),
                MongoCredential.createCredential(config.getString("user"), config.getString("database"), config.getString("password").toCharArray()),
                MongoClientOptions.builder().applicationName("Kingdoms").build()
        );
        database = mongo.getDatabase(config.getString("database"));
        String tablePrefix = config.getString("table-prefix");
        empiresCollection = database.getCollection(tablePrefix + "empires");
        settlementsCollection = database.getCollection(tablePrefix + "settlements");
        residentCollection = database.getCollection(tablePrefix + "residents");
        landCollection = database.getCollection(tablePrefix + "lands");
        logsCollection = database.getCollection(tablePrefix + "logs");
        otherData = database.getCollection(tablePrefix + "otherData");
    }

    @Override
    public void saveEmpire(Empire empire) {
        Utils.catchSync();
        if (empire.getId().isNull() || empiresCollection.find(new Document("_id", empire.getId().value())).first() == null) {
            empiresCollection.insertOne(createDocument(empire));
            Document document = empiresCollection.find(new Document("name", Pattern.compile(empire.getName(), Pattern.CASE_INSENSITIVE))).first();
            empire.setId(IdObject.get(document));
        } else {
            empiresCollection.updateOne(new Document("_id", empire.getId().value()), new Document("$set", createDocument(empire)));
        }
    }

    @Override
    public void deleteEmpire(Empire village) {
        Utils.catchSync();
        empiresCollection.deleteOne(new Document("name", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE)));
    }

    @Override
    public void saveVillage(Village village) {
        Utils.catchSync();
        if (village.getId().isNull() || settlementsCollection.find(new Document("_id", village.getId().value())).first() == null) {
            settlementsCollection.insertOne(createDocument(village));
            Document document = settlementsCollection.find(new Document("name", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE))).first();
            village.setId(IdObject.get(document));
        } else {
            settlementsCollection.updateOne(new Document("_id", village.getId().value()), new Document("$set", createDocument(village)));
        }
    }

    @Override
    public void deleteVillage(Village village) {
        Utils.catchSync();
        settlementsCollection.deleteOne(new Document("name", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE)));
    }

    @Override
    public void saveVillages(Collection<Village> villages) {
        for (Village v : villages) {
            saveVillage(v);
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
        Utils.catchSync();
        landCollection.deleteMany(new Document("ownerVillage", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE)));
    }

    @Override
    public void saveLand(OwnedLand... lands) {
        Utils.catchSync();
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
        Utils.catchSync();
        List<OwnedLand> lands = new ArrayList<>();
        for (Document doc : landCollection.find()) {
            lands.add(createOwnedLand(doc));
        }
        return lands;
    }

    @Override
    public Collection<Empire> getAllEmpires() {
        Utils.catchSync();
        List<Empire> empires = new ArrayList<>();
        for (Document doc : empiresCollection.find()) {
            Empire empire = createEmpire(doc);
            if (empire != null) {
                empires.add(empire);
            }
        }
        return empires;
    }

    @Override
    public Collection<Village> getAllVillages() {
        Utils.catchSync();
        List<Village> villages = new ArrayList<>();
        for (Document doc : settlementsCollection.find()) {
            Village village = createVillage(doc);
            if (village != null) {
                villages.add(village);
            }
        }
        return villages;
    }

    @Override
    public Resident getResident(UUID uuid) {
        Utils.catchSync();
        return createResident(residentCollection.find(new Document("uuid", uuid.toString())).first());
    }

    @Override
    public Resident getResident(String name) {
        Utils.catchSync();
        return createResident(residentCollection.find(new Document("name", Pattern.compile(name, Pattern.CASE_INSENSITIVE))).first());
    }

    @Override
    public void saveResident(Resident... residents) {
        Utils.catchSync();
        for (Resident resident : residents) {
            if (resident.getId() == null || residentCollection.find(new Document("uuid", resident.getUUID().toString())).first() == null) {
                residentCollection.insertOne(createDocument(resident));
                Document document = settlementsCollection.find(new Document("uuid", resident.getUUID().toString())).first();
                if (document.containsKey("_id")) {
                    resident.setId(document.get("_id"));
                }
            } else {
                residentCollection.updateOne(new Document("_id", resident.getId()), new Document("$set", createDocument(resident)));
            }
        }
    }

    @Override
    public void log(Empire empire, Village village, Resident resident, String action, Map<String, Long> values) {
        if (village.getId().isNull()) {
            return;
        }
        List<Document> insert = new ArrayList<>();
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (resident != null) {
                map.put("name", resident.getName());
                map.put("uuid", resident.getUUID());
                map.put("rank", resident.getVillageRank().name());
                map.put("title", resident.getTitle());
            }
            if (empire != null) {
                map.put("empire", empire.getId().value());
            }
            map.put("village", village.getId().value());
            map.put("action", action);
            map.put("type", entry.getKey().toLowerCase());
            map.put("amount", entry.getValue());
            map.put("timestamp", System.currentTimeMillis());
            insert.add(new Document(map));
        }
        logsCollection.insertMany(insert);
    }


    @Override
    public int getLogPageCount(Village village, Resident resident, LogType type) {
        return getLogPageCount("settlementId", village.getId(), resident, type);
    }

    @Override
    public int getLogPageCount(Empire empire, Resident resident, LogType type) {
        return getLogPageCount("empireId", empire.getId(), resident, type);
    }

    private int getLogPageCount(String column, IdObject id, Resident resident, LogType type) {
        if (id.isNull()) {
            return 0;
        }
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq(column, id.value()));
        if (!resident.getVillageRank().atLeast(VillageRank.CHIEF)) {
            filters.add(Filters.eq("action", "deposit"));
        }
        switch (type) {
            case BANK:
                filters.add(Filters.eq("type", "cash"));
                break;
            case VAULT:
                filters.add(Filters.not(Filters.eq("type", "cash")));
                filters.add(Filters.not(Filters.eq("type", "claim")));
                break;

        }
        return (int) Math.ceil(logsCollection.countDocuments(Filters.and(filters)) / (double) Utils.getEntriesPerPage());
    }

    @Override
    public List<String[]> getLog(Village village, Resident resident, int page, LogType type) {
        return getLog("village", village.getId(), resident, page, type);
    }

    @Override
    public List<String[]> getLog(Empire empire, Resident resident, int page, LogType type) {
        return getLog("empire", empire.getId(), resident, page, type);
    }

    private List<String[]> getLog(String column, IdObject id, Resident resident, int page, LogType type) {
        List<String[]> log = new ArrayList<>();
        if (id.isNull()) {
            return log;
        }
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq(column, id.value()));
        if (!resident.getVillageRank().atLeast(VillageRank.CHIEF)) {
            filters.add(Filters.eq("action", "deposit"));
        }
        switch (type) {
            case BANK:
                filters.add(Filters.eq("type", "cash"));
                break;
            case VAULT:
                filters.add(Filters.not(Filters.eq("type", "cash")));
                filters.add(Filters.not(Filters.eq("type", "claim")));
                break;

        }
        FindIterable<Document> logIt = logsCollection.find(Filters.and(filters))
                .sort(Sorts.descending("timestamp"))
                .skip((page-1) * 20)
                .limit(20);
        for (Document document : logIt) {
            Village village = VillageManager.getInstance().getVillageFromId(IdObject.get(document.get("village")));
            VillageRank rank = VillageRank.RESIDENT;
            try {
                rank = VillageRank.valueOf(document.getString("rank").toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            log.add(new String[]{
                    "name", document.getString("name"),
                    "uuid", document.getString("uuid"),
                    "rank", rank.getTag(village),
                    "title", document.getString("title"),
                    "action", document.getString("action"),
                    "type", document.getString("type"),
                    "amount", String.valueOf(document.getInteger("amount")),
                    "timestamp", Utils.TIME_FORMAT.format(new Date(document.getLong("timestamp")))
            });
        }
        return log;
    }

    // ------ Utility methods ----- //

    private Document createDocument(ConfigurationSerializable serializable) {
        return new Document(serializable.serialize());
    }

    private Resident createResident(Document document) {
        if (document == null || document.isEmpty()) {
            return null;
        }
        return Resident.deserialize(document.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private Empire createEmpire(Document document) {
        if (document == null || document.isEmpty()) {
            return null;
        }
        return Empire.deserialize(document.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private Village createVillage(Document document) {
        if (document == null || document.isEmpty()) {
            return null;
        }
        return Village.deserialize(document.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private Document createDocument(OwnedLand land) {
        return new Document(land.serialize());
    }

    private OwnedLand createOwnedLand(Document document) {
        if (document == null || document.isEmpty()) {
            return null;
        }
        return OwnedLand.deserialize(document.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}

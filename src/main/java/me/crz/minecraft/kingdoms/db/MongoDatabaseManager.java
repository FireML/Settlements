package me.crz.minecraft.kingdoms.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

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
            kingdomCollection.insertOne(village.getDocument());
        } else {
            kingdomCollection.updateOne(new Document("name", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE)), new Document("$set", village.getDocument()));
        }
    }

    @Override
    public void deleteVillage(Village village) {
        kingdomCollection.deleteOne(new Document("name", Pattern.compile(village.getName(), Pattern.CASE_INSENSITIVE)));
    }

    @Override
    public void saveAllVillages(Collection<Village> villages) {
        for (final Village v : villages) {
            saveVillage(v, false);
        }
    }

    @Override
    public void unclaimLand(OwnedLand... lands) {
        for (OwnedLand land : lands) {
            final Document filter = new Document();
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
            final Document filter = new Document();
            filter.put("x", land.getX());
            filter.put("z", land.getZ());
            filter.put("worldName", land.getWorldName());
            if (landCollection.find(filter).first() == null) {
                landCollection.insertOne(land.getDocument());
            } else {
                landCollection.updateOne(new Document("_id", land.getDocument().get("_id")), new Document("$set", land.getDocument()));
            }
        }
    }

    @Override
    public Collection<OwnedLand> getAllLand() {
        List<OwnedLand> lands = new ArrayList<>();
        for (Document doc : landCollection.find()) {
            lands.add(new OwnedLand(doc));
        }
        return lands;
    }

    @Override
    public Collection<Village> getAllVillages() {
        List<Village> villages = new ArrayList<>();
        for (Document doc : kingdomCollection.find()) {
            villages.add(new Village(doc));
        }
        return villages;
    }

    @Override
    public Resident getResident(UUID uuid) {
        Document doc = residentCollection.find(new Document("uuid", uuid.toString())).first();
        if (doc.isEmpty()) {
            return null;
        }
        return new Resident(doc);
    }

    @Override
    public Resident getResident(String name) {
        Document doc = residentCollection.find(new Document("name", Pattern.compile(name, Pattern.CASE_INSENSITIVE))).first();
        if (doc.isEmpty()) {
            return null;
        }
        return new Resident(doc);
    }

    @Override
    public void saveResident(Resident... residents) {
        for (Resident resident : residents) {
            if (residentCollection.find(new Document("uuid", resident.getUUID().toString())).first().isEmpty()) {
                residentCollection.insertOne(resident.getDocument());
            } else {
                residentCollection.updateOne(new Document("uuid", resident.getUUID().toString()), new Document("$set", resident.getDocument()));
            }
        }
    }
}

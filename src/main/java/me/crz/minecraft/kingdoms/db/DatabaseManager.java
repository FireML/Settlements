package me.crz.minecraft.kingdoms.db;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class DatabaseManager {
    private static MongoDatabase database;
    private static MongoClient mongo;
    private static MongoCollection<Document> kingdomCollection;
    private static MongoCollection<Document> residentCollection;
    private static MongoCollection<Document> landCollection;
    private static MongoCollection<Document> otherData;

    public DatabaseManager() {
        super();
    }

    public static boolean connect() {
        if (Config.host.isEmpty() && Config.port == 0) {
            System.out.println("Mongo not connecting due to bad data.json file!");
            return false;
        }
        try {
            mongo = new MongoClient(
                    new ServerAddress(Config.host, Config.port),
                    MongoCredential.createCredential(Config.databaseUser, Config.database, Config.databasePassword.toCharArray()),
                    MongoClientOptions.builder().applicationName("Kingdoms").build()
            );
        } catch (Exception e) {
            return false;
        }
        database = mongo.getDatabase(Config.database);
        kingdomCollection = database.getCollection("Kingdoms");
        residentCollection = database.getCollection("resident");
        landCollection = database.getCollection("land");
        otherData = database.getCollection("otherData");
        return true;
    }

    public static MongoCollection<Document> getKingdomCollection() {
        return kingdomCollection;
    }

    public static MongoCollection<Document> getResidentCollection() {
        return residentCollection;
    }

    public static MongoCollection<Document> getLandCollection() {
        return landCollection;
    }

    public static MongoCollection<Document> getOtherDataCollection() {
        return otherData;
    }
}

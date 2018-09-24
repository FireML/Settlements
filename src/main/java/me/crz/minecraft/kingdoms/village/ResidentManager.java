package me.crz.minecraft.kingdoms.village;

import java.util.ArrayList;
import java.util.List;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import org.bukkit.entity.Player;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import me.crz.minecraft.kingdoms.db.DatabaseManager;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

public class ResidentManager {
    private static ResidentManager instance;
    private Map<UUID, Resident> residents;

    public ResidentManager() {
        super();
        residents = new HashMap<>();
    }

    public void saveResident(final Resident res) {
        residents.put(res.getUUID(), res);
        final MongoCollection<Document> collection = DatabaseManager.getResidentCollection();
        if (collection.find(new Document("uuid", res.getUUID().toString())).first() == null) {
            collection.insertOne(res.getDocument());
        } else {
            collection.updateOne(new Document("_id", res.getDocument().get("_id")), new Document("$set", res.getDocument()));
        }
    }

    public boolean isListening(final Player p, final ChatChannel channel) {
        return this.getResident(p).getChannels().contains(channel.name());
    }

    public Resident getResident(final Player p) {
        return this.getResident(p.getUniqueId());
    }

    public Resident getResident(final UUID uuid) {
        if (residents.containsKey(uuid)) {
            return residents.get(uuid);
        }
        final Document doc = DatabaseManager.getResidentCollection().find(new Document("uuid", uuid.toString())).first();
        final Resident res = new Resident(doc, uuid);
        residents.put(uuid, res);
        return res;
    }

    public List<Resident> getResidentsForVillage(final Village village) {
        if (village == null) {
            return new ArrayList<>();
        }
        final List<Resident> residents = new ArrayList<>();
        for (final Document doc : DatabaseManager.getResidentCollection().find(new Document("villageName", village.getName()))) {
            residents.add(new Resident(doc, null));
        }
        return residents;
    }

    public static ResidentManager getInstance() {
        if (instance == null) {
            instance = new ResidentManager();
        }
        return instance;
    }

    public boolean doesResidentHaveVillage(final Player player) {
        return !this.getResident(player).getVillageName().equals("");
    }
}

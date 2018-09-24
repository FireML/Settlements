package me.crz.minecraft.kingdoms.village;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import org.bukkit.entity.Player;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import org.bson.Document;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

public class ResidentManager
{
    private static ResidentManager instance;
    private Map<UUID, Resident> residents;
    
    public ResidentManager() {
        super();
        residents = new HashMap<UUID, Resident>();
    }
    
    public void saveResident(final Resident res) {
        residents.put(res.getUUID(), res);
        final MongoCollection<Document> collection = DatabaseManager.getResidentCollection();
        if (collection.find((Bson)new Document("uuid", (Object)res.getUUID().toString())).first() == null) {
            collection.insertOne((Object)res.getDocument());
        }
        else {
            collection.updateOne((Bson)new Document("_id", res.getDocument().get((Object)"_id")), (Bson)new Document("$set", (Object)res.getDocument()));
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
        final Document doc = (Document)DatabaseManager.getResidentCollection().find((Bson)new Document("uuid", (Object)uuid.toString())).first();
        final Resident res = new Resident(doc, uuid);
        residents.put(uuid, res);
        return res;
    }
    
    public List<Resident> getResidentsForVillage(final Village village) {
        if (village == null) {
            return new ArrayList<Resident>();
        }
        final List<Resident> residents = new ArrayList<Resident>();
        for (final Document doc : DatabaseManager.getResidentCollection().find((Bson)new Document("villageName", (Object)village.getName()))) {
            residents.add(new Resident(doc, (UUID)null));
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

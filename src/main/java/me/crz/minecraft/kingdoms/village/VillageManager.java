package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.banish.CustomRunnable;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.entity.Player;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import org.bson.Document;
import com.mongodb.client.MongoCollection;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.List;

public class VillageManager {
    private static VillageManager instance;
    private List<Village> villages;

    public static void main(final String[] args) {
        System.out.println(Pattern.compile(".*TeSt.*", 2));
    }

    public VillageManager() {
        super();
        villages = new ArrayList<>();
    }

    public static VillageManager getInstance() {
        if (instance == null) {
            instance = new VillageManager();
        }
        return instance;
    }

    public MongoCollection<Document> getAllVillageDocuments() {
        return DatabaseManager.getKingdomCollection();
    }

    public void saveVillage(final Village village) {
        this.saveVillage(village, false);
    }

    public void saveVillage(final Village village, final boolean b) {
        final MongoCollection<Document> collection = DatabaseManager.getKingdomCollection();
        if (collection.find(new Document("name", Pattern.compile(".*" + village.getName() + ".*", 2))).first() == null && !b) {
            collection.insertOne(village.getDocument());
        } else {
            collection.updateOne(new Document("_id", village.getDocument().get("_id")), new Document("$set", village.getDocument()));
        }
    }

    public void registerNewVillage(final Village village) {
        villages.add(village);
        this.saveVillage(village);
    }

    public Village getVillageFromName(final String name) {
        if (name.equals("")) {
            return null;
        }
        for (final Village village : villages) {
            if (village.getName().equalsIgnoreCase(name)) {
                return village;
            }
        }
        final Document doc = DatabaseManager.getKingdomCollection().find(new Document("name", Pattern.compile(name, 2))).first();
        if (doc == null) {
            return null;
        }
        final Village village2 = new Village(doc);
        if (!village2.getName().equalsIgnoreCase(name)) {
            return null;
        }
        villages.add(village2);
        return village2;
    }

    public Village getVillageFromResident(final Resident res) {
        return this.getVillageFromName(res.getVillageName());
    }

    public Village getVillageFromPlayer(final Player player) {
        return this.getVillageFromResident(ResidentManager.getInstance().getResident(player));
    }

    public void deleteVillage(final Village village) {
        villages.remove(village);
        DatabaseManager.getKingdomCollection().deleteOne(village.getDocument());
        for (final String ally : village.getAllies()) {
            final Village al = this.getVillageFromName(ally);
            if (al != null) {
                al.removeAlly(village);
            }
            this.saveVillage(al);
        }
        for (final Document doc : DatabaseManager.getLandCollection().find(new Document("ownerVillage", Pattern.compile(".*" + village.getName() + ".*", 2)))) {
            final OwnedLand land = new OwnedLand(doc);
            LandManager.getInstance().unclaimLand(land);
        }
    }

    public void isResBanished(final Location to, final Player p, final CustomRunnable customRunnable) {
        final OwnedLand land = LandManager.getInstance().getLandFromLocation(to);
        if (land != null) {
            final Village village = this.getVillageFromName(land.getOwnerVillage());
            if (village != null && village.isBanished(p.getUniqueId())) {
                customRunnable.run(land, p, village);
            }
        }
    }

    public void saveAll() {
        for (final Village v : villages) {
            this.saveVillage(v);
        }
    }
}

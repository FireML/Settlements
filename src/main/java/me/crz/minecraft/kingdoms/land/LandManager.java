package me.crz.minecraft.kingdoms.land;

import java.util.ArrayList;
import java.util.List;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Location;
import com.mongodb.client.MongoCollection;
import org.bson.conversions.Bson;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Chunk;
import java.util.Iterator;
import org.bson.Document;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.KingdomRunnable;
import me.crz.minecraft.kingdoms.utils.ThreadManager;
import com.google.common.collect.HashBasedTable;

public class LandManager
{
    private static LandManager instance;
    HashBasedTable<String, String, OwnedLand> land0;
    HashBasedTable<String, String, Boolean> mobData;
    
    public String getData(final int left, final int right) {
        return String.valueOf(left) + "-" + right;
    }
    
    public LandManager() {
        super();
        land0 = (HashBasedTable<String, String, OwnedLand>)HashBasedTable.create();
        mobData = (HashBasedTable<String, String, Boolean>)HashBasedTable.create();
        ThreadManager.getInstance().execute(new KingdomRunnable() {
            final /* synthetic */ LandManager this$0;
            
            LandManager$1() {
                this$0 = this$0;
                super();
            }
            
            @Override
            public void run() {
                for (final Document doc : DatabaseManager.getLandCollection().find()) {
                    final OwnedLand land = new OwnedLand(doc);
                    land0.put((Object)land.getWorldName(), (Object)this$0.getData(land.getX(), land.getZ()), (Object)land);
                    mobData.put((Object)land.getWorldName(), (Object)this$0.getData(land.getX(), land.getZ()), (Object)land.isMobSpawning());
                }
            }
        });
    }
    
    public boolean getMobSpawning(final Chunk c) {
        if (mobData.contains((Object)c.getWorld().getName(), (Object)this.getData(c.getX(), c.getZ()))) {
            return (boolean)mobData.get((Object)c.getWorld().getName(), (Object)this.getData(c.getX(), c.getZ()));
        }
        final OwnedLand land = this.getLand(c.getX(), c.getZ(), c.getWorld().getName());
        return land == null || land.isMobSpawning();
    }
    
    public boolean getMobSpawning(final OwnedLand land) {
        return !mobData.contains((Object)land.getWorldName(), (Object)this.getData(land.getX(), land.getZ())) || (boolean)mobData.get((Object)land.getWorldName(), (Object)this.getData(land.getX(), land.getZ()));
    }
    
    public void saveMobData(final OwnedLand land, final Boolean b) {
        mobData.put((Object)land.getWorldName(), (Object)this.getData(land.getX(), land.getZ()), (Object)b);
        this.saveLand(land);
    }
    
    public void saveLand(final OwnedLand land) {
        land0.put((Object)land.getWorldName(), (Object)this.getData(land.getX(), land.getZ()), (Object)land);
        this.saveLandToDatabase(land);
    }
    
    public void updateAllLand(final String n, final Village village) {
        for (final OwnedLand land : this.getClaimedLand(village)) {
            land.setOwnerVillage(n);
            this.saveLand(land);
        }
    }
    
    public void saveAll() {
        for (final OwnedLand l : land0.values()) {
            this.saveLand(l);
        }
    }
    
    public void saveLandToDatabase(final OwnedLand land) {
        final Document filter = new Document();
        filter.put("x", (Object)land.getX());
        filter.put("z", (Object)land.getZ());
        filter.put("worldName", (Object)land.getWorldName());
        final MongoCollection<Document> collection = DatabaseManager.getLandCollection();
        if (collection.find((Bson)filter).first() == null) {
            collection.insertOne((Object)land.getDocument());
        }
        else {
            collection.updateOne((Bson)new Document("_id", land.getDocument().get((Object)"_id")), (Bson)new Document("$set", (Object)land.getDocument()));
        }
    }
    
    public OwnedLand claimLand(final Location location, final Village village) {
        final OwnedLand land = new OwnedLand(village.getName(), location.getChunk().getX(), location.getChunk().getZ(), location.getWorld().getName(), "", 0, false);
        if (this.getClaimedLandSize(village) == 0) {
            village.setVillageHome(location);
        }
        VillageManager.getInstance().saveVillage(village);
        this.saveLand(land);
        return land;
    }
    
    public void claimLand(final Integer x, final Integer z, final String worldName, final Village village) {
        final OwnedLand land = new OwnedLand(village.getName(), x, z, worldName, "", 0, false);
        this.saveLand(land);
    }
    
    public void unclaimLand(final OwnedLand land) {
        this.removeLand(land);
    }
    
    public boolean isLandClaimed(final Location location) {
        final Chunk chunk = location.getChunk();
        return this.getLand(chunk.getX(), chunk.getZ(), chunk.getWorld().getName()) != null;
    }
    
    public boolean isLandClaimed(final Integer x, final Integer z, final String worldName) {
        return this.getLand(x, z, worldName) != null;
    }
    
    public OwnedLand getLandFromLocation(final Location location) {
        if (location == null || location.getChunk() == null) {
            return null;
        }
        final Chunk chunk = location.getChunk();
        return this.getLand(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
    }
    
    public OwnedLand getLand(final int x, final int z, final String worldName) {
        if (land0.contains((Object)worldName, (Object)this.getData(x, z))) {
            return (OwnedLand)land0.get((Object)worldName, (Object)this.getData(x, z));
        }
        return null;
    }
    
    public OwnedLand getLandFromDatabase0(final int x, final int z, final String worldName) {
        final Document filter = new Document();
        filter.put("x", (Object)x);
        filter.put("z", (Object)z);
        filter.put("worldName", (Object)worldName);
        final Document doc = (Document)DatabaseManager.getLandCollection().find((Bson)filter).first();
        if (doc == null) {
            return null;
        }
        final OwnedLand ownedLand = new OwnedLand(doc);
        land0.put((Object)worldName, (Object)this.getData(x, z), (Object)ownedLand);
        return ownedLand;
    }
    
    public void removeLand(final OwnedLand land) {
        if (land0.contains((Object)land.getWorldName(), (Object)this.getData(land.getX(), land.getZ()))) {
            land0.remove((Object)land.getWorldName(), (Object)this.getData(land.getX(), land.getZ()));
        }
        final Document filter = new Document();
        filter.put("x", (Object)land.getX());
        filter.put("z", (Object)land.getZ());
        filter.put("worldName", (Object)land.getWorldName());
        DatabaseManager.getLandCollection().deleteOne((Bson)filter);
    }
    
    public static LandManager getInstance() {
        if (instance == null) {
            instance = new LandManager();
        }
        return instance;
    }
    
    public int getClaimedLandSize(final Village village) {
        return this.getClaimedLand(village).size();
    }
    
    public List<OwnedLand> getClaimedLand(final Village village) {
        final List<OwnedLand> temp = new ArrayList<OwnedLand>();
        for (final Document doc : DatabaseManager.getLandCollection().find((Bson)new Document("ownerVillage", (Object)village.getName()))) {
            temp.add(new OwnedLand(doc));
        }
        return temp;
    }
    
    public int getClaimedLandSizeOutpost(final Village village) {
        return Math.toIntExact(this.getClaimedLand(village).stream().filter(land -> land.getIsOutpost()).count());
    }
    
    public void claimOutpost(final Location location, final Village village) {
        final OwnedLand land = this.claimLand(location, village);
        land.setIsOutpost(true);
        this.saveLand(land);
        village.addOutpostHome(location, land, "outpost-" + this.getClaimedLandSize(village));
        VillageManager.getInstance().saveVillage(village);
    }
    
    private static /* synthetic */ boolean lambda$0(final OwnedLand land) {
        return land.getIsOutpost();
    }
}

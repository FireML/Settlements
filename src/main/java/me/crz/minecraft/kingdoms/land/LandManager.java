package me.crz.minecraft.kingdoms.land;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.Location;
import com.mongodb.client.MongoCollection;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Chunk;

import org.bson.Document;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.ThreadManager;
import com.google.common.collect.HashBasedTable;

public class LandManager {
    private static LandManager instance;
    private HashBasedTable<String, String, OwnedLand> land0;
    private HashBasedTable<String, String, Boolean> mobData;

    public String getData(final int left, final int right) {
        return String.valueOf(left) + "-" + right;
    }

    public LandManager() {
        super();
        land0 = HashBasedTable.create();
        mobData = HashBasedTable.create();
        ThreadManager.getInstance().execute(() -> {
            for (final Document doc : DatabaseManager.getLandCollection().find()) {
                final OwnedLand land = new OwnedLand(doc);
                land0.put(land.getWorldName(), getData(land.getX(), land.getZ()), land);
                mobData.put(land.getWorldName(), getData(land.getX(), land.getZ()), land.isMobSpawning());
            }
        });
    }

    public boolean getMobSpawning(final Chunk c) {
        if (mobData.contains(c.getWorld().getName(), this.getData(c.getX(), c.getZ()))) {
            return mobData.get(c.getWorld().getName(), this.getData(c.getX(), c.getZ()));
        }
        final OwnedLand land = this.getLand(c.getX(), c.getZ(), c.getWorld().getName());
        return land == null || land.isMobSpawning();
    }

    public boolean getMobSpawning(final OwnedLand land) {
        return !mobData.contains(land.getWorldName(), this.getData(land.getX(), land.getZ())) || mobData.get(land.getWorldName(), this.getData(land.getX(), land.getZ()));
    }

    public void saveMobData(final OwnedLand land, final Boolean b) {
        mobData.put(land.getWorldName(), this.getData(land.getX(), land.getZ()), b);
        this.saveLand(land);
    }

    public void saveLand(final OwnedLand land) {
        land0.put(land.getWorldName(), this.getData(land.getX(), land.getZ()), land);
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
        filter.put("x", land.getX());
        filter.put("z", land.getZ());
        filter.put("worldName", land.getWorldName());
        final MongoCollection<Document> collection = DatabaseManager.getLandCollection();
        if (collection.find(filter).first() == null) {
            collection.insertOne(land.getDocument());
        } else {
            collection.updateOne(new Document("_id", land.getDocument().get("_id")), new Document("$set", land.getDocument()));
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

    public void claimLand(final int x, final int z, final String worldName, final Village village) {
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
        if (location == null) {
            return null;
        }
        return this.getLand(new SimpleChunkLocation(location));
    }

    public OwnedLand getLand(SimpleChunkLocation location) {
        if (location == null) {
            return null;
        }
        return land0.get(location.getWorld(), this.getData(location.getX(), location.getZ()));
    }

    public OwnedLand getLand(final int x, final int z, final String worldName) {
        return land0.get(worldName, this.getData(x, z));
    }

    public OwnedLand getLandFromDatabase0(final int x, final int z, final String worldName) {
        final Document filter = new Document();
        filter.put("x", x);
        filter.put("z", z);
        filter.put("worldName", worldName);
        final Document doc = DatabaseManager.getLandCollection().find(filter).first();
        if (doc == null) {
            return null;
        }
        final OwnedLand ownedLand = new OwnedLand(doc);
        land0.put(worldName, this.getData(x, z), ownedLand);
        return ownedLand;
    }

    public void removeLand(final OwnedLand land) {
        if (land0.contains(land.getWorldName(), this.getData(land.getX(), land.getZ()))) {
            land0.remove(land.getWorldName(), this.getData(land.getX(), land.getZ()));
        }
        final Document filter = new Document();
        filter.put("x", land.getX());
        filter.put("z", land.getZ());
        filter.put("worldName", land.getWorldName());
        DatabaseManager.getLandCollection().deleteOne(filter);
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
        final List<OwnedLand> temp = new ArrayList<>();
        for (final Document doc : DatabaseManager.getLandCollection().find(new Document("ownerVillage", village.getName()))) {
            temp.add(new OwnedLand(doc));
        }
        return temp;
    }

    public int getClaimedLandSizeOutpost(final Village village) {
        return Math.toIntExact(this.getClaimedLand(village).stream().filter(OwnedLand::getIsOutpost).count());
    }

    public void claimOutpost(final Location location, final Village village) {
        final OwnedLand land = this.claimLand(location, village);
        land.setIsOutpost(true);
        this.saveLand(land);
        village.addOutpostHome(location, land, "outpost-" + this.getClaimedLandSize(village));
        VillageManager.getInstance().saveVillage(village);
    }

    public Collection<OwnedLand> getAllConnectedLand(OwnedLand land, boolean outpostMode) {
        Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        if (village == null) {
            return Collections.emptyList();
        }

        int[][] directions = {{1,0}, {0,1}, {-1,0}, {0,-1}};

        List<OwnedLand> ownedLands = new ArrayList<>();
        Set<SimpleChunkLocation> checked = new HashSet<>();
        Queue<SimpleChunkLocation> toSearch = new ArrayDeque<>();
        Set<SimpleChunkLocation> outposts = outpostMode
                ? Collections.emptySet()
                : village.getOutpostHomes().values().stream().map(SimpleChunkLocation::new).collect(Collectors.toSet());

        toSearch.add(land.getLocation());
        while (!toSearch.isEmpty()) {
            SimpleChunkLocation current = toSearch.poll();
            for (int[] direction : directions) {
                SimpleChunkLocation search = current.copy().add(direction[0], direction[1]);
                if (outpostMode) {
                    if (search.equals(village.getVillageHome().toChunkLocation())) {
                        // return an empty set if this is connected to the home chunk
                        return Collections.emptyList();
                    }
                } else {
                    if (outposts.contains(search)) {
                        // return an empty set if this is connected to an outpost
                        return Collections.emptyList();
                    }
                }
                if (!checked.contains(search)) {
                    checked.add(search);
                    OwnedLand foundLand = getLand(search);
                    if (foundLand != null && foundLand.getOwnerVillage().equals(land.getOwnerVillage())) {
                        ownedLands.add(foundLand);
                        toSearch.add(search);
                    }
                }
            }
        }

        return ownedLands;
    }
}

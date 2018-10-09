package me.crz.minecraft.kingdoms.land;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Chunk;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import com.google.common.collect.HashBasedTable;
import org.bukkit.World;

public class LandManager {
    private static LandManager instance;
    private HashBasedTable<String, String, OwnedLand> landTable = HashBasedTable.create();
    private Multimap<IdObject, SimpleChunkLocation> villageLands = MultimapBuilder.hashKeys().hashSetValues().build();
    private Multimap<UUID, SimpleChunkLocation> playerPlots = MultimapBuilder.hashKeys().hashSetValues().build();
    private HashBasedTable<String, String, Boolean> mobData = HashBasedTable.create();

    public LandManager() {
        super();
        for (OwnedLand land : DatabaseManager.getInstance().getAllLand()) {
            landTable.put(land.getWorldName(), land.getSimpleKey(), land);
            villageLands.put(land.getOwnerVillage().getId(), land.getLocation());
            if (land.isPlot()) {
                playerPlots.put(land.getPlotOwner(), land.getLocation());
            }
        }
    }

    /**
     * Use {@link DatabaseManager#saveLand(OwnedLand...)}
     */
    @Deprecated
    public void saveLand(OwnedLand land) {
        cacheLand(land);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
    }

    private void cacheLand(OwnedLand land) {
        landTable.put(land.getWorldName(), land.getSimpleKey(), land);
        villageLands.put(land.getOwnerVillage().getId(), land.getLocation());
        if (land.isPlot()) {
            playerPlots.put(land.getPlotOwner(), land.getLocation());
        }
    }

    public void changeOwner(Village village, Village newOwner) {
        Collection<OwnedLand> lands = getClaimedLand(village);
        villageLands.removeAll(village.getId());
        for (OwnedLand land : lands) {
            land.setOwnerVillage(newOwner);
            villageLands.put(newOwner.getId(), land.getLocation());
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(lands.toArray(new OwnedLand[0])));
    }

    public OwnedLand claimLand(Location location, Village village) {
        SimpleChunkLocation chunkLocation = new SimpleChunkLocation(location);
        OwnedLand land = new OwnedLand(village, chunkLocation);
        if (this.getClaimedLandSize(village) == 0) {
            village.setVillageHome(location);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        }
        cacheLand(land);
        return land;
    }

    public boolean isLandClaimed(Location location) {
        return this.getLand(new SimpleChunkLocation(location)) != null;
    }

    public OwnedLand getLand(Location location) {
        if (location == null) {
            return null;
        }
        return this.getLand(new SimpleChunkLocation(location));
    }

    public OwnedLand getLand(SimpleChunkLocation location) {
        if (location == null) {
            return null;
        }
        return landTable.get(location.getWorld(), location.getSimpleKey());
    }

    public OwnedLand getLand(int x, int z, World world) {
        return getLand(new SimpleChunkLocation(world.getName(), x, z));
    }

    public OwnedLand getLand(Chunk chunk) {
        if (chunk == null) {
            return null;
        }
        return getLand(new SimpleChunkLocation(chunk));
    }

    public void unclaimLand(Collection<OwnedLand> lands) {
        unclaimLand(lands.toArray(new OwnedLand[0]));
    }

    public void unclaimLand(OwnedLand... lands) {
        for (OwnedLand land : lands) {
            landTable.remove(land.getWorldName(), land.getSimpleKey());
            villageLands.remove(land.getOwnerVillage().getId(), land.getLocation());
            if (land.getOwnerVillage() != null && land.getIsOutpost()) {
                land.getOwnerVillage().getOutpostHomes().values().removeIf(l -> land.getLocation().contains(l));
            }
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().unclaimLand(lands));
    }

    public void unclaimLand(Village village) {
        for (SimpleChunkLocation claim : villageLands.get(village.getId())) {
            landTable.remove(claim.getWorld(), claim.getSimpleKey());
        }
        villageLands.removeAll(village.getId());
        Utils.runAsync(() -> DatabaseManager.getInstance().unclaimLand(village));
    }

    public static LandManager getInstance() {
        if (instance == null) {
            instance = new LandManager();
        }
        return instance;
    }

    public Collection<OwnedLand> getOwnedPlot(UUID uniqueId) {
        return playerPlots.get(uniqueId).stream().map(this::getLand).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void setPlotOwner(OwnedLand land, UUID owner) {
        if (land.isPlot()) {
            playerPlots.remove(land.getPlotOwner(), land.getLocation());
        }
        land.setPlotOwner(owner);
        land.setBuildState(PlotBuildState.PRIVATE);
        for (Use use : Use.values()) {
            land.setUseState(use, PlotBuildState.PRIVATE);
        }
        if (owner != null) {
            playerPlots.put(owner, land.getLocation());
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
    }

    public int getClaimedLandSize(Village village) {
        return villageLands.get(village.getId()).size();
    }

    public Collection<OwnedLand> getClaimedLand(Village village) {
        return villageLands.get(village.getId()).stream().map(this::getLand).collect(Collectors.toList());
    }

    public OwnedLand claimOutpost(Location location, Village village, String name) {
        OwnedLand land = this.claimLand(location, village);
        land.setIsOutpost(true);
        cacheLand(land);
        village.addOutpostHome(name, location);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        return land;
    }

    public Collection<OwnedLand> getAllConnectedLand(OwnedLand land, boolean outpostMode) {
        Village village = land.getOwnerVillage();
        if (village == null) {
            return new ArrayList<>();
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
                        return new ArrayList<>();
                    }
                } else {
                    if (outposts.contains(search)) {
                        // return an empty set if this is connected to an outpost
                        return new ArrayList<>();
                    }
                }
                if (!checked.contains(search)) {
                    checked.add(search);
                    OwnedLand foundLand = getLand(search);
                    if (foundLand != null && foundLand.getOwnerVillage().equals(land.getOwnerVillage()) && !foundLand.getIsOutpost()) {
                        ownedLands.add(foundLand);
                        toSearch.add(search);
                    }
                }
            }
        }

        return ownedLands;
    }
}

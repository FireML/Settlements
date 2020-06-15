package me.crz.minecraft.kingdoms.land;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Chunk;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import com.google.common.collect.HashBasedTable;
import org.bukkit.World;

public class LandManager {
    private static LandManager instance;
    private final int nearbyRadius;
    private Table<String, String, OwnedLand> landTable = HashBasedTable.create();
    private Multimap<String, SimpleChunkLocation> nearbyTable = MultimapBuilder.hashKeys().hashSetValues().build();
    private Multimap<IdObject, OwnedLand> villageLands = MultimapBuilder.hashKeys().hashSetValues().build();
    private Multimap<UUID, OwnedLand> playerPlots = MultimapBuilder.hashKeys().hashSetValues().build();
    private Multimap<UUID, OwnedLand> linkedPlots = MultimapBuilder.hashKeys().hashSetValues().build();

    private LandManager() {
        super();
        nearbyRadius = Utils.getCloseBuildingNotificationDistance();
        for (OwnedLand land : DatabaseManager.getInstance().getAllLand()) {
            cacheLand(land);
        }
    }

    public void saveLand(OwnedLand... lands) {
        for (OwnedLand land : lands) {
            cacheLand(land);
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(lands));
    }

    private void cacheLand(OwnedLand land) {
        landTable.put(land.getWorldName(), land.getSimpleKey(), land);
        if (land.getLandOwner() instanceof Village) {
            villageLands.put(land.getLandOwner().getId(), land);
            if (nearbyRadius > 0) {
                nearbyTable.removeAll(land.getKey());
                SimpleChunkLocation landLocation = land.getLocation().copy();
                for (SimpleChunkLocation chunkLocation : Utils.getChunksAround(land, nearbyRadius)) {
                    if (!landTable.contains(chunkLocation.getWorld(), chunkLocation.getSimpleKey())) {
                        nearbyTable.put(chunkLocation.getKey(), landLocation);
                    }
                }
            }
        }
        if (land.isPlot()) {
            playerPlots.put(land.getPlotOwner(), land);
            // Cache plot owner
            ResidentManager.getInstance().getResident(land.getPlotOwner());
        }
        if (land.hasLink()) {
            linkedPlots.put(land.getLink(), land);
        }
    }

    public void changeOwner(Village village, Village newOwner) {
        Collection<OwnedLand> lands = getClaimedLand(village);
        villageLands.removeAll(village.getId());
        for (OwnedLand land : lands) {
            land.setLandOwner(newOwner);
            villageLands.put(newOwner.getId(), land);
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(lands.toArray(new OwnedLand[0])));
    }

    public OwnedLand claimLand(SimpleChunkLocation chunk, Location location, Village village) {
        OwnedLand land = new OwnedLand(village, chunk);
        if (this.getClaimedLandSize(village) == 0) {
            SimpleLocation home = new SimpleLocation(location);
            if (chunk.contains(home)) {
                village.setHome(location);
            } else {
                village.setHome(chunk.getCenterBukkitLocation());
            }
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        }
        KingdomsPlugin.getInstance().getLogger().log(Level.INFO, "Chunk " + land.getKey() + " was claimed by " + village.getName() + "");
        cacheLand(land);
        return land;
    }

    public boolean isLandClaimed(Location location) {
        return this.getLand(location) != null;
    }

    public boolean hasLand(World world) {
        return world != null && landTable.containsRow(world.getName());
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
        OwnedLand land = landTable.get(location.getWorld(), location.getSimpleKey());
        if (land == null) {
            Empire empire = EmpireManager.getInstance().getEmpireFromWorld(location.getWorld());
            if (empire != null && !empire.isAbandoned()) {
                land = new OwnedLand(empire, location);
            }
        }
        return land;
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

    public void renameWorld(String oldName, String newName) {
        Table<String, String, OwnedLand> newEntries = landTable.row(oldName).entrySet().stream().collect(Tables.toTable(e -> newName, Map.Entry::getKey, e -> {
            e.getValue().setWorldName(newName);
            return e.getValue();
        }, HashBasedTable::create));
        landTable.row(oldName).clear();
        landTable.putAll(newEntries);
        Utils.runAsync(() -> DatabaseManager.getInstance().renameLandWorld(oldName, newName));
    }

    public void removeWorld(String worldName) {
        landTable.row(worldName).clear();
        Utils.runAsync(() -> DatabaseManager.getInstance().unclaimLand(worldName));
    }

    public void unclaimLand(Collection<OwnedLand> lands) {
        unclaimLand(lands.toArray(new OwnedLand[0]));
    }

    public void unclaimLand(OwnedLand... lands) {
        Set<Village> saveVillages = new HashSet<>();
        for (OwnedLand land : lands) {
            if (land.getLandOwner() == null) {
                continue;
            }
            if (land.containsHome()) {
                land.getLandOwner().setHome((SimpleLocation) null);
            }
            landTable.remove(land.getWorldName(), land.getSimpleKey());
            if (land.getLandOwner() instanceof Village) {
                villageLands.remove(land.getLandOwner().getId(), land);
                if (land.isOutpost()) {
                    if (((Village) land.getLandOwner()).getOutpostHomes().remove(land.getOutpostName().toLowerCase()) != null) {
                        saveVillages.add((Village) land.getLandOwner());
                    }
                }
            }
            KingdomsPlugin.getInstance().getLogger().log(Level.INFO, "Chunk " + land.getKey() + " owned by " + land.getLandOwner().getName() + " was unclaimed");
            land.setLandOwner(null);
        }
        if (nearbyRadius > 0) {
            for (OwnedLand land : lands) {
                for (SimpleChunkLocation chunkLocation : Utils.getChunksAround(land, nearbyRadius)) {
                    OwnedLand l = getLand(chunkLocation);
                    if (l != null) {
                        nearbyTable.put(land.getKey(), l.getLocation().copy());
                    }
                }
            }
        }
        Utils.runAsync(() -> {
            if (KingdomsPlugin.getInstance().getConfig().getBoolean("remove-locks-on-unclaim")) {
                removeLocks(lands);
            }
            DatabaseManager.getInstance().unclaimLand(lands);
            DatabaseManager.getInstance().saveVillages(saveVillages);
        });
    }

    public void unclaimLand(Village village) {
        Utils.runSync(() -> {
            List<OwnedLand> claims = new ArrayList<>(villageLands.get(village.getId()));
            for (OwnedLand claim : claims) {
                removeLink(claim);
                landTable.remove(claim.getWorld(), claim.getSimpleKey());
                KingdomsPlugin.getInstance().getLogger().log(Level.INFO, "Chunk " + claim.getKey() + " owned by " + village.getName() + " was unclaimed");
            }
            villageLands.removeAll(village.getId());
            if (KingdomsPlugin.getInstance().getConfig().getBoolean("remove-locks-on-unclaim")) {
                removeLocks(claims);
            }
        });
        Utils.runAsync(() -> DatabaseManager.getInstance().unclaimLand(village));
    }

    public boolean removeLink(OwnedLand land) {
        if (land != null && land.hasLink()) {
            UUID link = land.getLink();
            land.setLink(null);
            return linkedPlots.remove(link, land);
        }
        return false;
    }

    public boolean addLink(OwnedLand land, UUID link) {
        if (land != null && link != null && !link.equals(land.getLink())) {
            removeLink(land);
            land.setLink(link);
            return linkedPlots.put(link, land);
        }
        return false;
    }

    public Collection<OwnedLand> getLinked(OwnedLand land) {
        if (land != null && land.hasLink()) {
            return linkedPlots.get(land.getLink());
        }
        return new HashSet<>();
    }

    private void removeLocks(OwnedLand... lands) {
        removeLocks(Arrays.asList(lands));
    }

    private void removeLocks(Collection<? extends SimpleChunkLocation> claims) {
        KingdomsPlugin.getInstance().getLockProviders().forEach(i -> i.removeInChunks(claims));
    }

    synchronized public static LandManager getInstance() {
        if (instance == null) {
            instance = new LandManager();
        }
        return instance;
    }

    public Collection<OwnedLand> getOwnedPlot(UUID uniqueId) {
        return playerPlots.get(uniqueId).stream().map(this::getLand).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean ownsPlot(UUID playerId) {
        return playerPlots.containsKey(playerId);
    }

    public void setPlotOwner(OwnedLand land, UUID owner) {
        if (land.isPlot()) {
            playerPlots.remove(land.getPlotOwner(), land.getLocation());
        }
        land.removeLink();
        land.setPlotOwner(owner);
        if (owner != null) {
            land.setBuildState(BuildState.PRIVATE);
            for (Use use : Use.values()) {
                land.setUseState(use, use.getDefault(BuildState.PRIVATE));
            }
        } else {
            land.setEmbassy(false);
            land.setBuildState(land.getLandOwner().getBuildState());
            for (Use use : Use.values()) {
                land.unsetUseState(use);
            }
        }
        saveLand(land);
    }

    public int getClaimedLandSize(Village village) {
        return villageLands.get(village.getId()).size();
    }

    public Collection<OwnedLand> getClaimedLand(LandOwner owner) {
        if (owner instanceof Village) {
            return villageLands.get(owner.getId());
        } else if (owner instanceof Empire) {
            return landTable.row(((Empire) owner).getWorldName()).values();
        }

        return Collections.emptySet();
    }

    public OwnedLand claimOutpost(Location location, Village village, String name) {
        if (village.getBank() < Utils.getOutpostCreationCost()) {
            return null;
        }
        OwnedLand land = this.claimLand(new SimpleChunkLocation(location), location, village);
        land.setOutpost(name);
        cacheLand(land);
        village.setOutpostHome(name, location);
        village.setBank(village.getBank() - Utils.getOutpostCreationCost());
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(village);
            DatabaseManager.getInstance().saveLand(land);
        });
        return land;
    }

    public Set<OwnedLand> getAllConnectedLand(OwnedLand land, Function<OwnedLand, FilterResponse> filter) {
        Set<OwnedLand> found = new LinkedHashSet<>();
        if (land.getLandOwner() == null) {
            return found;
        }

        int[][] directions = {{1,0}, {0,1}, {-1,0}, {0,-1}};

        Set<SimpleChunkLocation> checked = new HashSet<>();
        checked.add(land);

        for (int[] branchDir : directions) {
            Set<SimpleChunkLocation> toSearch = new LinkedHashSet<>();
            SimpleChunkLocation branch = land.getLocation().copy().add(branchDir[0], branchDir[1]);
            if (checked.contains(branch)) {
                continue;
            }
            toSearch.add(branch);

            List<OwnedLand> branchFound = new ArrayList<>();

            boolean skipBranch = false;
            BRANCH:
            while (!toSearch.isEmpty()) {
                SimpleChunkLocation search = Utils.poll(toSearch);
                OwnedLand foundLand = getLand(search);
                if (foundLand != null && foundLand.isOwnedBy(land.getLandOwner())) {
                    if (filter != null) {
                        switch (filter.apply(foundLand)) {
                            case ABORT:
                                return new LinkedHashSet<>();
                            case RETURN:
                                branchFound.add(foundLand);
                            case RETURN_CURRENT:
                                found.addAll(branchFound);
                                return found;
                            case SKIP_ENTRY:
                                continue BRANCH;
                            case SKIP_BRANCH:
                                skipBranch = true;
                            case ACCEPT:
                                branchFound.add(foundLand);
                            case CONTINUE:
                                break;
                        }
                    }

                    for (int[] direction : directions) {
                        if (direction != branchDir) {
                            SimpleChunkLocation next = search.copy().add(direction[0], direction[1]);
                            if (!checked.contains(next)) {
                                toSearch.add(next);
                            }
                        }
                    }
                    SimpleChunkLocation next = search.copy().add(branchDir[0], branchDir[1]);
                    if (!checked.contains(next)) {
                        toSearch.add(next);
                    }
                }
                checked.add(search);
            }

            if (!skipBranch) {
                found.addAll(branchFound);
            }
        }

        return found;
    }

    public void syncAndSave(OwnedLand land) {
        List<OwnedLand> lands = new ArrayList<>();
        lands.add(land);
        cacheLand(land);
        for (OwnedLand c : land.getLinked()) {
            if (c.syncLinked(land)) {
                lands.add(c);
            }
        }

        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(lands));
    }

    public void checkUnclaim() {
        if (Utils.getCloseClaimsGraceDays() < 0) {
            return;
        }
        Map<LandOwner, Integer> villageUnclaims = new HashMap<>();
        List<OwnedLand> toUnclaim = new ArrayList<>();
        List<OwnedLand> toSave = new ArrayList<>();
        for (OwnedLand land : landTable.values()) {
            if (land.getUnclaimIn() > 0) {
                land.setUnclaimIn(land.getUnclaimIn() - 1);
                if (land.getUnclaimIn() == 0) {
                    toUnclaim.add(land);
                    villageUnclaims.put(land.getLandOwner(), villageUnclaims.getOrDefault(land.getLandOwner(), 0) + 1);
                } else {
                    toSave.add(land);
                }
            }
        }
        unclaimLand(toUnclaim);
        toUnclaim.clear();
        for (LandOwner landOwner : villageUnclaims.keySet()) {
            if (landOwner instanceof Village) {
                for (OwnedLand land : getClaimedLand((Village) landOwner)) {
                    if (!isConnected(land)) {
                        toUnclaim.add(land);
                        villageUnclaims.put(land.getLandOwner(), villageUnclaims.getOrDefault(land.getLandOwner(), 0) + 1);
                    }
                }
            }
        }
        unclaimLand(toUnclaim);
        for (Map.Entry<LandOwner, Integer> e : villageUnclaims.entrySet()) {
            Message.get("land-unclaim").tell(e.getKey().getOnlineResidents(), "amount", String.valueOf(e.getValue()));
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
    }

    public boolean isConnected(OwnedLand land) {
        return getAllConnectedLand(land, search -> {
            if (search.isOutpost() || search.containsHome()) {
                return FilterResponse.RETURN;
            }
            return FilterResponse.CONTINUE;
        }).size() > 0;
    }

    public void checkClaimDistance(LandOwner source, LandOwner oldAlly) {
        if (Utils.getClaimEnemyDistance() > 0 && (!(source instanceof Village) || !((Village) source).isAllowAllyClaim())) {
            if (oldAlly instanceof Village) {
                checkClaimDistance(source, (Village) oldAlly);
            } else if (oldAlly instanceof Empire) {
                for (Village v : ((Empire) oldAlly).getMembers()) {
                    checkClaimDistance(source, v);
                }
            }
        }
    }

    private void checkClaimDistance(LandOwner source, Village oldAlly) {
        int graceDays = Utils.getCloseClaimsGraceDays();
        if (graceDays < 0) {
            return;
        }
        boolean unclaimOutposts = Utils.getCloseClaimsUnclaimsOutposts();
        List<OwnedLand> toSave = new ArrayList<>();
        List<OwnedLand> toUnclaim = new ArrayList<>();
        int enemyClaimDistance = Utils.getClaimEnemyDistance();
        for (OwnedLand land : getClaimedLand(oldAlly)) {
            if (enemyClaimDistance > 0 && (unclaimOutposts || !land.isOutpost())) {
                for (SimpleChunkLocation c : Utils.getChunksAround(land, enemyClaimDistance)) {
                    OwnedLand around = getLand(c);
                    if (around != null && around.isOwnedBy(source)) {
                        if (graceDays > 0) {
                            if (around.getUnclaimIn() == 0 && (land.getUnclaimIn() == 0 || land.getUnclaimIn() > graceDays)) {
                                land.setUnclaimIn(graceDays);
                                toSave.add(land);
                            }
                        } else {
                            toUnclaim.add(land);
                        }
                        break;
                    }
                }
            }
        }
        if (!toUnclaim.isEmpty()) {
            unclaimLand(toUnclaim);
            Message.get("land-unclaim").tell(oldAlly.getOnlineResidents(), "amount", String.valueOf(toUnclaim.size()));
        }
        if (!toSave.isEmpty()) {
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
        }
    }

    public Set<LandOwner> getNearbyLandOwners(Location location) {
        if (nearbyRadius == 0) {
            return ImmutableSet.of();
        }
        return getNearbyLandOwners(new SimpleChunkLocation(location));
    }

    public Set<LandOwner> getNearbyLandOwners(SimpleChunkLocation chunkLocation) {
        if (nearbyRadius == 0) {
            return ImmutableSet.of();
        }
        Set<LandOwner> owners = new HashSet<>();
        for (Iterator<SimpleChunkLocation> it = nearbyTable.get(chunkLocation.getKey()).iterator(); it.hasNext(); ) {
            SimpleChunkLocation nearbyLocation = it.next();
            OwnedLand land = getLand(nearbyLocation);
            if (land != null) {
                owners.add(land.getLandOwner());
            } else {
                it.remove();
            }
        }
        return owners;
    }

    public enum FilterResponse {
        /**
         * Aborts and returns an empty collection
         */
        ABORT,
        /**
         * Return the current found collection without the searched object
         */
        RETURN_CURRENT,
        /**
         * Return the found collection including the current object
         */
        RETURN,
        /**
         * Skip the current entry
         */
        SKIP_ENTRY,
        /**
         * Skip the currently searched branch
         */
        SKIP_BRANCH,
        /**
         * Continue walking the branch without adding the current entry
         */
        CONTINUE,
        /**
         * Accept the entry and continue
         */
        ACCEPT;
    }
}

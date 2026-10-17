package de.themoep.settlements.land;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.events.LandClaimEvent;
import de.themoep.settlements.land.events.LandPreClaimEvent;
import de.themoep.settlements.land.events.LandUnclaimEvent;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.WorldUtils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.world.SimpleBlockLocation;
import de.themoep.settlements.world.SimpleChunkLocation;
import de.themoep.settlements.world.SimpleLocation;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
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

public class LandManager {
    private static LandManager instance;
    private final int nearbyRadius;
    private final Table<String, String, OwnedLand> landTable = HashBasedTable.create();
    private final Multimap<String, SimpleChunkLocation> nearbyTable = MultimapBuilder.hashKeys().hashSetValues().build();
    private final Multimap<IdObject, OwnedLand> settlementLands = MultimapBuilder.hashKeys().hashSetValues().build();
    private final Multimap<UUID, OwnedLand> playerPlots = MultimapBuilder.hashKeys().hashSetValues().build();
    private final Multimap<UUID, OwnedLand> linkedPlots = MultimapBuilder.hashKeys().hashSetValues().build();

    private LandManager() {
        nearbyRadius = Config.Claiming.Close.Building.DISTANCE;
    }

    private void setup() {
        for (OwnedLand land : DatabaseManager.getInstance().getAllLand()) {
            cacheLand(land);
        }
        SettlementsPlugin.logDebug("Loaded LandManager with " + settlementLands.size() + " lands, " + playerPlots.size() + " player plots and " + linkedPlots.size() + " linked plots");

    }

    public void saveLand(OwnedLand... lands) {
        for (OwnedLand land : lands) {
            cacheLand(land);
        }
        DatabaseManager.getInstance().saveLand(lands);
    }

    private void cacheLand(OwnedLand land) {
        landTable.put(land.getWorldName(), land.getSimpleKey(), land);
        if (land.getSettlement() instanceof Settlement) {
            settlementLands.put(land.getSettlement().getId(), land);
        }
        if (nearbyRadius > 0) {
            nearbyTable.removeAll(land.getKey());
            SimpleChunkLocation landLocation = land.getLocation().copy();
            for (SimpleChunkLocation chunkLocation : WorldUtils.getChunksAround(land, nearbyRadius)) {
                if (!landTable.contains(chunkLocation.getWorld(), chunkLocation.getSimpleKey())) {
                    nearbyTable.put(chunkLocation.getKey(), landLocation);
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

    public OwnedLand claimLand(SimpleChunkLocation chunk, Location location, Settlement landOwner) {
        OwnedLand land = new OwnedLand(landOwner, chunk);
        new LandPreClaimEvent(land).callEvent();
        if (this.getClaimedLandSize(landOwner) == 0) {
            SimpleLocation home = new SimpleLocation(location);
            if (chunk.contains(home)) {
                landOwner.setHome(location);
            } else {
                landOwner.setHome(chunk.getCenterBukkitLocation());
            }
            landOwner.save();
        }
        SettlementsPlugin.getInstance().getLogging().info("Chunk " + land.getKey() + " was claimed by " + landOwner.getStateTag() + " " + landOwner.getName());
        cacheLand(land);
        new LandClaimEvent(land).callEvent();
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

    public OwnedLand getLand(SimpleBlockLocation location) {
        if (location == null) {
            return null;
        }
        return this.getLand(new SimpleChunkLocation(location.getWorld(), location.getChunkX(), location.getChunkZ()));
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
        Set<Settlement> saveSettlements = new HashSet<>();
        for (OwnedLand land : lands) {
            if (land.getSettlement() == null) {
                continue;
            }
            if (land.containsHome()) {
                land.getSettlement().setHome((SimpleLocation) null);
            }
            new LandUnclaimEvent(land).callEvent();
            landTable.remove(land.getWorldName(), land.getSimpleKey());
            settlementLands.remove(land.getSettlement().getId(), land);
            if (land.isOutpost()) {
                if (land.getSettlement().getOutpostHomes().remove(land.getOutpostName().toLowerCase()) != null) {
                    saveSettlements.add(land.getSettlement());
                }
            }
            SettlementsPlugin.getInstance().getLogging().info("Chunk " + land.getKey() + " owned by " + land.getSettlement().getStateTag() + " " + land.getSettlement().getName() + " was unclaimed");
            land.setSettlement(null);
        }
        if (nearbyRadius > 0) {
            for (OwnedLand land : lands) {
                for (SimpleChunkLocation chunkLocation : WorldUtils.getChunksAround(land, nearbyRadius)) {
                    OwnedLand l = getLand(chunkLocation);
                    if (l != null) {
                        nearbyTable.put(land.getKey(), l.getLocation().copy());
                    }
                }
            }
        }
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().unclaimLand(lands);
            DatabaseManager.getInstance().saveSettlements(saveSettlements);
        });
    }

    public void unclaimLand(Settlement landowner) {
        Utils.runSync(() -> {
            List<OwnedLand> claims = new ArrayList<>(getClaimedLand(landowner));
            for (OwnedLand claim : claims) {
                new LandUnclaimEvent(claim).callEvent();
                removeLink(claim);
                landTable.remove(claim.getWorld(), claim.getSimpleKey());
                SettlementsPlugin.getInstance().getLogging().info("Chunk " + claim.getKey() + " owned by " + landowner.getName() + " was unclaimed");
            }
            settlementLands.removeAll(landowner.getId());
        });
        Utils.runAsync(() -> DatabaseManager.getInstance().unclaimLand(landowner));
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

    public static LandManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new LandManager();
            instance.setup();
        }
    }

    public int getOwnedPlotCount(UUID uniqueId) {
        return playerPlots.get(uniqueId).size();
    }

    public Collection<OwnedLand> getOwnedPlot(UUID uniqueId) {
        return playerPlots.get(uniqueId).stream().map(this::getLand).filter(Objects::nonNull).toList();
    }

    public boolean ownsPlot(UUID playerId) {
        return playerPlots.containsKey(playerId);
    }

    public void setPlotOwner(OwnedLand land, UUID owner) {
        if (land.isPlot()) {
            playerPlots.remove(land.getPlotOwner(), land.getLocation());
        }
        land.getPlotMembers().clear();
        land.removeLink();
        land.setPlotOwner(owner);
        if (owner != null) {
            land.setBuildState(BuildState.PRIVATE);
            for (UseFlag useFlag : UseFlagManager.getInstance().getUseFlags()) {
                land.setUseState(useFlag, useFlag.getDefault(BuildState.PRIVATE));
            }
        } else {
            land.setBuildState(null);
            land.setEmbassy(false);
            for (Use use : Use.values()) {
                land.unsetUseState(use);
            }
        }
        saveLand(land);
    }

    public int getClaimedLandSize(Settlement settlement) {
        return settlementLands.get(settlement.getId()).size();
    }

    public Collection<OwnedLand> getWorldLand(String world) {
        return landTable.row(world).values();
    }

    public Collection<OwnedLand> getClaimedLand(Settlement owner) {
        return settlementLands.get(owner.getId());
    }

    public OwnedLand claimOutpost(Location location, Settlement settlement, String name) {
        if (!settlement.removeFromBank(Config.Settlement.Outpost.CREATION_COST)) {
            return null;
        }
        OwnedLand land = this.claimLand(new SimpleChunkLocation(location), location, settlement);
        land.setOutpost(name);
        cacheLand(land);
        settlement.setOutpostHome(name, location);
        DatabaseManager.getInstance().saveSettlement(settlement);
        DatabaseManager.getInstance().saveLand(land);
        return land;
    }

    public Set<OwnedLand> getAllConnectedLand(OwnedLand land, Function<OwnedLand, FilterResponse> filter) {
        Set<OwnedLand> found = new LinkedHashSet<>();
        if (land.getSettlement() == null) {
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
            while (!toSearch.isEmpty()) {
                SimpleChunkLocation search = Utils.poll(toSearch);
                OwnedLand foundLand = getLand(search);
                if (foundLand != null && foundLand.isOwnedBy(land.getSettlement())) {
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
                                continue;
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

        DatabaseManager.getInstance().saveLand(lands);
    }

    public void checkUnclaim() {
        if (Config.Claiming.Close.Unclaim.GRACE_DAYS < 0) {
            return;
        }
        Map<Settlement, Integer> landUnclaims = new HashMap<>();
        List<OwnedLand> toUnclaim = new ArrayList<>();
        List<OwnedLand> toSave = new ArrayList<>();
        for (OwnedLand land : landTable.values()) {
            if (land.getUnclaimIn() > 0) {
                land.setUnclaimIn(land.getUnclaimIn() - 1);
                if (land.getUnclaimIn() == 0) {
                    toUnclaim.add(land);
                    landUnclaims.put(land.getSettlement(), landUnclaims.getOrDefault(land.getSettlement(), 0) + 1);
                } else {
                    toSave.add(land);
                }
            }
        }
        unclaimLand(toUnclaim);
        toUnclaim.clear();
        for (Settlement settlement : landUnclaims.keySet()) {
            for (OwnedLand land : getClaimedLand(settlement)) {
                if (!isConnected(land)) {
                    toUnclaim.add(land);
                    landUnclaims.put(land.getSettlement(), landUnclaims.getOrDefault(land.getSettlement(), 0) + 1);
                }
            }
        }
        unclaimLand(toUnclaim);
        for (Map.Entry<Settlement, Integer> e : landUnclaims.entrySet()) {
            Message.get("land-unclaim").tell(e.getKey().getOnlineResidents(), "amount", String.valueOf(e.getValue()));
        }
        DatabaseManager.getInstance().saveLand(toSave);
    }

    public boolean isConnected(OwnedLand land) {
        return !getAllConnectedLand(land, search -> {
            if (search.isOutpost() || search.containsHome()) {
                return FilterResponse.RETURN;
            }
            return FilterResponse.CONTINUE;
        }).isEmpty();
    }

    public void checkClaimDistance(Settlement source, Settlement oldAlly) {
        if (Config.Claiming.ENEMY_DISTANCE <= 0) {
            return;
        }

        int graceDays = Config.Claiming.Close.Unclaim.GRACE_DAYS;
        if (graceDays < 0) {
            return;
        }

        if (source.isAllowAllyClaim()) {
            return;
        }

        Ally ally = source.getAlly(oldAlly);
        if (ally != null && ally.isNearbyClaim()) {
            return;
        }

        List<OwnedLand> toSave = new ArrayList<>();
        List<OwnedLand> toUnclaim = new ArrayList<>();
        for (OwnedLand land : getClaimedLand(oldAlly)) {
            if (Config.Claiming.ENEMY_DISTANCE > 0 && !land.containsHome() && (Config.Claiming.Close.Unclaim.OUTPOSTS || !land.isOutpost())) {
                for (SimpleChunkLocation c : WorldUtils.getChunksAround(land, Config.Claiming.ENEMY_DISTANCE)) {
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
            DatabaseManager.getInstance().saveLand(toSave);
        }
    }

    public Set<Settlement> getNearbyLandOwners(Location location) {
        if (nearbyRadius == 0) {
            return ImmutableSet.of();
        }
        return getNearbyLandOwners(new SimpleChunkLocation(location));
    }

    public Set<Settlement> getNearbyLandOwners(SimpleChunkLocation chunkLocation) {
        if (nearbyRadius == 0) {
            return ImmutableSet.of();
        }
        Set<Settlement> owners = new HashSet<>();
        for (Iterator<SimpleChunkLocation> it = nearbyTable.get(chunkLocation.getKey()).iterator(); it.hasNext(); ) {
            SimpleChunkLocation nearbyLocation = it.next();
            OwnedLand land = getLand(nearbyLocation);
            if (land != null) {
                owners.add(land.getSettlement());
            } else {
                it.remove();
            }
        }
        return owners;
    }

    public Collection<OwnedLand> getAllLand() {
        return landTable.values();
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
        ACCEPT
    }
}

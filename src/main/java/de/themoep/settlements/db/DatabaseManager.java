package de.themoep.settlements.db;

import com.google.common.collect.ImmutableMap;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.invites.SettlementInvite;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.lock.Lock;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.world.SimpleChunkLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class DatabaseManager {
    private static DatabaseManager instance;

    synchronized public static DatabaseManager getInstance() {
        if (instance == null) {
            String type = Config.Storage.TYPE;
            if ("mysql".equalsIgnoreCase(type)) {
                instance = new MySqlDatabase();
                SettlementsPlugin.logDebug("Using MySQL as storage.");
            } else if ("sqlite".equalsIgnoreCase(type)) {
                instance = new SQLiteDatabase();
                SettlementsPlugin.logDebug("Using SQLite as storage.");
            } else {
                SettlementsPlugin.getInstance().getLogger().severe("Invalid database type config value: " + Config.Storage.TYPE);
                SettlementsPlugin.getInstance().getServer().getPluginManager().disablePlugin(SettlementsPlugin.getInstance());
            }
        }
        return instance;
    }

    public abstract void connect() throws Exception;

    public abstract void addNewEmpire(Empire empire);

    public abstract void saveEmpire(Empire empire);

    public abstract void saveEmpires(Collection<Empire> empires);

    public abstract void deleteEmpire(Empire empire);

    public abstract void addNewSettlement(Settlement settlement);

    public abstract void saveSettlement(Settlement settlement);

    public abstract void saveMissionData(Settlement settlement);

    public abstract void saveMissionData(Collection<Settlement> settlements);

    public abstract void deleteSettlement(Settlement settlement);

    public abstract void saveSettlements(Collection<Settlement> values);

    public abstract void unclaimLand(OwnedLand... lands);

    public abstract void unclaimLand(Settlement settlement);

    public abstract void unclaimLand(String worldName);

    public abstract void renameLandWorld(String oldName, String newName);

    public void saveLand(Collection<OwnedLand> lands) {
        saveLand(lands.toArray(new OwnedLand[0]));
    }

    /**
     * To ensure data consistency use {@link LandManager#saveLand(OwnedLand...)}
     */
    public abstract void saveLand(OwnedLand... lands);

    public abstract Collection<OwnedLand> getAllLand();

    public abstract void deleteLand(IdObject idObject);

    public abstract Collection<Settlement> getAllSettlements();

    public abstract Collection<SettlementInvite> getSettlementInvites();

    public abstract <T extends Targetable & DatabaseSerializable, S extends Targetable & DatabaseSerializable> void save(Invite<T,S> invite);

    public abstract <T extends Targetable & DatabaseSerializable, S extends Targetable & DatabaseSerializable> void delete(Invite<T,S> invite);

    public abstract Collection<Empire> getAllEmpires();

    public abstract Collection<Portal> getAllPortals();

    public abstract Resident getResident(int id);

    public abstract Resident getResident(UUID uuid);

    public abstract Resident getResident(String name);

    public void saveResident(Collection<Resident> residents) {
        saveResident(residents.toArray(new Resident[0]));
    }

    public abstract void saveResident(Resident... residents);

    public abstract void renameLockWorld(String oldName, String newName);

    public abstract void savePortal(Portal portal);

    public abstract void savePortals(Portal... portals);

    public abstract void deletePortals(Portal... portals);

    /**
     * Queue locks for saving, does not have to be run async
     * @param locks The locks to save
     */
    public abstract void saveLocks(Lock... locks);

    /**
     * Queue lock deletion, does not have to be run async
     * @param locks The locks to delete
     */
    public abstract void deleteLocks(Lock... locks);

    /**
     * Queue lock deletion, does not have to be run async
     * @param locks The locks to delete
     */
    public void deleteLocks(Collection<Lock> locks) {
        deleteLocks(locks.toArray(new Lock[0]));
    }

    /**
     * Queue lock deletion, does not have to be run async
     * @param chunks The chunks to delete the locks in
     */
    public abstract void deleteLocksInChunks(Collection<? extends SimpleChunkLocation> chunks);

    /**
     * Queue lock deletion, does not have to be run async
     * @param worldName The name of the world to delete the locks in
     */
    public abstract void deleteLocks(String worldName);

    public abstract Collection<Lock> getAllLocks();

    public abstract Collection<Lock> getLocks(SimpleChunkLocation... chunks);

    public Collection<Lock> getLocks(Collection<? extends SimpleChunkLocation> chunks) {
        return getLocks(chunks.toArray(new SimpleChunkLocation[0]));
    }

    public abstract void log(Empire empire, Settlement settlement, Resident resident, String action, Map<String, Long> values);

    public void log(Empire empire, Resident resident, String action, Map<String, Long> values) {
        log(empire, resident.getSettlement(), resident, action, values);
    }

    public void log(Settlement settlement, Resident resident, String action, Map<String, Long> values) {
        log(null, settlement, resident, action, values);
    }

    public void log(Empire empire, Settlement settlement, Resident resident, String action, String type, long amount) {
        log(empire, settlement, resident, action, ImmutableMap.of(type, amount));
    }

    public void log(LandOwner claimsHolder, Resident resident, String action, String type, long amount) {
        if (claimsHolder instanceof Settlement) {
            log(null, (Settlement) claimsHolder, resident, action, ImmutableMap.of(type, amount));
        } else {
            log((Empire) claimsHolder, null, resident, action, ImmutableMap.of(type, amount));
        }
    }

    public void logDeposit(LandOwner landOwner, Resident resident, Map<String, Long> deposited) {
        log(
                landOwner instanceof Empire ? (Empire) landOwner : null,
                landOwner instanceof Empire ? resident.getSettlement() : (Settlement) landOwner,
                resident, "deposit", deposited
        );
    }

    public void logDeposit(Settlement settlement, Resident resident, long deposited) {
        log(null, settlement, resident, "deposit", "cash", deposited);
    }

    public void logDeposit(Settlement settlement, Resident resident, Map<String, Long> deposited) {
        log(null, settlement, resident, "deposit", deposited);
    }

    public void logDeposit(Empire empire, Settlement settlement, Resident resident, long deposited) {
        log(empire, settlement, resident, "deposit", "cash", deposited);
    }

    public void logDeposit(Empire empire, Settlement settlement, Resident resident, Map<String, Long> deposited) {
        log(empire, settlement, resident, "deposit", deposited);
    }

    public void logLevelup(Empire empire, Resident resident, Map<String, Long> removed) {
        log(empire, resident.getSettlement(), resident, "levelup", removed);
    }

    public void logLevelup(Settlement settlement, Resident resident, Map<String, Long> removed) {
        log(null, settlement, resident, "levelup", removed);
    }

    public void logWithdraw(Settlement settlement, Resident resident, long withdrawn) {
        log(null, settlement, resident, "withdraw", "cash", withdrawn);
    }

    public void logWithdraw(Empire empire, Settlement settlement, Resident resident, long withdrawn) {
        log(empire, settlement, resident, "withdraw", "cash", withdrawn);
    }

    public abstract int getLogPageCount(Settlement settlement, Resident resident, LogType type);

    public abstract int getLogPageCount(Empire Empire, Resident resident, LogType type);

    public abstract List<String[]> getLog(Settlement settlement, Resident resident, int page, LogType type);

    public abstract List<String[]> getLog(Empire Empire, Resident resident, int page, LogType type);

    public void save(LandOwner landOwner) {
        landOwner.save();
    }

    public void save(Collection<LandOwner> landOwners) {
        List<Settlement> settlements = new ArrayList<>();
        List<Empire> empires = new ArrayList<>();
        for (LandOwner landOwner : landOwners) {
            if (landOwner instanceof Settlement) {
                settlements.add((Settlement) landOwner);
            } else if (landOwner instanceof Empire) {
                empires.add((Empire) landOwner);
            }
        }
        saveSettlements(settlements);
        saveEmpires(empires);
    }

    public void saveAllies(Collection<Ally> allies) {
        allies.stream().map(Ally::getLandOwner).filter(Objects::nonNull).forEach(DatabaseSerializable::save);
    }

    public abstract void disable();
}

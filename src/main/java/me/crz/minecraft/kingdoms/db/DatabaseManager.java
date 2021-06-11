package me.crz.minecraft.kingdoms.db;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.lock.Lock;
import me.crz.minecraft.kingdoms.portal.Portal;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class DatabaseManager {
    private static DatabaseManager instance;

    synchronized public static DatabaseManager getInstance() {
        if (instance == null) {
            String type = Config.Storage.TYPE;
            if ("mysql".equalsIgnoreCase(type)) {
                instance = new MySqlDatabase();
                KingdomsPlugin.logDebug("Using MySQL as storage.");
            } else if ("sqlite".equalsIgnoreCase(type)) {
                instance = new SQLiteDatabase();
                KingdomsPlugin.logDebug("Using SQLite as storage.");
            } else {
                KingdomsPlugin.getInstance().getLogger().severe("Invalid database type config value: " + Config.Storage.TYPE);
                KingdomsPlugin.getInstance().getServer().getPluginManager().disablePlugin(KingdomsPlugin.getInstance());
            }
        }
        return instance;
    }

    public abstract void connect() throws Exception;

    public abstract void saveEmpire(Empire empire);

    public abstract void saveEmpires(Collection<Empire> empires);

    public abstract void deleteEmpire(Empire village);

    public abstract void saveVillage(Village village);

    public abstract void deleteVillage(Village village);

    public abstract void saveVillages(Collection<Village> values);

    public abstract void unclaimLand(OwnedLand... lands);

    public abstract void unclaimLand(Village village);

    public abstract void unclaimLand(String worldName);

    public abstract void renameLandWorld(String oldName, String newName);

    public void saveLand(Collection<OwnedLand> lands) {
        saveLand(lands.toArray(new OwnedLand[0]));
    }

    /**
     * To ensure data consistency use {@link me.crz.minecraft.kingdoms.land.LandManager#saveLand(OwnedLand...)}
     */
    public abstract void saveLand(OwnedLand... lands);

    public abstract Collection<OwnedLand> getAllLand();

    public abstract void deleteLand(IdObject idObject);

    public abstract Collection<Village> getAllVillages();

    public abstract Collection<Empire> getAllEmpires();

    public abstract Collection<Portal> getAllPortals();

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

    public abstract void log(Empire empire, Village village, Resident resident, String action, Map<String, Long> values);

    public void log(Empire empire, Resident resident, String action, Map<String, Long> values) {
        log(empire, resident.getVillage(), resident, action, values);
    }

    public void log(Village village, Resident resident, String action, Map<String, Long> values) {
        log(null, village, resident, action, values);
    }

    public void log(Empire empire, Village village, Resident resident, String action, String type, long amount) {
        log(empire, village, resident, action, ImmutableMap.of(type, amount));
    }

    public void log(LandOwner claimsHolder, Resident resident, String action, String type, long amount) {
        if (claimsHolder instanceof Village) {
            log(null, (Village) claimsHolder, resident, action, ImmutableMap.of(type, amount));
        } else {
            log((Empire) claimsHolder, null, resident, action, ImmutableMap.of(type, amount));
        }
    }

    public void logDeposit(LandOwner landOwner, Resident resident, Map<String, Long> deposited) {
        log(
                landOwner instanceof Empire ? (Empire) landOwner : null,
                landOwner instanceof Empire ? resident.getVillage() : (Village) landOwner,
                resident, "deposit", deposited
        );
    }

    public void logDeposit(Village village, Resident resident, long deposited) {
        log(null, village, resident, "deposit", "cash", deposited);
    }

    public void logDeposit(Village village, Resident resident, Map<String, Long> deposited) {
        log(null, village, resident, "deposit", deposited);
    }

    public void logDeposit(Empire empire, Village village, Resident resident, long deposited) {
        log(empire, village, resident, "deposit", "cash", deposited);
    }

    public void logDeposit(Empire empire, Village village, Resident resident, Map<String, Long> deposited) {
        log(empire, village, resident, "deposit", deposited);
    }

    public void logLevelup(Empire empire, Resident resident, Map<String, Long> removed) {
        log(empire, resident.getVillage(), resident, "levelup", removed);
    }

    public void logLevelup(Village village, Resident resident, Map<String, Long> removed) {
        log(null, village, resident, "levelup", removed);
    }

    public void logWithdraw(Village village, Resident resident, long withdrawn) {
        log(null, village, resident, "withdraw", "cash", withdrawn);
    }

    public void logWithdraw(Empire empire, Village village, Resident resident, long withdrawn) {
        log(empire, village, resident, "withdraw", "cash", withdrawn);
    }

    public abstract int getLogPageCount(Village village, Resident resident, LogType type);

    public abstract int getLogPageCount(Empire Empire, Resident resident, LogType type);

    public abstract List<String[]> getLog(Village village, Resident resident, int page, LogType type);

    public abstract List<String[]> getLog(Empire Empire, Resident resident, int page, LogType type);

    public void save(LandOwner landOwner) {
        if (landOwner instanceof Village) {
            saveVillage((Village) landOwner);
        } else if (landOwner instanceof Empire) {
            saveEmpire((Empire) landOwner);
        }
    }

    public void save(Collection<LandOwner> landOwners) {
        List<Village> villages = new ArrayList<>();
        List<Empire> empires = new ArrayList<>();
        for (LandOwner landOwner : landOwners) {
            if (landOwner instanceof Village) {
                villages.add((Village) landOwner);
            } else if (landOwner instanceof Empire) {
                empires.add((Empire) landOwner);
            }
        }
        saveVillages(villages);
        saveEmpires(empires);
    }

    public abstract void disable();
}

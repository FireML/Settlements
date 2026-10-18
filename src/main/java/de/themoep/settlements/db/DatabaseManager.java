package de.themoep.settlements.db;

import com.google.common.collect.ImmutableMap;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.invites.SettlementInvite;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.config.Config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class DatabaseManager {
    private static DatabaseManager instance;

    synchronized public static DatabaseManager getInstance() {
        return instance;
    }

    synchronized public static void load() {
        if (instance == null) {
            instance = new SQLiteDatabase();
            SettlementsPlugin.logDebug("Using SQLite as storage.");
        }
    }

    public abstract void connect() throws Exception;

    public abstract void addNewSettlement(Settlement settlement);

    public abstract void saveSettlement(Settlement settlement);

    public void save(Settlement settlement) {
        saveSettlement(settlement);
    }

    public abstract void saveMissionData(Resident resident);

    public abstract void saveResidentMissionData(Collection<Resident> residents);

    public abstract void deleteSettlement(Settlement settlement);

    public abstract void saveSettlements(Collection<Settlement> values);

    public abstract void delete(Settlement settlement, Ally ally);

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

    public abstract <T extends Targetable & DatabaseSerializable, S extends Targetable & DatabaseSerializable> void save(Invite<T, S> invite);

    public abstract <T extends Targetable & DatabaseSerializable, S extends Targetable & DatabaseSerializable> void delete(Invite<T, S> invite);

    public abstract Resident getResident(int id);

    public abstract Resident getResident(UUID uuid);

    public abstract Resident getResident(String name);

    public abstract void saveResidents(Collection<Resident> residents);

    public abstract void saveResident(Resident residents);

    /**
     * Saves the resident information directly to the database, bypassing the queue!
     * Should be run async!
     */
    public abstract void saveResidentNow(Resident resident);

    public abstract void log(Settlement settlement, Resident resident, String action, Map<String, Long> values);

    public void log(Resident resident, String action, Map<String, Long> values) {
        log(resident.getSettlement(), resident, action, values);
    }

    public void log(Settlement settlement, Resident resident, String action, String type, long amount) {
        log(settlement, resident, action, ImmutableMap.of(type, amount));
    }

    public void logDeposit(Settlement settlement, Resident resident, Map<String, Long> deposited) {
        log(
            settlement,
            resident,
            "deposit",
            deposited
        );
    }

    public void logDeposit(Settlement settlement, Resident resident, long deposited) {
        log(settlement, resident, "deposit", "cash", deposited);
    }

    public void logLevelup(Settlement settlement, Resident resident, Map<String, Long> removed) {
        log(settlement, resident, "levelup", removed);
    }

    public void logWithdraw(Settlement settlement, Resident resident, long withdrawn) {
        log(settlement, resident, "withdraw", "cash", withdrawn);
    }

    public abstract int getLogPageCount(Settlement settlement, Resident resident, LogType type);

    public abstract List<String[]> getLog(Settlement settlement, Resident resident, int page, LogType type);

    public void saveAllies(Collection<Ally> allies) {
        allies.stream().map(Ally::getSettlement).filter(Objects::nonNull).forEach(DatabaseSerializable::save);
    }

    public abstract void disable();
}

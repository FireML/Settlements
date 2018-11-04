package me.crz.minecraft.kingdoms.db;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.BonusClaimsHolder;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class DatabaseManager {
    private static DatabaseManager instance;

    public static DatabaseManager getInstance() {
        if (instance == null) {
            String type = KingdomsPlugin.getInstance().getConfig().getString("storage.type");
            if ("mongo".equalsIgnoreCase(type)) {
                instance = new MongoDatabaseManager();
            } else if ("mysql".equalsIgnoreCase(type)) {
                instance = new MySqlDatabaseManager();
            }
        }
        return instance;
    }

    public abstract void connect() throws Exception;

    public abstract void saveEmpire(Empire empire);

    public abstract void deleteEmpire(Empire village);

    public abstract void saveVillage(Village village);

    public abstract void deleteVillage(Village village);

    public abstract void saveVillages(Collection<Village> values);

    public abstract void unclaimLand(OwnedLand... lands);

    public abstract void unclaimLand(Village village);

    public void saveLand(Collection<OwnedLand> lands) {
        saveLand(lands.toArray(new OwnedLand[0]));
    }

    public abstract void saveLand(OwnedLand... lands);

    public abstract Collection<OwnedLand> getAllLand();

    public abstract Collection<Village> getAllVillages();

    public abstract Collection<Empire> getAllEmpires();

    public abstract Resident getResident(UUID uuid);

    public abstract Resident getResident(String name);

    public void saveResident(Collection<Resident> residents) {
        saveResident(residents.toArray(new Resident[0]));
    }

    public abstract void saveResident(Resident... residents);

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

    public void log(BonusClaimsHolder claimsHolder, Resident resident, String action, String type, long amount) {
        if (claimsHolder instanceof Village) {
            log(null, (Village) claimsHolder, resident, action, ImmutableMap.of(type, amount));
        } else {
            log((Empire) claimsHolder, null, resident, action, ImmutableMap.of(type, amount));
        }
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
}

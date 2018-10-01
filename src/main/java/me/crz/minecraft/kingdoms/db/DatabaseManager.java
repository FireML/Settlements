package me.crz.minecraft.kingdoms.db;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Collection;
import java.util.UUID;

public abstract class DatabaseManager {
    private static DatabaseManager instance;

    public DatabaseManager() {
        super();
    }

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

    public abstract Resident getResident(UUID uuid);

    public abstract Resident getResident(String name);

    public void saveResident(Collection<Resident> residents) {
        saveResident(residents.toArray(new Resident[0]));
    }

    public abstract void saveResident(Resident... residents);
}

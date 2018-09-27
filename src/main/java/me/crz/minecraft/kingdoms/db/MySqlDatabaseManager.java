package me.crz.minecraft.kingdoms.db;

import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Collection;
import java.util.UUID;

public class MySqlDatabaseManager extends DatabaseManager {
    @Override
    public void connect() {

    }

    @Override
    public void saveVillage(Village village) {

    }

    @Override
    public void saveVillage(Village village, boolean b) {

    }

    @Override
    public void deleteVillage(Village village) {

    }

    @Override
    public void saveAllVillages(Collection<Village> values) {

    }

    @Override
    public void unclaimLand(OwnedLand... lands) {

    }

    @Override
    public void unclaimLand(Village village) {

    }

    @Override
    public void saveLand(OwnedLand... lands) {

    }

    @Override
    public Collection<OwnedLand> getAllLand() {
        return null;
    }

    @Override
    public Collection<Village> getAllVillages() {
        return null;
    }

    @Override
    public Resident getResident(UUID uuid) {
        return null;
    }

    @Override
    public Resident getResident(String name) {
        return null;
    }

    @Override
    public void saveResident(Resident... residents) {

    }
}

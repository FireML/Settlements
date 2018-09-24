package me.crz.minecraft.kingdoms.command.village;

import java.util.Iterator;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.Village;

enum mode$2
{
    mode$2(final String s, final int n) {
    }
    
    @Override
    public void set(final Boolean bool, final Village village) {
        village.setPvp(bool);
        VillageManager.getInstance().saveVillage(village);
        for (final OwnedLand land : LandManager.getInstance().getClaimedLand(village)) {
            land.setPvp(bool);
            LandManager.getInstance().saveLand(land);
        }
    }
    
    @Override
    public Boolean get(final Village village) {
        return village.isPvp();
    }
    
    @Override
    public /* bridge */ Object get(final Village village) {
        return this.get(village);
    }
}
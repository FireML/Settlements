package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;

enum plotMode$3
{
    plotMode$3(final String s, final int n) {
    }
    
    @Override
    public void set(final Boolean bool, final OwnedLand land) {
        land.setMobSpawning(bool);
        LandManager.getInstance().saveMobData(land, bool);
    }
    
    @Override
    public Boolean get(final OwnedLand land) {
        return land.isMobSpawning();
    }
    
    @Override
    public /* bridge */ Object get(final OwnedLand land) {
        return this.get(land);
    }
}
package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;

enum plotMode$4
{
    plotMode$4(final String s, final int n) {
    }
    
    @Override
    public void set(final Boolean bool, final OwnedLand land) {
        land.setFireSpread(bool);
        LandManager.getInstance().saveLand(land);
    }
    
    @Override
    public Boolean get(final OwnedLand land) {
        return land.isFireSpread();
    }
    
    @Override
    public /* bridge */ Object get(final OwnedLand land) {
        return this.get(land);
    }
}
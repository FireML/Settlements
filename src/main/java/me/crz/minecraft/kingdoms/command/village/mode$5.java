package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.Village;

enum mode$5
{
    mode$5(final String s, final int n) {
    }
    
    @Override
    public void set(final Boolean bool, final Village village) {
        village.setBuff(bool);
        VillageManager.getInstance().saveVillage(village);
    }
    
    @Override
    public Boolean get(final Village village) {
        return village.isBuff();
    }
    
    @Override
    public /* bridge */ Object get(final Village village) {
        return this.get(village);
    }
}
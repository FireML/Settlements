package me.crz.minecraft.kingdoms.command.village;

import java.util.HashMap;
import java.util.Map;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.Village;

enum mode$6
{
    mode$6(final String s, final int n) {
    }
    
    @Override
    public void set(final Boolean bool, final Village village) {
        village.setTeleporter(bool);
        VillageManager.getInstance().saveVillage(village);
    }
    
    @Override
    public Boolean get(final Village village) {
        return village.isTeleporter();
    }
    
    @Override
    public Map<String, Boolean> getOptions() {
        final Map<String, Boolean> options = new HashMap<String, Boolean>();
        options.put("private", true);
        options.put("public", false);
        return options;
    }
    
    @Override
    public /* bridge */ Object get(final Village village) {
        return this.get(village);
    }
}
package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.VillageState;

enum VillageRank$2
{
    VillageRank$2(final String s, final int n) {
    }
    
    @Override
    public String getTag(final VillageState state) {
        if (state.equals(VillageState.VILLAGE)) {
            return ConfigManager.getInstance().getConfig().getString("elder-village-name");
        }
        return ConfigManager.getInstance().getConfig().getString("elder-kingdom-name");
    }
}
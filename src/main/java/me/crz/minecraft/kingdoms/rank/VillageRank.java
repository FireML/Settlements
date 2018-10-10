package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.Rankable;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.VillageState;

public enum VillageRank implements Rankable {
    CHIEF,
    ELDER,
    RECRUITER,
    BUILDER,
    RESIDENT;

    public String getTag(final VillageState state) {
        if (state.equals(VillageState.VILLAGE)) {
            return ConfigManager.getInstance().getMessagesConfig().getString("village.name." + name().toLowerCase());
        }
        return ConfigManager.getInstance().getMessagesConfig().getString("kingdom.name." + name().toLowerCase());
    }
}

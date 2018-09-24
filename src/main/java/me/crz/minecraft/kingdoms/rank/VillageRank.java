package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.VillageState;

public enum VillageRank {
    CHIEF,
    ELDER,
    RECRUITER,
    BUILDER,
    RESIDENT;

    public String getTag(final VillageState state) {
        if (state.equals(VillageState.VILLAGE)) {
            return ConfigManager.getInstance().getConfig().getString(name().toLowerCase() + "-village-name");
        }
        return ConfigManager.getInstance().getConfig().getString(name().toLowerCase() + "-kingdom-name");
    }

    public static boolean hasRank(final VillageRank current, final VillageRank looking) {
        return current.ordinal() <= looking.ordinal();
    }
}

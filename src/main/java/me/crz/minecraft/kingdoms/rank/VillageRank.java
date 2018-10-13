package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Rankable;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;

public enum VillageRank implements Rankable, ConfigKey {
    CHIEF,
    ELDER,
    RECRUITER,
    BUILDER,
    RESIDENT;

    public String getTag(Village village) {
        return Message.get(village.getState().getKey() + ".name." + getKey()).getText();
    }
}

package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Rankable;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.ChatColor;

public enum VillageRank implements Rankable, ConfigKey {
    CHIEF,
    ELDER,
    RECRUITER,
    BUILDER,
    RESIDENT;

    public String getTag(Village village) {
        return village != null ? ChatColor.stripColor(Message.get(village.getState().getKey() + ".rank." + getKey()).getText()) : "";
    }
}

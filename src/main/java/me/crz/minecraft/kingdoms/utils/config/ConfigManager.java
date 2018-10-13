package me.crz.minecraft.kingdoms.utils.config;

import java.io.IOException;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
    private static ConfigManager instance;
    private ConfigAccessor messages;
    private ConfigAccessor levelConf;

    public ConfigManager() {
        super();
        KingdomsPlugin.getInstance().saveDefaultConfig();
        KingdomsPlugin.getInstance().reloadConfig();
        messages = new ConfigAccessor(KingdomsPlugin.getInstance(), "messages.yml");
        messages.saveDefaultConfig();
        messages.reloadConfig();
        levelConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "levelConf.yml");
        levelConf.saveDefaultConfig();
        levelConf.reloadConfig();
    }

    public static void save(File file, FileConfiguration fileConfiguration) {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messages.getConfig();
    }

    public FileConfiguration getLevelConfig() {
        return levelConf.getConfig();
    }

    public FileConfiguration getConfig() {
        return KingdomsPlugin.getInstance().getConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
}

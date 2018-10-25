package me.crz.minecraft.kingdoms.utils.config;

import java.io.IOException;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.Message;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
    private static ConfigManager instance;
    private ConfigAccessor messages;
    private ConfigAccessor levelConf;
    private ConfigAccessor mapConf;

    public ConfigManager() {
        super();
        reload();
    }

    public void reload() {
        KingdomsPlugin.getInstance().saveDefaultConfig();
        KingdomsPlugin.getInstance().reloadConfig();
        messages = new ConfigAccessor(KingdomsPlugin.getInstance(), "messages.yml");
        messages.saveDefaultConfig();
        messages.reloadConfig();
        levelConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "levelConf.yml");
        levelConf.saveDefaultConfig();
        levelConf.reloadConfig();
        mapConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "mapConf.yml");
        mapConf.saveDefaultConfig();
        mapConf.reloadConfig();
        Message.clearCache();
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

    public FileConfiguration getMapConfig() {
        return mapConf.getConfig();
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

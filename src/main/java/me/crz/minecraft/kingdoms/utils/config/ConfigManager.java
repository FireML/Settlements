package me.crz.minecraft.kingdoms.utils.config;

import java.io.IOException;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.utils.IconRpMapping;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
    private static ConfigManager instance;
    private ConfigAccessor messages;
    private ConfigAccessor commands;
    private ConfigAccessor levelConf;
    private ConfigAccessor mapConf;
    private ConfigAccessor settlementPerksConf;
    private ConfigAccessor empirePerksConf;
    private IconRpMapping iconRpMapping;
    private ConfigAccessor cursesConf;
    private ConfigAccessor lastUpkeepConf;

    private ConfigManager() {
        super();
        reload();
    }

    public void reload() {
        KingdomsPlugin.getInstance().saveDefaultConfig();
        KingdomsPlugin.getInstance().reloadConfig();
        messages = new ConfigAccessor(KingdomsPlugin.getInstance(), "messages.yml");
        messages.saveDefaultConfig();
        messages.reloadConfig();
        commands = new ConfigAccessor(KingdomsPlugin.getInstance(), "commands.yml");
        commands.saveDefaultConfig();
        commands.reloadConfig();
        levelConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "levelConf.yml");
        levelConf.saveDefaultConfig();
        levelConf.reloadConfig();
        mapConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "mapConf.yml");
        mapConf.saveDefaultConfig();
        mapConf.reloadConfig();
        settlementPerksConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "settlementPerksConf.yml");
        settlementPerksConf.saveDefaultConfig();
        settlementPerksConf.reloadConfig();
        empirePerksConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "empirePerksConf.yml");
        empirePerksConf.saveDefaultConfig();
        empirePerksConf.reloadConfig();
        cursesConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "curses.yml");
        cursesConf.saveDefaultConfig();
        cursesConf.reloadConfig();
        lastUpkeepConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "lastUpkeep.yml");
        lastUpkeepConf.saveDefaultConfig();
        lastUpkeepConf.reloadConfig();
        iconRpMapping = new IconRpMapping(KingdomsPlugin.getInstance());
        if (KingdomsPlugin.getInstance().isRunning()) {
            UtilChat.getInstance().load();
            VillageManager.getInstance().loadPerks();
            Message.clearCache();
            LockManager.getInstance().loadLockables();
        }
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

    public FileConfiguration getCommandsConfig() {
        return commands.getConfig();
    }

    public FileConfiguration getLevelConfig() {
        return levelConf.getConfig();
    }

    public FileConfiguration getMapConfig() {
        return mapConf.getConfig();
    }

    public FileConfiguration getPerksConfig() {
        return settlementPerksConf.getConfig();
    }

    public FileConfiguration getEmpirePerksConfig() {
        return empirePerksConf.getConfig();
    }

    public FileConfiguration getCursesConfig() {
        return cursesConf.getConfig();
    }

    public FileConfiguration getConfig() {
        return KingdomsPlugin.getInstance().getConfig();
    }

    public IconRpMapping getIconRpMapping() {
        return iconRpMapping;
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void updateLastUpkeep() {
        lastUpkeepConf.getConfig().set("last-upkeep", System.currentTimeMillis());
        lastUpkeepConf.saveConfig();
    }

    public long getLastUpkeep() {
        return lastUpkeepConf.getConfig().getLong("last-upkeep");
    }
}

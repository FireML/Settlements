package me.crz.minecraft.kingdoms.utils.config;

import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
    private static ConfigManager instance;
    public File messagesf;
    public File levelConff;
    public File configf;
    private FileConfiguration messages;
    private FileConfiguration levelConf;
    private FileConfiguration config;

    public ConfigManager() {
        super();
        this.createNewFile(messagesf = new File(KingdomsPlugin.getInstance().getDataFolder(), "messages.yml"), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesf);
        this.createNewFile(levelConff = new File(KingdomsPlugin.getInstance().getDataFolder(), "levelConf.yml"), "levelConf.yml");
        levelConf = YamlConfiguration.loadConfiguration(levelConff);
        configf = new File(KingdomsPlugin.getInstance().getDataFolder(), "config.yml");
        this.createNewFile(levelConff, "config.yml");
        config = YamlConfiguration.loadConfiguration(configf);
    }

    private void createNewFile(final File file, final String fileName) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            KingdomsPlugin.getInstance().saveResource(fileName, false);
        }
    }

    public void save(final File file, final FileConfiguration fileConfiguration) {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messages;
    }

    public FileConfiguration getLevelConfig() {
        return levelConf;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
}

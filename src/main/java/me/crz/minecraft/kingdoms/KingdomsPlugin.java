package me.crz.minecraft.kingdoms;

import me.crz.minecraft.kingdoms.land.LandManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.ThreadManager;
import org.bukkit.Bukkit;

import java.util.Calendar;

import me.crz.minecraft.kingdoms.landflags.LandFlagsUseEvents;
import me.crz.minecraft.kingdoms.landflags.LandFlagsBlockEvents;
import me.crz.minecraft.kingdoms.world.WorldListener;
import me.crz.minecraft.kingdoms.land.LandFlags;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.village.VillageListener;
import me.crz.minecraft.kingdoms.vault.VaultListener;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.OfflinePlayerManager;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import me.crz.minecraft.kingdoms.land.LandPerks;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class KingdomsPlugin extends JavaPlugin {
    private static KingdomsPlugin instance;
    public static Config config;
    private LandPerks lPerks;
    public Economy econ;
    public boolean emgcy;
    public static Chat chat;
    public UtilChat utilChat;

    public KingdomsPlugin() {
        super();
        econ = null;
        emgcy = false;
        utilChat = new UtilChat();
    }

    public void onDisable() {
        OfflinePlayerManager.getInstance().save();
        VillageManager.getInstance().saveAll();
    }

    public void onEnable() {
        instance = this;
        if (!this.setupEconomy() || !this.setupChat()) {
            this.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", this.getDescription().getName()));
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.checkDataFolder();
        config = new Config();
        if (config.load(this.getDataFolder())) {
            try {
                DatabaseManager.getInstance().connect();
            } catch (Exception e) {
                this.getLogger().severe(String.format("[%s] - Disabled due to data.json error!", this.getDescription().getName()));
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
            this.loadManagers();
            this.getLandPerks().runTimer();
            this.getServer().getPluginManager().registerEvents(new VaultListener(), this);
            this.getServer().getPluginManager().registerEvents(new VillageListener(), this);
            this.getServer().getPluginManager().registerEvents(new ChatManager(), this);
            this.getServer().getPluginManager().registerEvents(new LandFlags(), this);
            this.getServer().getPluginManager().registerEvents(new WorldListener(), this);
            this.getServer().getPluginManager().registerEvents(new Advancements(), this);
            this.getServer().getPluginManager().registerEvents(new LandFlagsBlockEvents(), this);
            this.getServer().getPluginManager().registerEvents(new LandFlagsUseEvents(), this);
            this.getServer().getPluginManager().registerEvents(OfflinePlayerManager.getInstance(), this);
            this.upkeep();
            return;
        }
        this.getLogger().severe(String.format("[%s] - Disabled due to data.json error!", this.getDescription().getName()));
        this.getServer().getPluginManager().disablePlugin(this);
    }

    public void upkeep() {
        final Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 1);
        midnight.set(Calendar.DAY_OF_YEAR, midnight.get(Calendar.DAY_OF_YEAR) + 1);
        final long tillMidnight = midnight.getTimeInMillis() - System.currentTimeMillis() - 1L;
        final long ticksTillMidnight = tillMidnight / 50L;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> System.out.println("should be doing upkeep here!"), ticksTillMidnight, 1728000L);
    }

    public LandPerks getLandPerks() {
        if (lPerks == null) {
            lPerks = new LandPerks();
        }
        return lPerks;
    }

    public void loadManagers() {
        ThreadManager.getInstance();
        DatabaseManager.getInstance();
        VillageManager.getInstance();
        LandManager.getInstance();
        ResidentManager.getInstance();
        ConfigManager.getInstance();
        ScoreboardManager.getInstance().setScoreboardPolicy(new ScoreBoard());
        new CommandManager();
    }

    private boolean setupChat() {
        final RegisteredServiceProvider<Chat> rsp = this.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().info("null plugin");
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            this.getLogger().info("null rsp");
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void checkDataFolder() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
    }

    public static KingdomsPlugin getInstance() {
        return instance;
    }

}

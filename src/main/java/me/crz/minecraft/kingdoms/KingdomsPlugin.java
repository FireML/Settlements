package me.crz.minecraft.kingdoms;

import me.crz.minecraft.kingdoms.land.LandManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.ThreadManager;
import org.bukkit.Bukkit;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import me.crz.minecraft.kingdoms.landflags.LandFlagsUseEvents;
import me.crz.minecraft.kingdoms.landflags.LandFlagsBlockEvents;
import me.crz.minecraft.kingdoms.world.WorldListener;
import me.crz.minecraft.kingdoms.land.LandFlags;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.village.VillageListener;
import me.crz.minecraft.kingdoms.vault.VaultListener;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import me.crz.minecraft.kingdoms.land.LandPerks;
import me.crz.minecraft.kingdoms.utils.config.Data;
import org.bukkit.plugin.java.JavaPlugin;

public class KingdomsPlugin extends JavaPlugin implements Listener {
    public static final int UPKEEP = 500;
    private static KingdomsPlugin instance;
    public static Data data;
    private LandPerks lPerks;
    public Economy econ = null;
    public boolean emgcy = false;
    public static Chat chat;
    public UtilChat utilChat = new UtilChat();
    private boolean running = false;
    private String kingdomsWorld = "";

    public void onDisable() {
        if (running) {
            running = false;
            VillageManager.getInstance().saveAll();
        }
    }

    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(this, this);
        this.checkDataFolder();
        data = new Data();
        if (data.load(this.getDataFolder())) {
            ConfigManager.getInstance();
            kingdomsWorld = getConfig().getString("kingdoms-world");
            try {
                DatabaseManager.getInstance().connect();
            } catch (Exception e) {
                this.getLogger().log(Level.SEVERE, "Disabled due to databaser error!", e);
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
            this.upkeep();
            running = true;
            return;
        }
        this.getLogger().log(Level.SEVERE, "Disabled due to data.json error!");
        this.getServer().getPluginManager().disablePlugin(this);
    }

    @EventHandler
    public void on(ServerLoadEvent event) {
        if (!this.setupEconomy()) {
            this.getLogger().log(Level.SEVERE, "Disabled due to missing economy provider!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!this.setupChat()) {
            this.getLogger().log(Level.SEVERE, "Disabled due to missing chat provider! (e.g. a permission plugin)");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    public void upkeep() {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 12);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 1);
        midnight.set(Calendar.DAY_OF_YEAR, midnight.get(Calendar.DAY_OF_YEAR) + 1);
        long tillNoon = midnight.getTimeInMillis() - System.currentTimeMillis() - 1L;
        long ticksTillNoon = tillNoon / 50L;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> VillageManager.getInstance().doUpkeep(), ticksTillNoon, TimeUnit.DAYS.toSeconds(1L) * 20);
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
        ScoreboardManager.getInstance().setScoreboardPolicy(new ScoreBoard());
        new CommandManager();
    }

    private boolean setupChat() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().log(Level.SEVERE, "Vault is not available?");
            return false;
        }
        RegisteredServiceProvider<Chat> rsp = this.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            this.getLogger().log(Level.SEVERE, "No chat service provider registered!");
            return false;
        }
        chat = rsp.getProvider();
        this.getLogger().log(Level.INFO, "Hooked into chat service provider " + chat.getName());
        return chat != null;
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().log(Level.SEVERE, "Vault is not available?");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            this.getLogger().log(Level.SEVERE, "No economy service provider registered!");
            return false;
        }
        econ = rsp.getProvider();
        this.getLogger().log(Level.INFO, "Hooked into economy service provider " + econ.getName());
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

    public boolean isRunning() {
        return running;
    }

    public String getKingdomsWorld() {
        return kingdomsWorld;
    }
}

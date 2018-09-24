package me.crz.minecraft.kingdoms;

import org.bukkit.plugin.RegisteredServiceProvider;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardPolicy;
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
import org.bukkit.event.Listener;
import me.crz.minecraft.kingdoms.vault.VaultListener;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import org.bukkit.plugin.Plugin;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.OfflinePlayerManager;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import me.crz.minecraft.kingdoms.land.LandPerks;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bukkit.plugin.java.JavaPlugin;

public class KingdomsPlugin extends JavaPlugin
{
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
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        this.checkDataFolder();
        config = new Config();
        if (config.load(this.getDataFolder())) {
            try {
                DatabaseManager.connect();
            }
            catch (Exception e) {
                this.getLogger().severe(String.format("[%s] - Disabled due to data.json error!", this.getDescription().getName()));
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                return;
            }
            this.loadManagers();
            this.getLandPerks().runTimer();
            this.getServer().getPluginManager().registerEvents((Listener)new VaultListener(), (Plugin)this);
            this.getServer().getPluginManager().registerEvents((Listener)new VillageListener(), (Plugin)this);
            this.getServer().getPluginManager().registerEvents((Listener)new ChatManager(), (Plugin)this);
            this.getServer().getPluginManager().registerEvents((Listener)new LandFlags(), (Plugin)this);
            this.getServer().getPluginManager().registerEvents((Listener)new WorldListener(), (Plugin)this);
            this.getServer().getPluginManager().registerEvents((Listener)new Advancements(), (Plugin)this);
            this.getServer().getPluginManager().registerEvents((Listener)new LandFlagsBlockEvents(), (Plugin)this);
            this.getServer().getPluginManager().registerEvents((Listener)new LandFlagsUseEvents(), (Plugin)this);
            this.getServer().getPluginManager().registerEvents((Listener)OfflinePlayerManager.getInstance(), (Plugin)this);
            this.upkeep();
            return;
        }
        this.getLogger().severe(String.format("[%s] - Disabled due to data.json error!", this.getDescription().getName()));
        this.getServer().getPluginManager().disablePlugin((Plugin)this);
    }
    
    public void upkeep() {
        final Calendar midnight = Calendar.getInstance();
        midnight.set(11, 0);
        midnight.set(12, 0);
        midnight.set(13, 0);
        midnight.set(14, 1);
        midnight.set(6, midnight.get(6) + 1);
        final long tillMidnight = midnight.getTimeInMillis() - System.currentTimeMillis() - 1L;
        final long ticksTillMidnight = tillMidnight / 50L;
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)this, () -> System.out.println("should be doing upkeep here!"), ticksTillMidnight, 1728000L);
    }
    
    public LandPerks getLandPerks() {
        if (lPerks == null) {
            lPerks = new LandPerks();
        }
        return lPerks;
    }
    
    public void loadManagers() {
        ThreadManager.getInstance();
        VillageManager.getInstance();
        ResidentManager.getInstance();
        ConfigManager.getInstance();
        ScoreboardManager.getInstance().setScoreboardPolicy(new ScoreBoard());
        new CommandManager();
    }
    
    private boolean setupChat() {
        final RegisteredServiceProvider<Chat> rsp = (RegisteredServiceProvider<Chat>)this.getServer().getServicesManager().getRegistration(Chat.class);
        chat = (Chat)rsp.getProvider();
        return chat != null;
    }
    
    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().info("null plugin");
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>)this.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (rsp == null) {
            this.getLogger().info("null rsp");
            return false;
        }
        econ = (Economy)rsp.getProvider();
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
    
    private static /* synthetic */ void lambda$0() {
        System.out.println("should be doing upkeep here!");
    }
}

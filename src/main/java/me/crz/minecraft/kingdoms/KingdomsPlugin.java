package me.crz.minecraft.kingdoms;

import com.griefcraft.lwc.LWC;
import me.crz.minecraft.kingdoms.banish.BanishListener;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireListener;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.integration.LWCIntegration;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.vault.EmpireVaultListener;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.event.Listener;
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
    private static KingdomsPlugin instance;
    public static Data data;
    private LandPerks lPerks;
    public Economy econ = null;

    /**
     * LWC integration
     */
    private LWCIntegration lwcIntegration = null;

    public boolean emgcy = false;
    public static Chat chat;
    public UtilChat utilChat = new UtilChat();
    private boolean running = false;
    private String kingdomsWorld = "";
    private String empiresWorldPrefix = "";

    @Override
    public void onDisable() {
        if (running) {
            running = false;
            VillageManager.getInstance().saveAll();
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        this.checkDataFolder();
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
        data = new Data();
        if (getServer().getPluginManager().isPluginEnabled("LWC")) {
            lwcIntegration = new LWCIntegration(LWC.getInstance());

            getLogger().log(Level.INFO, "Hooked into LWC " + lwcIntegration.getLWC().getPlugin().getDescription().getVersion() + "!");
        }
        if (data.load(this.getDataFolder())) {
            ConfigManager.getInstance();
            kingdomsWorld = getConfig().getString("kingdoms-world");
            empiresWorldPrefix = getConfig().getString("empires-world.prefix");
            try {
                DatabaseManager.getInstance().connect();
            } catch (Exception e) {
                this.getLogger().log(Level.SEVERE, "Disabled due to databaser error!", e);
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
            this.loadManagers();
            this.getLandPerks().runTimer();
            this.getServer().getPluginManager().registerEvents(new BanishListener(), this);
            this.getServer().getPluginManager().registerEvents(new VaultListener(), this);
            this.getServer().getPluginManager().registerEvents(new EmpireVaultListener(), this);
            this.getServer().getPluginManager().registerEvents(new VillageListener(), this);
            this.getServer().getPluginManager().registerEvents(new EmpireListener(), this);
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

    public void upkeep() {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 12);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 1);
        midnight.set(Calendar.DAY_OF_YEAR, midnight.get(Calendar.DAY_OF_YEAR) + 1);
        long tillNoon = midnight.getTimeInMillis() - System.currentTimeMillis() - 1L;
        long ticksTillNoon = tillNoon / 50L;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            LandManager.getInstance().checkUnclaim();
            for (Village village : VillageManager.getInstance().getVillages()) {
                village.doUpkeep();
            }
            for (Empire empire : EmpireManager.getInstance().getEmpires()) {
                empire.doUpkeep();
            }
        }, ticksTillNoon, TimeUnit.DAYS.toSeconds(1L) * 20);
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
        EmpireManager.getInstance();
        VillageManager.getInstance();
        LandManager.getInstance();
        ResidentManager.getInstance();
        ScoreboardManager.getInstance();
        CommandManager.getInstance();
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

    public String getEmpiresWorldPrefix() {
        return empiresWorldPrefix;
    }

    public LWCIntegration getLWCIntegration() {
        return lwcIntegration;
    }
}

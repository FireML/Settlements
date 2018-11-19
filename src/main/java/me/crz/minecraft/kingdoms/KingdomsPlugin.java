package me.crz.minecraft.kingdoms;

import com.griefcraft.lwc.LWC;
import me.crz.minecraft.kingdoms.banish.BanishListener;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireListener;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.integration.ChestShopIntegration;
import me.crz.minecraft.kingdoms.integration.CmiIntegration;
import me.crz.minecraft.kingdoms.integration.IgnoreProvider;
import me.crz.minecraft.kingdoms.integration.LWCIntegration;
import me.crz.minecraft.kingdoms.integration.MultiverseIntegration;
import me.crz.minecraft.kingdoms.lock.LockProvider;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.lock.LockListener;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.ThreadManager;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import me.crz.minecraft.kingdoms.landflags.LandFlagsUseEvents;
import me.crz.minecraft.kingdoms.landflags.LandFlagsBlockEvents;
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
import org.bukkit.scheduler.BukkitTask;

public class KingdomsPlugin extends JavaPlugin implements Listener {
    private static KingdomsPlugin instance;
    public static Data data;
    private LandPerks lPerks;
    public Economy econ = null;
    private MultiverseIntegration multiverse = null;

    private Set<LockProvider> lockProviders = new LinkedHashSet<>();
    private Set<IgnoreProvider> ignoreProviders = new LinkedHashSet<>();

    public boolean emgcy = false;
    public static Chat chat;
    private boolean running = false;
    private String empiresWorldPrefix = "";
    private String empiresWorldSuffix = "";
    private BukkitTask upkeepTask = null;

    @Override
    public void onDisable() {
        if (upkeepTask != null) {
            upkeepTask.cancel();
        }
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
        if (getServer().getPluginManager().isPluginEnabled("Multiverse-Core")) {
            multiverse = new MultiverseIntegration(this);
            this.getLogger().log(Level.INFO, "Hooked into Multiverse-Core v" + multiverse.getVersion());
        }
        if (getServer().getPluginManager().isPluginEnabled("LWC")) {
            LWCIntegration lwcIntegration = new LWCIntegration(LWC.getInstance());
            getLogger().log(Level.INFO, "Hooked into LWC " + lwcIntegration.getLWC().getPlugin().getDescription().getVersion() + "!");
        }
        if (getServer().getPluginManager().isPluginEnabled("ChestShop")) {
            ChestShopIntegration csIntegration = new ChestShopIntegration();
            getLogger().log(Level.INFO, "Hooked into ChestShop " + csIntegration.getChestShop().getDescription().getVersion() + "!");
        }
        if (getServer().getPluginManager().isPluginEnabled("CMI")) {
            CmiIntegration cmiIntegration = new CmiIntegration();
            getLogger().log(Level.INFO, "Hooked into CMI " + cmiIntegration.getCmi().getDescription().getVersion() + "!");
        }
        if (data.load(this.getDataFolder())) {
            ConfigManager.getInstance();
            empiresWorldPrefix = getConfig().getString("empires-world.prefix");
            empiresWorldSuffix = getConfig().getString("empires-world.suffix");
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
            this.getServer().getPluginManager().registerEvents(new VillageListener(), this);
            this.getServer().getPluginManager().registerEvents(new EmpireListener(), this);
            this.getServer().getPluginManager().registerEvents(new LandFlags(), this);
            this.getServer().getPluginManager().registerEvents(new LockListener(), this);
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

    private void upkeep() {
        Runnable runnable = () -> {
            ConfigManager.getInstance().updateLastUpkeep();
            LandManager.getInstance().checkUnclaim();
            for (Village village : VillageManager.getInstance().getVillages()) {
                village.doUpkeep();
            }
            for (Empire empire : EmpireManager.getInstance().getEmpires()) {
                empire.doUpkeep();
            }
        };

        long nextUpkeep = (Utils.getNextUpkeep().getTime() - System.currentTimeMillis()) / 1000;
        upkeepTask = getServer().getScheduler().runTaskTimer(this, runnable, nextUpkeep * 20, TimeUnit.DAYS.toSeconds(1) * 20);

        // check if the last upkeep should have happened while the server was offline
        long lastUpkeep = ConfigManager.getInstance().getLastUpkeep();
        if (lastUpkeep > 0 && lastUpkeep + TimeUnit.DAYS.toMillis(1) < System.currentTimeMillis()) {
            runnable.run();
        }
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
        LockManager.getInstance();
        ScoreboardManager.getInstance();
        CommandManager.getInstance();
        UtilChat.getInstance();
        ChatManager.getInstance();
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
        this.getLogger().log(Level.INFO, "Hooked into chat service provider " + chat.getName() + " " + rsp.getPlugin().getDescription().getVersion());
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
        this.getLogger().log(Level.INFO, "Hooked into economy service provider " + econ.getName() + " " + rsp.getPlugin().getDescription().getVersion());
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

    public String getEmpiresWorldPrefix() {
        return empiresWorldPrefix;
    }

    public String getEmpiresWorldSuffix() {
        return empiresWorldSuffix;
    }

    public Set<LockProvider> getLockProviders() {
        return lockProviders;
    }

    public Set<IgnoreProvider> getIgnoreProviders() {
        return ignoreProviders;
    }

    public MultiverseIntegration getMultiverse() {
        return multiverse;
    }
}

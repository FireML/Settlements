package me.crz.minecraft.kingdoms;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.griefcraft.lwc.LWC;
import me.crz.minecraft.kingdoms.banish.BanishListener;
import me.crz.minecraft.kingdoms.chat.ChatListener;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireListener;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.event.EventListener;
import me.crz.minecraft.kingdoms.event.EventManager;
import me.crz.minecraft.kingdoms.integration.AfkProvider;
import me.crz.minecraft.kingdoms.integration.BreweryIntegration;
import me.crz.minecraft.kingdoms.integration.ChestShopIntegration;
import me.crz.minecraft.kingdoms.integration.CmiIntegration;
import me.crz.minecraft.kingdoms.integration.IgnoreProvider;
import me.crz.minecraft.kingdoms.integration.LWCIntegration;
import me.crz.minecraft.kingdoms.integration.MailProvider;
import me.crz.minecraft.kingdoms.integration.MultiverseIntegration;
import me.crz.minecraft.kingdoms.integration.PlaceholderApiIntegration;
import me.crz.minecraft.kingdoms.integration.ShopProvider;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.lock.LockProvider;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.lock.LockListener;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.portal.PortalListener;
import me.crz.minecraft.kingdoms.portal.PortalManager;
import me.crz.minecraft.kingdoms.rank.RankManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.landflags.LandFlagsUseEvents;
import me.crz.minecraft.kingdoms.landflags.LandFlagsBlockEvents;
import me.crz.minecraft.kingdoms.land.LandFlags;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.village.VillageListener;
import me.crz.minecraft.kingdoms.vault.VaultListener;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
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
    private MultiverseIntegration multiverse = null;
    private PlaceholderApiIntegration placeholderApi = null;

    private Set<LockProvider> lockProviders = new LinkedHashSet<>();
    private Set<IgnoreProvider> ignoreProviders = new LinkedHashSet<>();
    private Set<AfkProvider> afkProviders = new LinkedHashSet<>();
    private Set<ShopProvider> shopProviders = new LinkedHashSet<>();
    private Set<MailProvider> mailProviders = new LinkedHashSet<>();

    public boolean emgcy = false;
    public static Chat chat;
    private boolean running = false;
    private String empiresWorldPrefix = "";
    private String empiresWorldSuffix = "";
    private static final Timer timer = new Timer();
    private LandFlags landFlags;
    private Timing pluginTimings;

    private List<Village> newlyAbandonedVillages = new ArrayList<>();
    private List<Village> newlyDeletedVillages = new ArrayList<>();
    private List<Empire> newlyAbandonedEmpires = new ArrayList<>();
    private List<Empire> newlyDeletedEmpires = new ArrayList<>();

    @Override
    public void onDisable() {
        timer.cancel();
        if (running) {
            running = false;
        }
        DatabaseManager.getInstance().disable();
    }

    @Override
    public void onEnable() {
        pluginTimings = Timings.of(this, "Plugin actions");
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
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderApi = new PlaceholderApiIntegration();
            getLogger().log(Level.INFO, "Hooked into PlaceholderAPI " + placeholderApi.getPlaceholderAPI().getDescription().getVersion() + "!");
        }
        if (getServer().getPluginManager().isPluginEnabled("Brewery")) {
            BreweryIntegration breweryIntegration = new BreweryIntegration();
            getLogger().log(Level.INFO, "Hooked into Brewery " + breweryIntegration.getBrewery().getDescription().getVersion() + "!");
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
            this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
            this.getServer().getPluginManager().registerEvents(new BanishListener(), this);
            this.getServer().getPluginManager().registerEvents(new VaultListener(), this);
            this.getServer().getPluginManager().registerEvents(new VillageListener(), this);
            this.getServer().getPluginManager().registerEvents(new EmpireListener(), this);
            this.getServer().getPluginManager().registerEvents((landFlags = new LandFlags()), this);
            this.getServer().getPluginManager().registerEvents(new LockListener(), this);
            this.getServer().getPluginManager().registerEvents(new LandFlagsBlockEvents(), this);
            this.getServer().getPluginManager().registerEvents(new LandFlagsUseEvents(), this);
            this.getServer().getPluginManager().registerEvents(new EventListener(), this);
            this.getServer().getPluginManager().registerEvents(new PortalListener(), this);

            EmpireManager.getInstance().loadEmpireWorlds();

            this.upkeep();
            running = true;
            return;
        }
        this.getLogger().log(Level.SEVERE, "Disabled due to data.json error!");
        this.getServer().getPluginManager().disablePlugin(this);
    }

    private void upkeep() {
        Runnable runnable = () -> {
            getLogger().log(Level.INFO, "UPKEEP");
            ConfigManager.getInstance().updateLastUpkeep();
            getLogger().log(Level.INFO, "Checking chunk unclaiming...");
            LandManager.getInstance().checkUnclaim();
            getLogger().log(Level.INFO, "Handling settlement taxes and upkeep...");
            newlyAbandonedVillages.clear();
            newlyDeletedVillages.clear();

            for (Village village : new ArrayList<>(VillageManager.getInstance().getVillages())) {
                village.doUpkeep();
            }

            if (!newlyAbandonedVillages.isEmpty()) {
                if (newlyAbandonedVillages.size() > 1) {
                    Message.get("broadcast.villages-abandoned").broadcast("villages", newlyAbandonedVillages.stream()
                            .map(v -> Message.get("broadcast.villages-tag").getReplacedText("state", v.getStateTag(), "name", v.getName()))
                            .collect(Collectors.joining(", ")));
                } else {
                    Message.get("broadcast.village-abandoned").broadcast("state", newlyAbandonedVillages.get(0).getStateTag(), "name", newlyAbandonedVillages.get(0).getName());
                }
            }
            newlyAbandonedVillages.clear();

            if (!newlyDeletedVillages.isEmpty()) {
                if (newlyDeletedVillages.size() > 1) {
                    Message.get("broadcast.villages-deletion").broadcast("villages", newlyDeletedVillages.stream()
                            .map(v -> Message.get("broadcast.villages-tag").getReplacedText("state", v.getStateTag(), "name", v.getName()))
                            .collect(Collectors.joining(", ")));
                } else {
                    Message.get("broadcast.village-deletion").broadcast("state", newlyDeletedVillages.get(0).getStateTag(), "name", newlyDeletedVillages.get(0).getName());
                }
            }
            newlyDeletedVillages.clear();

            getLogger().log(Level.INFO, "Handling empire taxes and upkeep...");
            newlyAbandonedEmpires.clear();
            newlyDeletedEmpires.clear();

            for (Empire empire : new ArrayList<>(EmpireManager.getInstance().getEmpires())) {
                empire.doUpkeep();
            }

            if (!newlyAbandonedEmpires.isEmpty()) {
                if (newlyAbandonedEmpires.size() > 1) {
                    Message.get("broadcast.empires-abandoned").broadcast("empires", newlyAbandonedEmpires.stream()
                            .map(v -> Message.get("broadcast.empires-tag").getReplacedText("state", v.getStateTag(), "name", v.getName()))
                            .collect(Collectors.joining(", ")));
                } else {
                    Message.get("broadcast.village-abandoned").broadcast("state", newlyAbandonedEmpires.get(0).getStateTag(), "name", newlyAbandonedEmpires.get(0).getName());
                }
            }
            newlyAbandonedEmpires.clear();

            if (!newlyDeletedEmpires.isEmpty()) {
                if (newlyDeletedEmpires.size() > 1) {
                    Message.get("broadcast.empires-deletion").broadcast("empires", newlyDeletedEmpires.stream()
                            .map(v -> Message.get("broadcast.empires-tag").getReplacedText("state", v.getStateTag(), "name", v.getName()))
                            .collect(Collectors.joining(", ")));
                } else {
                    Message.get("broadcast.empire-deletion").broadcast("state", newlyDeletedEmpires.get(0).getStateTag(), "name", newlyDeletedEmpires.get(0).getName());
                }
            }
            newlyDeletedEmpires.clear();

            getLogger().log(Level.INFO, "UPKEEP FINISHED!");
        };

        // check if the last upkeep should have happened while the server was offline
        long lastUpkeep = ConfigManager.getInstance().getLastUpkeep();
        if (lastUpkeep > 0 && lastUpkeep + TimeUnit.DAYS.toMillis(1) < System.currentTimeMillis()) {
            getLogger().log(Level.INFO, "Last upkeep was at " + Utils.ISO_TIME_FORMAT.format(new Date(lastUpkeep)) + " (" + lastUpkeep + ") which is longer ago than a day, running upkeep once to catch up!");
            runnable.run();
        } else {
            getLogger().log(Level.INFO, "Last upkeep was at " + Utils.ISO_TIME_FORMAT.format(new Date(lastUpkeep)) + " (" + lastUpkeep + ")");
        }

        scheduleUpkeep(runnable);
    }

    public void addNewlyAbandoned(LandOwner landOwner) {
        if (landOwner instanceof Village) {
            newlyAbandonedVillages.add((Village) landOwner);
        } else if (landOwner instanceof Empire) {
            newlyAbandonedEmpires.add((Empire) landOwner);
        }
    }

    public void addNewlyDeleted(LandOwner landOwner) {
        if (landOwner instanceof Village) {
            newlyDeletedVillages.add((Village) landOwner);
        } else if (landOwner instanceof Empire) {
            newlyDeletedEmpires.add((Empire) landOwner);
        }
    }

    private void scheduleUpkeep(Runnable runnable) {
        Date nextUpkeepDate = Utils.getNextUpkeep();
        getLogger().log(Level.INFO, "Next upkeep: " + Utils.ISO_TIME_FORMAT.format(nextUpkeepDate) + " (" + nextUpkeepDate.getTime() + ")!");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Utils.runSync(() -> {
                    runnable.run();
                    scheduleUpkeep(runnable);
                });
            }
        }, nextUpkeepDate);
    }

    public LandPerks getLandPerks() {
        if (lPerks == null) {
            lPerks = new LandPerks();
        }
        return lPerks;
    }

    public void loadManagers() {
        // Order is important!
        DatabaseManager.getInstance();
        RankManager.getInstance();
        EmpireManager.getInstance();
        VillageManager.getInstance();
        LandManager.getInstance();
        ResidentManager.getInstance();
        PortalManager.getInstance();
        LockManager.getInstance();
        EventManager.getInstance();
        ScoreboardManager.getInstance();
        CommandManager.getInstance();
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

    public Set<AfkProvider> getAfkProviders() {
        return afkProviders;
    }

    public Set<ShopProvider> getShopProviders() {
        return shopProviders;
    }

    public Set<MailProvider> getMailProviders() {
        return mailProviders;
    }

    public MultiverseIntegration getMultiverse() {
        return multiverse;
    }

    public PlaceholderApiIntegration getPlaceholderApi() {
        return placeholderApi;
    }

    public LandFlags getLandFlags() {
        return landFlags;
    }

    public Timing getTiming(String name) {
        return Timings.of(KingdomsPlugin.getInstance(), name, pluginTimings);
    }

    public Timing getAndStartTiming(String name) {
        return Timings.ofStart(KingdomsPlugin.getInstance(), name, pluginTimings);
    }

    public static Timer getTimer() {
        return timer;
    }

    public static void logDebug(String message) {
        if (ConfigManager.getInstance().isDebug()) {
            KingdomsPlugin.getInstance().getLogger().info("[DEBUG] " + message);
        }
    }
}

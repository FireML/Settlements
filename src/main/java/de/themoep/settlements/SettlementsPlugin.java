package de.themoep.settlements;

import de.themoep.settlements.banish.BanishListener;
import de.themoep.settlements.chat.ChatManager;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.TownyCommand;
import de.themoep.settlements.customevents.CustomEventManager;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireListener;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.event.EventManager;
import de.themoep.settlements.integration.*;
import de.themoep.settlements.integration.citizens.CitizensIntegration;
import de.themoep.settlements.integration.denizen.DenizenIntegration;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.*;
import de.themoep.settlements.land.events.listeners.LandChangeListener;
import de.themoep.settlements.land.events.listeners.LandEnterListener;
import de.themoep.settlements.land.events.listeners.LandLeaveListener;
import de.themoep.settlements.lock.LockManager;
import de.themoep.settlements.lock.LockProvider;
import de.themoep.settlements.missions.MissionListener;
import de.themoep.settlements.npc.NpcManager;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.rank.RankManager;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementListener;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.adapter.PaperAdapter;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import uk.firedev.poleislib.libs.Anon8281.universalScheduler.UniversalRunnable;
import uk.firedev.poleislib.libs.Anon8281.universalScheduler.UniversalScheduler;
import uk.firedev.poleislib.libs.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SettlementsPlugin extends JavaPlugin implements Listener {
    private static SettlementsPlugin instance;
    private static TaskScheduler scheduler;
    private LandPerks lPerks;
    private Economy econ = null;
    private CitizensIntegration citizensIntegration = null;
    private MultiverseIntegration multiverse = null;
    private PlaceholderApiIntegration placeholderApi = null;

    private final Set<LockProvider> lockProviders = new LinkedHashSet<>();
    private final Set<IgnoreProvider> ignoreProviders = new LinkedHashSet<>();
    private final Set<AfkProvider> afkProviders = new LinkedHashSet<>();
    private final Set<ShopProvider> shopProviders = new LinkedHashSet<>();
    private final Set<MailProvider> mailProviders = new LinkedHashSet<>();

    public static Chat chat = null;
    private boolean running = false;
    private static final Timer timer = new Timer();
    private LandModeListener landModeListener;
    private PaperAdapter serverAdapter;

    private final List<Settlement> newlyAbandonedSettlements = new ArrayList<>();
    private final List<Settlement> newlyDeletedSettlements = new ArrayList<>();
    private final List<Empire> newlyAbandonedEmpires = new ArrayList<>();
    private final List<Empire> newlyDeletedEmpires = new ArrayList<>();
    private Map<String, Function<Plugin, Boolean>> dependencies = new HashMap<>();

    @Override
    public void onDisable() {
        timer.cancel();
        if (ConfigManager.getInstance().getBossBar() != null) {
            ConfigManager.getInstance().getBossBar().removeAll();
        }
        if (running) {
            running = false;
        }
        if (Config.Storage.SAVE_ON_SHUTDOWN) {
            getLogger().log(Level.INFO, "Saving all resident data...");
            DatabaseManager.getInstance().saveResidents(ResidentManager.getInstance().getCachedResidents());
            DatabaseManager.getInstance().saveResidentMissionData(ResidentManager.getInstance().getCachedResidents());
            getLogger().log(Level.INFO, "Saving all settlement data...");
            DatabaseManager.getInstance().saveSettlements(SettlementManager.getInstance().getSettlements());
            DatabaseManager.getInstance().saveSettlementMissionData(SettlementManager.getInstance().getSettlements());
            for (Settlement settlement : SettlementManager.getInstance().getSettlements()) {
                DatabaseManager.getInstance().saveNpcs(settlement.getNpcData());
            }
            getLogger().log(Level.INFO, "Saving all empire data...");
            DatabaseManager.getInstance().saveEmpires(EmpireManager.getInstance().getEmpires());
            for (Empire empire : EmpireManager.getInstance().getEmpires()) {
                DatabaseManager.getInstance().saveNpcs(empire.getNpcData());
            }
            getLogger().log(Level.INFO, "Saving all lock data...");
            DatabaseManager.getInstance().saveLocks(LockManager.getInstance().getLocks());
            getLogger().log(Level.INFO, "Saving all land data...");
            DatabaseManager.getInstance().saveLand(LandManager.getInstance().getAllLand());
            getLogger().log(Level.INFO, "Saving all portal data...");
            DatabaseManager.getInstance().savePortals(PortalManager.getInstance().getPortals());
        }
        ConfigManager.getInstance().getDataConfig().saveConfig();
        DatabaseManager.getInstance().disable();
    }

    @Override
    public void onEnable() {
        instance = this;
        scheduler = UniversalScheduler.getScheduler(this);
        registerListener(this);
        serverAdapter = new PaperAdapter();
        logDebug("Using improved API provided by Paper.");
        this.checkDataFolder();
        if (!this.setupEconomy()) {
            this.getLogger().log(Level.SEVERE, "Disabled due to missing economy!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.setupChat();

        if (!loadManagers()) {
            return;
        }

        for (Map.Entry<String, Function<Plugin, Boolean>> entry : dependencies.entrySet()) {
            Plugin plugin = getServer().getPluginManager().getPlugin(entry.getKey());
            if (plugin != null && plugin.isEnabled()) {
                handleDependency(plugin);
            }
        }

        registerListener(new SettlementsListener());
        registerListener(new LandLeaveListener());
        registerListener(new LandEnterListener());
        registerListener(new LandChangeListener());

        if (!Config.Chat.CHAT_ONLY) {

            this.getLandPerks().runTimer();
            registerListener(new CustomEventManager());
            registerListener(new BanishListener());
            registerListener(new SettlementListener());
            registerListener(new EmpireListener());
            if (Config.Settlement.Missions.ENABLED || Config.Empire.Missions.ENABLED) {
                registerListener(new MissionListener());
            }
            registerListener(landModeListener = new LandModeListener());
            registerListener(new LandBlockListener());
            registerListener(new LandUseListener());

            EmpireManager.getInstance().loadEmpireWorlds();

            this.upkeep();

            ConfigManager.getInstance().updateBossBar();
        }
        running = true;
        getCommand("towny").setExecutor(new TownyCommand());
    }

    private void upkeep() {
        UniversalRunnable runnable = new UniversalRunnable() {
            @Override
            public void run() {
                getLogger().log(Level.INFO, "UPKEEP");
                ConfigManager.getInstance().updateLastUpkeep();
                getLogger().log(Level.INFO, "Checking chunk unclaiming...");
                LandManager.getInstance().checkUnclaim();
                getLogger().log(Level.INFO, "Handling settlement taxes and upkeep...");
                newlyAbandonedSettlements.clear();
                newlyDeletedSettlements.clear();

                for (Settlement settlement : new ArrayList<>(SettlementManager.getInstance().getSettlements())) {
                    settlement.doUpkeep();
                }

                if (!newlyAbandonedSettlements.isEmpty()) {
                    if (newlyAbandonedSettlements.size() > 1) {
                        Message.get("broadcast.settlements-abandoned").broadcast("settlements", newlyAbandonedSettlements.stream()
                                .map(v -> Message.get("broadcast.settlements-tag").getReplacedText("state", v.getStateTag(), "name", v.getName()))
                                .collect(Collectors.joining(", ")));
                    } else {
                        Message.get("broadcast.settlement-abandoned").broadcast("state", newlyAbandonedSettlements.get(0).getStateTag(), "name", newlyAbandonedSettlements.get(0).getName());
                    }
                }
                newlyAbandonedSettlements.clear();

                if (!newlyDeletedSettlements.isEmpty()) {
                    if (newlyDeletedSettlements.size() > 1) {
                        Message.get("broadcast.settlements-deletion").broadcast("settlements", newlyDeletedSettlements.stream()
                                .map(v -> Message.get("broadcast.settlements-tag").getReplacedText("state", v.getStateTag(), "name", v.getName()))
                                .collect(Collectors.joining(", ")));
                    } else {
                        Message.get("broadcast.settlement-deletion").broadcast("state", newlyDeletedSettlements.get(0).getStateTag(), "name", newlyDeletedSettlements.get(0).getName());
                    }
                }
                newlyDeletedSettlements.clear();

                getLogger().log(Level.INFO, "Handling empire taxes and upkeep...");
                newlyAbandonedEmpires.clear();
                newlyDeletedEmpires.clear();

                for (Empire empire : EmpireManager.getInstance().getEmpires()) {
                    empire.doUpkeep();
                }

                if (!newlyAbandonedEmpires.isEmpty()) {
                    if (newlyAbandonedEmpires.size() > 1) {
                        Message.get("broadcast.empires-abandoned").broadcast("empires", newlyAbandonedEmpires.stream()
                                .map(v -> Message.get("broadcast.empires-tag").getReplacedText("state", v.getStateTag(), "name", v.getName()))
                                .collect(Collectors.joining(", ")));
                    } else {
                        Message.get("broadcast.empire-abandoned").broadcast("state", newlyAbandonedEmpires.get(0).getStateTag(), "name", newlyAbandonedEmpires.get(0).getName());
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
            }
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
        if (landOwner instanceof Settlement settlement) {
            newlyAbandonedSettlements.add(settlement);
        } else if (landOwner instanceof Empire empire) {
            newlyAbandonedEmpires.add(empire);
        }
    }

    public void addNewlyDeleted(LandOwner landOwner) {
        if (landOwner instanceof Settlement settlement) {
            newlyDeletedSettlements.add(settlement);
        } else if (landOwner instanceof Empire empire) {
            newlyDeletedEmpires.add(empire);
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

    @Override
    public void onLoad() {
        registerDependency("CMI", p -> {
            new CmiIntegration();
            return true;
        });
        registerDependency("PlaceholderAPI", p -> {
            placeholderApi = new PlaceholderApiIntegration();
            return true;
        });
        registerDependency("Brewery", p -> {
            new BreweryIntegration();
            return true;
        });
        registerDependency("Citizens", p -> {
            if (!Config.Chat.CHAT_ONLY) {
                citizensIntegration = new CitizensIntegration();
            }
            return true;
        });
        registerDependency("Denizen", p -> {
            if (!Config.Chat.CHAT_ONLY) {
                new DenizenIntegration().init();
            }
            return true;
        });
        registerDependency("Multiverse-Core", p -> {
            if (!Config.Chat.CHAT_ONLY) {
                multiverse = new MultiverseIntegration(this);
            }
            return true;
        });
        registerDependency("LWC", p -> {
            if (!Config.Chat.CHAT_ONLY) {
                new LWCIntegration();
            }
            return true;
        });
        registerDependency("ChestShop", p -> {
            if (!Config.Chat.CHAT_ONLY) {
                new ChestShopIntegration();
            }
            return true;
        });
        registerDependency("UpgradeableHoppers", p -> {
            if (!Config.Chat.CHAT_ONLY) {
                getServer().getPluginManager().registerEvents(new UpgradeableHoppersIntegration(), this);
            }
            return true;
        });
    }

    private void registerDependency(String name, Function<Plugin, Boolean> hook) {
        dependencies.put(name.toLowerCase(Locale.ROOT), hook);
    }

    private void handleDependency(Plugin plugin) {
        Function<Plugin, Boolean> hook = dependencies.get(plugin.getName().toLowerCase(Locale.ROOT));
        if (hook != null) {
            if (hook.apply(plugin)) {
                getLogger().log(Level.INFO, "Hooked into " + plugin.getName() + " " + plugin.getPluginMeta().getVersion());
            } else {
                getLogger().log(Level.WARNING, "Error while trying to hook into " + plugin.getName() + " " + plugin.getPluginMeta().getVersion());
            }
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        handleDependency(event.getPlugin());
    }

    public boolean loadManagers() {
        // Order is important!
        ConfigManager.load();
        DatabaseManager.load();
        try {
            DatabaseManager.getInstance().connect();
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Disabled due to database error!", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        CommandManager.load();
        UseFlagManager.load();
        EmpireManager.load();
        SettlementManager.load();
        RankManager.load();
        ResidentManager.load();
        // Post load actions after residents, settlements and empires are accessible
        EmpireManager.getInstance().postLoad();
        SettlementManager.getInstance().postLoad();
        NpcManager.load();
        if (!Config.Chat.CHAT_ONLY) {
            LandManager.load();
        }
        InviteManager.load();
        if (Config.Portal.ENABLED) {
            PortalManager.load();
        }
        if (Config.Lock.ENABLED) {
            LockManager.load();
        }
        if (ConfigManager.getInstance().getEventsConf().getConfig().getBoolean("enabled")) {
            EventManager.load();
        }
        if (Config.Chat.ENABLED) {
            ChatManager.load();
        }
        return true;
    }

    private boolean setupChat() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.getLogger().log(Level.SEVERE, "Vault is not available?");
            return false;
        }
        RegisteredServiceProvider<Chat> rsp = this.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            this.getLogger().log(Level.WARNING, "No chat service provider registered!");
            return false;
        }
        chat = rsp.getProvider();
        this.getLogger().log(Level.INFO, "Hooked into chat service provider " + chat.getName() + " " + rsp.getPlugin().getPluginMeta().getVersion());
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
        this.getLogger().log(Level.INFO, "Hooked into economy service provider " + econ.getName() + " " + rsp.getPlugin().getPluginMeta().getVersion());
        return econ != null;
    }

    public void checkDataFolder() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
    }

    public static SettlementsPlugin getInstance() {
        return instance;
    }

    public static TaskScheduler getScheduler() { return scheduler; }

    public boolean isRunning() {
        return running;
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

    public CitizensIntegration getCitizens() {
        return citizensIntegration;
    }

    public LandModeListener getLandModeListener() {
        return landModeListener;
    }

    public PaperAdapter getServerAdapter() {
        return serverAdapter;
    }

    public Economy getEconomy() {
        return econ;
    }

    public void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public static Timer getTimer() {
        return timer;
    }

    public static void logDebug(String message) {
        if (Config.DEBUG) {
            SettlementsPlugin.getInstance().getLogger().info("[DEBUG] " + message);
        }
    }
}

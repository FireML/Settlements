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
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.LandBlockListener;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandModeListener;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandPerks;
import de.themoep.settlements.land.LandUseListener;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.land.events.listeners.LandChangeListener;
import de.themoep.settlements.land.events.listeners.LandEnterListener;
import de.themoep.settlements.land.events.listeners.LandLeaveListener;
import de.themoep.settlements.missions.MissionListener;
import de.themoep.settlements.rank.RankManager;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementListener;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.DaisyLib;
import uk.firedev.daisylib.external.vault.VaultWrapper;
import uk.firedev.daisylib.logging.ComponentLogging;
import uk.firedev.daisylib.logging.Logging;
import uk.firedev.daisylib.utils.Scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SettlementsPlugin extends JavaPlugin implements Listener {
    private static SettlementsPlugin instance;
    private LandPerks lPerks;

    private final ComponentLogging logging = Logging.logging(this);

    private boolean running = false;
    private static final Timer timer = new Timer();
    private LandModeListener landModeListener;

    private final List<Settlement> newlyAbandonedSettlements = new ArrayList<>();
    private final List<Settlement> newlyDeletedSettlements = new ArrayList<>();
    private final List<Empire> newlyAbandonedEmpires = new ArrayList<>();
    private final List<Empire> newlyDeletedEmpires = new ArrayList<>();

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
            logging.info("Saving all resident data...");
            DatabaseManager.getInstance().saveResidents(ResidentManager.getInstance().getCachedResidents());
            DatabaseManager.getInstance().saveResidentMissionData(ResidentManager.getInstance().getCachedResidents());
            logging.info("Saving all settlement data...");
            DatabaseManager.getInstance().saveSettlements(SettlementManager.getInstance().getSettlements());
            DatabaseManager.getInstance().saveSettlementMissionData(SettlementManager.getInstance().getSettlements());
            logging.info("Saving all empire data...");
            DatabaseManager.getInstance().saveEmpires(EmpireManager.getInstance().getEmpires());
            logging.info("Saving all land data...");
            DatabaseManager.getInstance().saveLand(LandManager.getInstance().getAllLand());
        }
        ConfigManager.getInstance().getDataConfig().saveConfig();
        DatabaseManager.getInstance().disable();
    }

    @Override
    public void onEnable() {
        instance = this;
        DaisyLib.get().init(this);
        registerListener(this);
        logDebug("Using improved API provided by Paper.");
        this.checkDataFolder();
        if (!VaultWrapper.get().isEconomyAvailable()) {
            throw new IllegalStateException("Vault Economy is not available.");
        }

        if (!loadManagers()) {
            return;
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
        Runnable runnable = () -> {
            logging.info("UPKEEP");
            ConfigManager.getInstance().updateLastUpkeep();
            logging.info("Checking chunk unclaiming...");
            LandManager.getInstance().checkUnclaim();
            logging.info("Handling settlement taxes and upkeep...");
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

            logging.info("Handling empire taxes and upkeep...");
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

            logging.info("UPKEEP FINISHED!");
        };

        // check if the last upkeep should have happened while the server was offline
        long lastUpkeep = ConfigManager.getInstance().getLastUpkeep();
        if (lastUpkeep > 0 && lastUpkeep + TimeUnit.DAYS.toMillis(1) < System.currentTimeMillis()) {
            logging.info("Last upkeep was at " + Utils.ISO_TIME_FORMAT.format(new Date(lastUpkeep)) + " (" + lastUpkeep + ") which is longer ago than a day, running upkeep once to catch up!");
            Scheduling.runTask(runnable);
        } else {
            logging.info("Last upkeep was at " + Utils.ISO_TIME_FORMAT.format(new Date(lastUpkeep)) + " (" + lastUpkeep + ")");
        }

        scheduleUpkeep(runnable);
    }

    public void addNewlyAbandoned(LandOwner<?, ?> landOwner) {
        if (landOwner instanceof Settlement settlement) {
            newlyAbandonedSettlements.add(settlement);
        } else if (landOwner instanceof Empire empire) {
            newlyAbandonedEmpires.add(empire);
        }
    }

    public void addNewlyDeleted(LandOwner<?, ?> landOwner) {
        if (landOwner instanceof Settlement settlement) {
            newlyDeletedSettlements.add(settlement);
        } else if (landOwner instanceof Empire empire) {
            newlyDeletedEmpires.add(empire);
        }
    }

    private void scheduleUpkeep(Runnable runnable) {
        Date nextUpkeepDate = Utils.getNextUpkeep();
        logging.info("Next upkeep: " + Utils.ISO_TIME_FORMAT.format(nextUpkeepDate) + " (" + nextUpkeepDate.getTime() + ")!");
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
        // Register dependencies here
    }

    public boolean loadManagers() {
        // Order is important!
        ConfigManager.load();
        DatabaseManager.load();
        try {
            DatabaseManager.getInstance().connect();
        } catch (Exception e) {
            logging.error("Disabled due to database error!");
            logging.exception(e);
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
        if (!Config.Chat.CHAT_ONLY) {
            LandManager.load();
        }
        InviteManager.load();
        if (ConfigManager.getInstance().getEventsConf().getConfig().getBoolean("enabled")) {
            EventManager.load();
        }
        if (Config.Chat.ENABLED) {
            ChatManager.load();
        }
        return true;
    }

    public void checkDataFolder() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
    }

    public static SettlementsPlugin getInstance() {
        return instance;
    }

    public boolean isRunning() {
        return running;
    }

    public LandModeListener getLandModeListener() {
        return landModeListener;
    }

    public Economy getEconomy() {
        return VaultWrapper.get().getEconomy();
    }

    public Chat getChat() {
        return VaultWrapper.get().getChat();
    }

    public void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public static Timer getTimer() {
        return timer;
    }

    public static void logDebug(String message) {
        if (Config.DEBUG) {
            instance.logging.info("[DEBUG] " + message);
        }
    }

    public @NonNull ComponentLogging getLogging() {
        return this.logging;
    }

}

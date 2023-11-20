package de.themoep.settlements.event;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.*;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.libs.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import uk.firedev.poleislib.utils.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class EventManager implements ConfigMessages {

    private static EventManager instance;

    private Map<String, Event> events = new LinkedHashMap<>();
    private Map<String, Reward> rewards = new LinkedHashMap<>();
    private List<String> automaticEvents = new ArrayList<>();

    private Map<String, TimerTask> nextEventTasks = new LinkedHashMap<>();

    private String bossBarFormat;
    private BarColor bossBarColor = BarColor.YELLOW;
    private BarStyle bossBarStyle;
    private Set<BarFlag> bossBarFlags = Collections.EMPTY_SET;

    private Event activeEvent = null;
    private TimerTask activeBarTask = null;
    private BossBar activeBar = null;
    private long activeEventStart = -1;

    private Map<IdObject, EventData> eventData = new HashMap<>();
    private Settlement first = null;
    private MyScheduledTask firstAnnounceTask = null;
    private Map<IdObject, Set<String>> settlementRewards = new HashMap<>();


    private void setup() {
        loadConfig();
        SettlementsPlugin.getInstance().registerListener(new EventListener());
        SettlementsPlugin.logDebug("Loaded EventManager with " + events.size() + " events (" + automaticEvents.size() + " automatic events) and " + rewards.size() + " rewards");
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public String name() {
        return "events";
    }

    public void loadConfig() {
        Configuration config = ConfigManager.getInstance().getEventsConf().getConfig();
        bossBarFormat = config.getString("bossbar.format");
        try {
            bossBarColor = BarColor.valueOf(config.getString("bossbar.color").toUpperCase());
        } catch (IllegalArgumentException e) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("bossbar.color") + " is not a valid boss bar color!");
        }try {
            bossBarStyle = BarStyle.valueOf(config.getString("bossbar.style").toUpperCase());
        } catch (IllegalArgumentException e) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("bossbar.style") + " is not a valid boss bar style!");
        }
        bossBarFlags = config.getStringList("bossbar.flags").stream()
                .map(s -> {
                    try {
                        return BarFlag.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, s + " is not a valid boss bar flag!");
                    }
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        rewards.clear();
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        for (String rewardId : rewardsSection.getKeys(false)) {
            try {
                rewards.put(rewardId.toLowerCase(), new Reward(rewardId, rewardsSection.getConfigurationSection(rewardId)));
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not load reward " + rewardId + ": " + e.getMessage());
            }
        }

        events.clear();
        ConfigurationSection eventsSection = config.getConfigurationSection("events");
        for (String eventName : eventsSection.getKeys(false)) {
            Event event = new Event(eventName, eventsSection.getConfigurationSection(eventName));
            events.put(eventName.toLowerCase(), event);
            for (List<String> rewardList : event.getRewards().values()) {
                for (String reward : rewardList) {
                    if (getReward(reward) == null) {
                        SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Event " + eventName + " defines a reward " + reward + " that doesn't exist.");
                    }
                }
            }
        }

        automaticEvents = config.getStringList("automatic-events.events");
        for (Iterator<String> it = automaticEvents.iterator(); it.hasNext();) {
            String eventName = it.next();
            if (getEvent(eventName) == null) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Event " + eventName + " defined in the automatic events list does not exist?");
                it.remove();
            }
        }

        nextEventTasks.values().forEach(TimerTask::cancel);
        nextEventTasks.clear();
        scheduleAutomaticEvents();
    }

    private void scheduleAutomaticEvents() {
        if (automaticEvents.isEmpty()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, "No automatic events configured.");
            return;
        }

        Configuration config = ConfigManager.getInstance().getEventsConf().getConfig();
        TimeZone timeZone = TimeZone.getTimeZone(config.getString("automatic-events.timezone"));

        for (String timeString : config.getStringList("automatic-events.times")) {
            String[] parts = timeString.split(":", 2);
            if (parts.length != 2) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Automatic event time " + timeString + " is not in a valid format!");
                continue;
            }
            try {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                Calendar dateTime = Calendar.getInstance();
                dateTime.setTimeZone(timeZone);
                if (dateTime.get(Calendar.HOUR_OF_DAY) > hours || dateTime.get(Calendar.HOUR_OF_DAY) == hours && dateTime.get(Calendar.MINUTE) >= minutes) {
                    dateTime.set(Calendar.DAY_OF_YEAR, dateTime.get(Calendar.DAY_OF_YEAR) + 1);
                }
                dateTime.set(Calendar.HOUR_OF_DAY, hours);
                dateTime.set(Calendar.MINUTE, minutes);
                dateTime.set(Calendar.SECOND, 0);
                dateTime.set(Calendar.MILLISECOND, 1);

                Event event = getRandomEventAt(dateTime.get(Calendar.YEAR), dateTime.get(Calendar.DAY_OF_YEAR), hours, minutes);
                if (event == null) {
                    SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not schedule event as " + timeString + " at " + dateTime.getTime() + " as the one selected doesn't exist?");
                    continue;
                }
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Utils.runSync(() -> {
                            if (activeEvent == null) {
                                startEvent(event);
                            } else {
                                SettlementsPlugin.getInstance().getLogger().log(Level.INFO, "Automatic event " + event.getId() + " was not run at " + dateTime.getTime() + " as the event " + activeEvent.getId() + " was already running.");
                            }
                            nextEventTasks.remove(timeString);
                            if (nextEventTasks.isEmpty()) {
                                scheduleAutomaticEvents();
                            }
                        });
                    }
                };
                nextEventTasks.put(timeString, task);
                SettlementsPlugin.getTimer().schedule(task, dateTime.getTime());
                SettlementsPlugin.logDebug("Scheduled event " + event.getId() + "/" + event.getTitle() + " as " + timeString + " event at " + dateTime.getTime());
            } catch (NumberFormatException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Automatic event time " + timeString + " is not in a valid format! " + e.getMessage());
            }
        }
    }

    private Event getRandomEventAt(int year, int dayOfYear, int hours, int minutes) {
        if (automaticEvents.isEmpty()) {
            return null;
        }
        long seed = minutes + TimeUnit.HOURS.toMinutes(hours) + TimeUnit.DAYS.toMinutes(dayOfYear) + TimeUnit.DAYS.toMinutes((year - 2000L) * 365);
        return getEvent(automaticEvents.get(new Random(seed).nextInt(automaticEvents.size())));
    }

    public Event getActiveEvent() {
        return activeEvent;
    }

    public BossBar getActiveBar() {
        return activeBar;
    }

    public Event getEvent(String eventId) {
        return events.get(eventId.toLowerCase());
    }

    public Collection<Event> getEvents() {
        return events.values();
    }

    public void startEvent(Event event) {
        List<Settlement> settlements = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            Resident res = ResidentManager.getInstance().getResident(p);
            if (!settlements.contains(res.getSettlement())) {
                settlements.add(res.getSettlement());
            }
        }
        if (settlements.isEmpty() || settlements.size() > 3) {
            return;
        }
        if (activeBarTask != null) {
            activeBarTask.cancel();
        }
        if (activeBar != null) {
            activeBar.setVisible(false);
            activeBar.removeAll();
            activeBar = null;
        }
        settlementRewards.clear();
        eventData.clear();
        activeEvent = event;
        activeEventStart = System.currentTimeMillis();
        long length = TimeUnit.SECONDS.toMillis(event.getTime());
        long eventEnd = activeEventStart + length;
        getMessage("started").broadcast(
                "title", activeEvent.getTitle(),
                "id", activeEvent.getId(),
                "duration", UtilTime.format((double) activeEvent.getTime())
        );

        activeBar = Bukkit.createBossBar(StringUtils.parseColors(bossBarFormat,
                "title", activeEvent.getTitle(),
                "id", activeEvent.getId(),
                "timeleft", UtilTime.format((double) activeEvent.getTime())
        ), bossBarColor, bossBarStyle, bossBarFlags.toArray(new BarFlag[0]));
        activeBar.setProgress(1);
        Bukkit.getOnlinePlayers().forEach(activeBar::addPlayer);
        activeBar.setVisible(true);
        activeBarTask = Utils.runRepeating(() -> {
            if (activeEvent != event || activeEventStart < 0 || System.currentTimeMillis() > eventEnd) {
                if (activeEvent == null && activeBar != null) {
                    activeBar.setVisible(false);
                    activeBar.removeAll();
                    activeBar = null;
                }
                if (activeBarTask != null) {
                    activeBarTask.cancel();
                    activeBarTask = null;
                }
            } else {
                long left = eventEnd - System.currentTimeMillis();
                activeBar.setTitle(StringUtils.parseColors(bossBarFormat,
                        "title", activeEvent.getTitle(),
                        "id", activeEvent.getId(),
                        "timeleft", UtilTime.format((double) Math.round(left / 1000.0))
                ));
                activeBar.setProgress(left / (double) length);
            }
        }, 1, 1, TimeUnit.SECONDS);

        Utils.runLater(this::finishActiveEvent, activeEvent.getTime(), TimeUnit.SECONDS);
    }

    public boolean finishActiveEvent() {
        if (activeEvent == null) {
            return false;
        }
        if (first != null) {
            getMessage("ended").broadcast(
                    "title", activeEvent.getTitle(),
                    "id", activeEvent.getId(),
                    "state", first.getStateTag(),
                    "name", first.getName(),
                    "amount", String.valueOf(eventData.get(first.getId()).getTotalAmount())
            );
            List<EventData> top = EventManager.getInstance().getTop(eventData.size());
            for (int i = 0; i < top.size(); i++) {
                EventData eventData = top.get(i);
                Settlement settlement = eventData.getSettlement();
                int place = i + 1;
                getMessage("stats").tell(settlement.getOnlineResidents(),
                        "title", activeEvent.getTitle(),
                        "id", activeEvent.getId(),
                        "state", settlement.getStateTag(),
                        "name", settlement.getName(),
                        "place", String.valueOf(place),
                        "amount", String.valueOf(eventData.getTotalAmount())
                );
                List<String> rewards = activeEvent.getRewards().getOrDefault(String.valueOf(place), activeEvent.getRewards().get("rest"));
                if (rewards != null) {
                    settlementRewards.put(settlement.getId(), new LinkedHashSet<>(rewards));
                    getMessage("reward-info").tell(settlement.getOnlineResidents(),
                            "title", activeEvent.getTitle(),
                            "id", activeEvent.getId(),
                            "state", settlement.getStateTag(),
                            "name", settlement.getName(),
                            "place", String.valueOf(place),
                            "amount", String.valueOf(eventData.getTotalAmount())
                    );
                }
            }
        }

        if (activeBarTask != null) {
            activeBarTask.cancel();
        }
        if (activeBar != null) {
            activeBar.setVisible(false);
            activeBar.removeAll();
            activeBar = null;
        }
        eventData.clear();
        activeEventStart = -1;
        activeEvent = null;
        first = null;
        return true;
    }

    public Set<String> getRewards(Settlement settlement) {
        return settlementRewards.get(settlement.getId());
    }

    public Reward getReward(String id) {
        return rewards.get(id.toLowerCase());
    }

    public static EventManager getInstance() {
        if (!ConfigManager.getInstance().getEventsConf().getConfig().getBoolean("enabled")) {
            throw new IllegalStateException("Tried to get EventManager while events were disabled!");
        }
        return instance;
    }

    public static void load() {
        if (!ConfigManager.getInstance().getEventsConf().getConfig().getBoolean("enabled")) {
            throw new IllegalStateException("Tried to load EventManager while events were disabled!");
        }
        if (instance == null) {
            instance = new EventManager();
            instance.setup();
        }
    }

    public void handle(Player player, Event.Action action, String... data) {
        handle(player, action, 1, data);
    }

    public void handle(Player player, Event.Action action, int amount, String... data) {
        if (data.length < 1) {
            throw new IllegalArgumentException("At least one data entry is required!");
        }
        if (activeEvent == null
                || !activeEvent.getWorlds().contains(player.getWorld().getName().toLowerCase())
                || !activeEvent.getActions().containsKey(action)) {
            return;
        }

        Resident resident = ResidentManager.getInstance().getResident(player);
        if (!resident.hasSettlement()) {
            return;
        }

        String matches = null;
        for (String d : data) {
            if (activeEvent.getActions().containsEntry(action, d.toLowerCase())) {
                matches = d.toLowerCase();
                break;
            }
        }

        if (matches == null) {
            return;
        }

        EventData ed = eventData.computeIfAbsent(resident.getSettlement().getId(), EventData::new);
        ed.increment(matches, amount);

        if (first == null || ed.getTotalAmount() > eventData.get(first.getId()).getTotalAmount()) {
            first = resident.getSettlement();
            if (firstAnnounceTask != null) {
                firstAnnounceTask.cancel();
            }
            firstAnnounceTask = Utils.runLater(() -> {
                getMessage("first").broadcast(
                        "title", activeEvent.getTitle(),
                        "id", activeEvent.getId(),
                        "state", first.getStateTag(),
                        "name", first.getName(),
                        "amount", String.valueOf(ed.getTotalAmount())
                );
                firstAnnounceTask = null;
            }, 100);
        }
    }

    public void giveReward(Reward reward, Player player, Settlement settlement) {
        player.getInventory().addItem(reward.getItems().toArray(new ItemStack[0])).values()
                .forEach(i -> {
                    Item item = player.getLocation().getWorld().dropItem(player.getLocation(), i);
                    SettlementsPlugin.getInstance().getServerAdapter().markOwned(item, player);
                });
        for (String command : reward.getCommands()) {
            Map<String, String> replacements = Utils.toMap(
                "claimer", player.getName(),
                "claimerid", player.getUniqueId().toString(),
                "settlement", settlement.getName(),
                "settlementid", String.valueOf(settlement.getId().value())
            );
            if (command.contains("onlineresident") || command.contains("onlineresidentid")) {
                for (Resident resident : settlement.getOnlineResidents()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.parsePlaceholders(command, Utils.addAll(replacements,
                            "onlineresident", resident.getName(),
                            "onlineresidentid", resident.getUUID().toString()
                    )));
                }
            } else if (command.contains("resident") || command.contains("residentid")) {
                for (Resident resident : settlement.getResidents()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.parsePlaceholders(command, Utils.addAll(replacements,
                            "resident", resident.getName(),
                            "residentid", resident.getUUID().toString()
                    )));
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.parsePlaceholders(command, replacements));
            }
        }
        if (reward.getMoney() > 0) {
            settlement.addToBank(reward.getMoney());
            DatabaseManager.getInstance().saveSettlement(settlement);
        }
    }

    public List<EventData> getTop(int amount) {
        return eventData.values().stream().sorted(Comparator.reverseOrder()).limit(amount).collect(Collectors.toList());
    }

    public class EventData implements Comparable<EventData>{
        private final IdObject id;
        private int totalAmount = 0;
        private Map<String, Integer> counts = new HashMap<>();

        public EventData(IdObject id) {
            this.id = id;
        }

        public void increment(String data, int amount) {
            counts.put(data.toLowerCase(), getCount(data) + amount);
            totalAmount++;
        }

        public Settlement getSettlement() {
            return SettlementManager.getInstance().getFromId(id);
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public Map<String, Integer> getCounts() {
            return counts;
        }

        public int getCount(String data) {
            return counts.getOrDefault(data.toLowerCase(), 0);
        }

        @Override
        public int compareTo(EventData o) {
            return Integer.compare(totalAmount, o.getTotalAmount());
        }
    }
}

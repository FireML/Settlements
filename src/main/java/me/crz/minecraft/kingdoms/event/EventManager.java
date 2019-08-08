package me.crz.minecraft.kingdoms.event;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
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
    private long activeEventStart = -1;

    private Map<IdObject, EventData> eventData = new HashMap<>();
    private Village first = null;
    private BukkitTask firstAnnounceTask = null;
    private Map<IdObject, Set<String>> villageRewards = new HashMap<>();


    private EventManager() {
        loadConfig();
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
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("bossbar.color") + " is not a valid boss bar color!");
        }try {
            bossBarStyle = BarStyle.valueOf(config.getString("bossbar.style").toUpperCase());
        } catch (IllegalArgumentException e) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("bossbar.style") + " is not a valid boss bar style!");
        }
        bossBarFlags = config.getStringList("bossbar.flags").stream()
                .map(s -> {
                    try {
                        return BarFlag.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, s + " is not a valid boss bar flag!");
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
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not load reward " + rewardId + ": " + e.getMessage());
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
                        KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Event " + eventName + " defines a reward " + reward + " that doesn't exist.");
                    }
                }
            }
        }

        automaticEvents = config.getStringList("automatic-events.events");
        for (Iterator<String> it = automaticEvents.iterator(); it.hasNext();) {
            String eventName = it.next();
            if (getEvent(eventName) == null) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Event " + eventName + " defined in the automatic events list does not exist?");
                it.remove();
            }
        }

        nextEventTasks.values().forEach(TimerTask::cancel);
        nextEventTasks.clear();
        scheduleAutomaticEvents();
    }

    private void scheduleAutomaticEvents() {
        if (automaticEvents.isEmpty()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.INFO, "No automatic events configured.");
            return;
        }

        Configuration config = ConfigManager.getInstance().getEventsConf().getConfig();
        TimeZone timeZone = TimeZone.getTimeZone(config.getString("automatic-events.timezone"));

        for (String timeString : config.getStringList("automatic-events.times")) {
            String[] parts = timeString.split(":", 2);
            if (parts.length != 2) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Automatic event time " + timeString + " is not in a valid format!");
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
                    KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not schedule event as " + timeString + " at " + dateTime.getTime().toString() + " as the one selected doesn't exist?");
                    continue;
                }
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Utils.runSync(() -> {
                            if (activeEvent == null) {
                                startEvent(event);
                            } else {
                                KingdomsPlugin.getInstance().getLogger().log(Level.INFO, "Automatic event " + event.getId() + " was not run at " + dateTime.getTime().toString() + " as the event " + activeEvent.getId() + " was already running.");
                            }
                            nextEventTasks.remove(timeString);
                            if (nextEventTasks.isEmpty()) {
                                scheduleAutomaticEvents();
                            }
                        });
                    }
                };
                nextEventTasks.put(timeString, task);
                KingdomsPlugin.getTimer().schedule(task, dateTime.getTime());
                KingdomsPlugin.logDebug("Scheduled event " + event.getId() + "/" + event.getTitle() + " as " + timeString + " event at " + dateTime.getTime().toString());
            } catch (NumberFormatException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Automatic event time " + timeString + " is not in a valid format! " + e.getMessage());
            }
        }
    }

    private Event getRandomEventAt(int year, int dayOfYear, int hours, int minutes) {
        if (automaticEvents.isEmpty()) {
            return null;
        }
        long seed = minutes + TimeUnit.HOURS.toMinutes(hours) + TimeUnit.DAYS.toMinutes(dayOfYear) + TimeUnit.DAYS.toMinutes((year - 2000) * 365);
        return getEvent(automaticEvents.get(new Random(seed).nextInt(automaticEvents.size())));
    }

    public Event getActiveEvent() {
        return activeEvent;
    }

    public Event getEvent(String eventId) {
        return events.get(eventId.toLowerCase());
    }

    public Collection<Event> getEvents() {
        return events.values();
    }

    public void startEvent(Event event) {
        if (activeBarTask != null) {
            activeBarTask.cancel();
        }
        villageRewards.clear();
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

        BossBar bossBar = Bukkit.createBossBar(MessageUtils.replaceColorCodes(MessageUtils.replacePlaceholders(bossBarFormat,
                "title", activeEvent.getTitle(),
                "id", activeEvent.getId(),
                "timeleft", UtilTime.format((double) activeEvent.getTime())
        )), bossBarColor, bossBarStyle, bossBarFlags.toArray(new BarFlag[0]));
        bossBar.setProgress(0);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
        bossBar.setVisible(true);
        activeBarTask = Utils.runRepeating(() -> {
            if (activeEvent != event || activeEventStart < 0 || System.currentTimeMillis() > eventEnd) {
                bossBar.setVisible(false);
                bossBar.removeAll();
                activeBarTask.cancel();
                activeBarTask = null;
            } else {
                long left = eventEnd - System.currentTimeMillis();
                bossBar.setTitle(MessageUtils.replaceColorCodes(MessageUtils.replacePlaceholders(bossBarFormat,
                        "title", activeEvent.getTitle(),
                        "id", activeEvent.getId(),
                        "timeleft", UtilTime.format((double) Math.round(left / 1000.0))
                )));
                bossBar.setProgress(1 - left / (double) length);
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
            List<EventManager.EventData> top = EventManager.getInstance().getTop(eventData.size());
            for (int i = 0; i < top.size(); i++) {
                EventManager.EventData eventData = top.get(i);
                Village village = eventData.getVillage();
                int place = i + 1;
                getMessage("stats").tell(village.getOnlineResidents(),
                        "title", activeEvent.getTitle(),
                        "id", activeEvent.getId(),
                        "state", village.getStateTag(),
                        "name", village.getName(),
                        "place", String.valueOf(place),
                        "amount", String.valueOf(eventData.getTotalAmount())
                );
                List<String> rewards = activeEvent.getRewards().getOrDefault(String.valueOf(place), activeEvent.getRewards().get("rest"));
                if (rewards != null) {
                    villageRewards.put(first.getId(), new LinkedHashSet<>(rewards));
                    getMessage("reward-info").tell(first.getOnlineResidents(),
                            "title", activeEvent.getTitle(),
                            "id", activeEvent.getId(),
                            "state", first.getStateTag(),
                            "name", first.getName(),
                            "place", String.valueOf(place),
                            "amount", String.valueOf(eventData.getTotalAmount())
                    );
                }
            }
        }

        eventData.clear();
        activeEventStart = -1;
        activeEvent = null;
        first = null;
        return true;
    }

    public Set<String> getRewards(Village village) {
        return villageRewards.get(village.getId());
    }

    public Reward getReward(String id) {
        return rewards.get(id.toLowerCase());
    }

    synchronized public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void handle(Player player, Event.Action action, String... data) {
        if (data.length < 1) {
            throw new IllegalArgumentException("At least one data entry is required!");
        }
        if (activeEvent == null
                || !activeEvent.getWorlds().contains(player.getWorld().getName().toLowerCase())
                || !activeEvent.getActions().containsKey(action)) {
            return;
        }

        Resident resident = ResidentManager.getInstance().getResident(player);
        if (!resident.hasVillage()) {
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

        EventData ed = eventData.computeIfAbsent(resident.getVillage().getId(), id -> new EventData(id));
        ed.increment(matches);

        if (first == null || ed.getTotalAmount() > eventData.get(first.getId()).getTotalAmount()) {
            first = resident.getVillage();
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

    public void giveReward(Reward reward, Player player, Village village) {
        player.getInventory().addItem(reward.getItems().toArray(new ItemStack[0])).values()
                .forEach(i -> {
                    Item item = player.getLocation().getWorld().dropItem(player.getLocation(), i);
                    item.setOwner(player.getUniqueId());
                    item.setCanMobPickup(false);
                });
        for (String command : reward.getCommands()) {
            Map<String, String> replacements = Utils.toMap(
                "claimer", player.getName(),
                "claimerid", player.getUniqueId().toString(),
                "village", village.getName(),
                "villageid", String.valueOf(village.getId().value())
            );
            if (command.contains("onlineresident") || command.contains("onlineresidentid")) {
                for (Resident resident : village.getOnlineResidents()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtils.replacePlaceholders(command, Utils.addAll(replacements,
                            "onlineresident", resident.getName(),
                            "onlineresidentid", resident.getUUID().toString()
                    )));
                }
            } else if (command.contains("resident") || command.contains("residentid")) {
                for (Resident resident : village.getResidents()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtils.replacePlaceholders(command, Utils.addAll(replacements,
                            "resident", resident.getName(),
                            "residentid", resident.getUUID().toString()
                    )));
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtils.replacePlaceholders(command, replacements));
            }
        }
        if (reward.getMoney() > 0) {
            village.addToBank(reward.getMoney());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
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

        public void increment(String data) {
            counts.put(data.toLowerCase(), getCount(data) + 1);
            totalAmount++;
        }

        public Village getVillage() {
            return VillageManager.getInstance().getVillageFromId(id);
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

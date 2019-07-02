package me.crz.minecraft.kingdoms.event;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private Map<String, TimerTask> nextEventTasks = new LinkedHashMap<>();

    private String bossBarFormat;
    private BarColor bossBarColor = BarColor.YELLOW;
    private BarStyle bossBarStyle;
    private Set<BarFlag> bossBarFlags = Collections.EMPTY_SET;

    private Event activeEvent = null;
    private BukkitTask activeBarTask = null;
    private long activeEventStart = -1;

    private Map<IdObject, EventData> eventData = new HashMap<>();
    private Village first = null;


    private EventManager() {
        loadConfig();
        // TODO: Timer & Event starting/stopping
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
                        KingdomsPlugin.getInstance().getLogger().log(Level.WARNING,s + " is not a valid boss bar flag!");
                    }
                    return null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        ConfigurationSection eventsSection = config.getConfigurationSection("events");
        for (String eventName : eventsSection.getKeys(false)) {
            events.put(eventName.toLowerCase(), new Event(eventName, eventsSection.getConfigurationSection(eventName)));
        }

        // TODO: Rewards?

        nextEventTasks.values().forEach(TimerTask::cancel);
        nextEventTasks.clear();
        scheduleAutomaticEvents();
    }

    private void scheduleAutomaticEvents() {
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
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Utils.runSync(() -> {
                            startEvent(event);
                            nextEventTasks.remove(timeString);
                            if (nextEventTasks.isEmpty()) {
                                scheduleAutomaticEvents();
                            }
                        });
                    }
                };
                nextEventTasks.put(timeString, task);
                KingdomsPlugin.getTimer().schedule(task, dateTime.getTime());
                KingdomsPlugin.logDebug("Scheduled event " + event.getId() + "/" + event.getTitle() + " as " + timeString + " event at " + dateTime.toString());
            } catch (NumberFormatException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Automatic event time " + timeString + " is not in a valid format! " + e.getMessage());
            }
        }
    }

    private Event getRandomEventAt(int year, int dayOfYear, int hours, int minutes) {
        long seed = minutes + TimeUnit.HOURS.toMinutes(hours) + TimeUnit.DAYS.toMinutes(dayOfYear) + TimeUnit.DAYS.toMinutes((year - 2000) * 365);
        return getEvents().stream().skip(new Random(seed).nextInt(getEvents().size())).findFirst().get();
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
        activeBarTask = Bukkit.getScheduler().runTaskTimer(KingdomsPlugin.getInstance(), () -> {
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
                        "timeleft", UtilTime.format(left)
                )));
                bossBar.setProgress(left / (double) length);
            }
        }, 20, 20);

        Utils.runLater(this::finishActiveEvent, activeEvent.getTime(), TimeUnit.SECONDS);
    }

    public boolean finishActiveEvent() {
        if (activeEvent == null) {
            return false;
        }
        if (activeBarTask != null) {
            activeBarTask.cancel();
        }
        if (first != null) {
            getMessage("ended").broadcast(
                    "title", activeEvent.getTitle(),
                    "id", activeEvent.getId(),
                    "state", first.getStateTag(),
                    "name", first.getName(),
                    "amount", String.valueOf(eventData.get(first.getId()).getTotalAmount())
            );
            for (Map.Entry<IdObject, EventData> entry : eventData.entrySet()) {
                Village village = VillageManager.getInstance().getVillageFromId(entry.getKey());
                getMessage("stats").tell(village.getOnlineResidents(),
                        "title", activeEvent.getTitle(),
                        "id", activeEvent.getId(),
                        "state", village.getStateTag(),
                        "name", village.getName(),
                        "amount", String.valueOf(entry.getValue().getTotalAmount())
                );
            }
            getMessage("winner-info").tell(first.getOnlineResidents(),
                    "title", activeEvent.getTitle(),
                    "id", activeEvent.getId(),
                    "state", first.getStateTag(),
                    "name", first.getName(),
                    "amount", String.valueOf(eventData.get(first.getId()).getTotalAmount())
            );
        }

        activeEventStart = -1;
        activeEvent = null;
        first = null;
        return true;
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

        EventData ed = eventData.computeIfAbsent(resident.getVillage().getId(), id -> new EventData());
        ed.increment(matches);

        if (first == null || ed.getTotalAmount() > eventData.get(first.getId()).getTotalAmount()) {
            first = resident.getVillage();
            getMessage("first").broadcast(
                    "title", activeEvent.getTitle(),
                    "id", activeEvent.getId(),
                    "state", first.getStateTag(),
                    "name", first.getName(),
                    "amount", String.valueOf(ed.getTotalAmount())
            );
        }
    }

    private class EventData {
        private int totalAmount = 0;
        private Map<String, Integer> counts = new HashMap<>();

        public void increment(String data) {
            counts.put(data.toLowerCase(), getCount(data) + 1);
            totalAmount++;
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
    }
}

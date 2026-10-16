package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import uk.firedev.daisylib.builders.TitleBuilder;
import uk.firedev.daisylib.utils.Scheduling;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Utils {
    public final static DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###");
    public final static DecimalFormat SUFFIX_FORMAT = new DecimalFormat("#.##");
    private final static DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#.##");
    private final static char[] SUFFIXES = {'k', 'M', 'B', 'T', 'P', 'E'};
    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat();
    public final static SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat ISO_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    public static final UUID ZERO_UUID = new UUID(0, 0);
    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();
    private static final Set<EntityType> SPECIAL_HOSTILE = EnumSet.of(
            EntityType.SHULKER,
            EntityType.SLIME,
            EntityType.PHANTOM,
            EntityType.HOGLIN,
            EntityType.MAGMA_CUBE,
            EntityType.GHAST,
            EntityType.VEX,
            EntityType.EVOKER_FANGS
    );
    private static final Set<EntityType> SPECIAL_PASSIVE = EnumSet.of(
            EntityType.SNOW_GOLEM,
            EntityType.VILLAGER,
            EntityType.SQUID,
            EntityType.IRON_GOLEM
    );

    public static String toPercentage(double v) {
        return PERCENTAGE_FORMAT.format(v * 100);
    }

    public static boolean isWorldClaimable(World world) {
        return !Config.Claiming.DISABLED_WORLDS.contains(world.getName())
                || EmpireManager.getInstance().getEmpireFromWorld(world) != null;
    }

    public static int getTeleportDelay(String type) {
        try {
            Field f = Config.Teleport.Delay.class.getField(type.toUpperCase());
            return (int) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            SettlementsPlugin.getInstance().getLogging().exception(e);
        }
        return 0;
    }

    public static int getTeleportCooldown(String type) {
        try {
            Field f = Config.Teleport.Cooldown.class.getField(type.toUpperCase());
            return (int) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            SettlementsPlugin.getInstance().getLogging().exception(e);
        }
        return 0;
    }

    public static TextColor getChannelColor(ChatChannel channel, ChatColorType type) {
        return switch (type) {
            case PREFIX ->
                    ColorUtils.getColor(ConfigManager.getInstance().getConfig().getString("chat.channel-prefix-colors." + channel.getKey()).toLowerCase(), NamedTextColor.WHITE);
            case MESSAGE ->
                    ColorUtils.getColor(ConfigManager.getInstance().getConfig().getString("chat.channel-colors." + channel.getKey()).toLowerCase(), NamedTextColor.WHITE);
        };
    }

    public static boolean hasPlotMode(PlotMode mode, String type) {
        return ConfigManager.getInstance().getConfig().isSet("modes." + type + "." + mode.getKey());
    }

    public static boolean getPlotMode(PlotMode mode, String type) {
        return ConfigManager.getInstance().getConfig().getBoolean("modes." + type + "." + mode.getKey(), mode.getDefault());
    }

    public static boolean settlementHasInvite(Settlement settlement, Empire joinEmpire) {
        return InviteCache.getPendingEmpireInvites().containsKey(settlement.getId()) && InviteCache.getPendingEmpireInvites().get(settlement.getId()).contains(joinEmpire.getId());
    }

    public static int getMaxDrunkeness(ChatChannel channel) {
        return ConfigManager.getInstance().getConfig().getInt("brewery.block-channels." + channel.getKey(), 9001);
    }

    public static void addDisabledWorld(World world) {
        Collection<String> temp = Config.Claiming.DISABLED_WORLDS;
        if (!temp.contains(world.getName())) {
            temp.add(world.getName());
            ConfigManager.getInstance().savePluginConfig();
        }
    }

    public static void removeDisabledWorld(World world) {
        Collection<String> temp = Config.Claiming.DISABLED_WORLDS;
        if (temp.contains(world.getName())) {
            temp.remove(world.getName());
            ConfigManager.getInstance().savePluginConfig();
        }
    }

    /**
     * Make sure to run something not on the primary server thread.
     * A new task is started if this is the primary thread, if not the current one is used!
     * @param run What to run
     * @return The ScheduledTask that was started or null if we are already on an async thread
     */
    public static void runAsync(Runnable run) {
        Bukkit.getAsyncScheduler().runNow(SettlementsPlugin.getInstance(), _ -> run.run());
    }

    /**
     * Make sure to run something on the primary server thread.
     * A new task is started if this is not the primary thread, if not the main one is used!
     * @param run What to run
     * @return The ScheduledTask that was started or null if we are already on the main thread
     */
    public static void runSync(Runnable run) {
        Scheduling.runTask(run);
    }

    /**
     * Run something in the next tick on the primary server thread.
     * @param run What to run
     * @return The ScheduledTask that was started
     */
    public static void runNextTick(Runnable run) {
        Scheduling.runTask(run);
    }

    /**
     * Run something later on the primary server thread.
     * @param run   What to run
     * @param ticks The ticks to wait
     * @return The ScheduledTask that was started
     */
    public static ScheduledTask runLater(Runnable run, long ticks) {
        return Scheduling.runTaskLater(run, ticks);
    }

    /**
     * Run something later on the primary server thread.
     * @param run       What to run
     * @param amount    The amount of time to wait
     * @param unit      The time unit the amount is in
     * @return The TimerTask that was started
     */
    public static TimerTask runLater(Runnable run, long amount, TimeUnit unit) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runSync(run);
            }
        };
        SettlementsPlugin.getTimer().schedule(task, unit.toMillis(amount));
        return task;
    }

    /**
     * Run something later on the primary server thread.
     * @param run       What to run
     * @param delay     The amount of time to wait before first run
     * @param period    The amount of time between each run
     * @param unit      The time unit the amount is in
     * @return The TimerTask that was started
     */
    public static TimerTask runRepeating(Runnable run, long delay, long period, TimeUnit unit) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runSync(run);
            }
        };
        SettlementsPlugin.getTimer().scheduleAtFixedRate(task, unit.toMillis(delay), unit.toMillis(period));
        return task;
    }

    public static UUID parseUuid(String uuidStr) {
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static <R extends Enum<R>> R parseEnum(Class<R> enumClass, String name) {
        return parseEnum(enumClass, name, null);
    }

    public static <R extends Enum<R>> R parseEnum(Class<R> enumClass, String name, R defaultValue) {
        if (name == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, name.replace('-', '_').toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void catchSync() {
        if (SettlementsPlugin.getInstance().isRunning() && Bukkit.isPrimaryThread()) {
            SettlementsPlugin.getInstance().getLogging().warn("Doing an operation on the main thread that should be done async!");
            SettlementsPlugin.getInstance().getLogging().exception(new Throwable());
        }
    }

    public static String trim(String string, int length) {
        if (string == null) {
            return null;
        }
        if (string.length() < length) {
            return string;
        }
        String[] split = string.split(" ");
        if (split.length <= 1) {
            return string.substring(0, length);
        }
        int tooLong = string.length() - length;
        boolean shortened = true;
        while (shortened && tooLong > 0) {
            shortened = false;
            for (int i = 0; i < split.length && tooLong > 0; i++) {
                if (split[i].length() > 3) {
                    split[i] = split[i].substring(0, split[i].length() - 1);
                    tooLong--;
                    shortened = true;
                }
            }
        }
        if (tooLong == 0) {
            return String.join(" ", split);
        }
        return String.join(" ", split).substring(0, length);
    }

    public static String wrapLore(String lore) {
        return wrap(lore, 48);
    }

    /**
     * Wrap a string to lines with a certain length.
     * @param string The string to wrap
     * @param length The line length
     * @return The string wrapped with all lines wrapped to the length. Tries to take colors/formatting into account.
     */
    public static String wrap(String string, int length) {
        if (StringUtils.stripColors(string).length() < length) {
            return string;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : string.split("\n")) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            if (StringUtils.stripColors(line).length() < length) {
                sb.append(line);
            } else {
                String[] parts = line.split(" ");
                StringBuilder part1 = new StringBuilder(parts[0]);
                int i = 1;
                for (; i < parts.length && StringUtils.stripColors(part1 + " " + parts[i]).length() < length; i++) {
                    part1.append(" ").append(parts[i]);
                }
                StringBuilder part2 = new StringBuilder(i < parts.length ? parts[i] : "");
                for (i += 1; i < parts.length; i++) {
                    part2.append(" ").append(parts[i]);
                }
                while (!part1.isEmpty() && part1.charAt(part1.length() - 1) == '§'
                        || part1.length() > 1 && part1.charAt(part1.length() - 2) == '§') {
                    if (part1.charAt(part1.length() - 1) == '§') {
                        part2.insert(0, '§');
                        part1 = new StringBuilder(part1.substring(0, part1.length() - 1));
                    } else if (part1.charAt(part1.length() - 2) == '§') {
                        part2.insert(0, '§').insert(1, part1.charAt(part1.length() - 1));
                        part1 = new StringBuilder(part1.substring(0, part1.length() - 2));
                    }
                }
                sb.append(part1).append("\n").append(wrap(part2.toString(), length));
            }
        }
        return sb.toString();
    }

    public static Date getNextUpkeep() {
        Calendar nextUpkeep = Calendar.getInstance();
        nextUpkeep.setTimeZone(TimeZone.getTimeZone(Config.Upkeep.TIMEZONE));
        if (nextUpkeep.get(Calendar.HOUR_OF_DAY) >= Config.Upkeep.HOUR) {
            nextUpkeep.set(Calendar.DAY_OF_YEAR, nextUpkeep.get(Calendar.DAY_OF_YEAR) + 1);
        }
        nextUpkeep.set(Calendar.HOUR_OF_DAY, Config.Upkeep.HOUR);
        nextUpkeep.set(Calendar.MINUTE, 0);
        nextUpkeep.set(Calendar.SECOND, 0);
        nextUpkeep.set(Calendar.MILLISECOND, 1);
        return nextUpkeep.getTime();
    }

    public static long timeTillNextUpkeep() {
        return getNextUpkeep().getTime() - System.currentTimeMillis();
    }

    /**
     * Teleports a player. Checks cooldown and delay bypass permissions
     * @return false if teleport occurred instantly and no wait task was started
     */
    public static boolean teleport(ConfigMessages messages, Player player, Location loc, String type, String... replacements) {
        if (loc == null || loc.getWorld() == null) {
            messages.getError("destination-unknown").tell(player, "type", loc == null ? "location" : "world");
            return false;
        }
        return teleport(messages, player, type, () -> loc.getWorld().getChunkAtAsync(loc).thenAccept(c -> {
            if (c == null) {
                messages.getError("destination-obstructed").tell(player);
                SettlementsPlugin.logDebug("Could not load chunk at " + loc);
                return;
            }
            Block block = loc.getBlock();
            Block up;
            while (block.getY() < block.getWorld().getMaxHeight() && ((up = block.getRelative(BlockFace.UP)).isPassable() || !block.isPassable())) {
                block = up;
            }
            Block down;
            while (!(down = block.getRelative(BlockFace.DOWN)).getType().isSolid() && down.getY() > down.getWorld().getMinHeight()) {
                block = down;
            }
            if (!down.getType().isSolid()) {
                messages.getError("destination-obstructed").tell(player);
                SettlementsPlugin.logDebug("Location " + loc + " is has no floor");
                return;
            }

            player.sendBlockChange(down.getLocation(), down.getBlockData());
            loc.setY(block.getY());
            player.teleportAsync(loc).thenAccept(success -> {
                String path = success ? "success" : "fail";
                messages.getMessage(path).tell(player, replacements);
                TitleBuilder.create()
                        .withTitle(messages.getMessage(path + "-title.title").getReplacedComponent(replacements))
                        .withSubtitle(messages.getMessage(path + "-title.subtitle").getReplacedComponent(replacements))
                        .send(player);
                ComponentUtils.sendActionBar(messages.getMessage(path + "-title.actionbar").getReplacedComponent(replacements), player);
                if (!success) {
                    SettlementsPlugin.logDebug("Failed to teleport " + player.getName() + " to " + loc);
                }
            });
        }));
    }

    /**
     * Teleports a player. Checks cooldown and delay bypass permissions
     * @return false if teleport occurred instantly and no wait task was started
     */
    public static boolean teleport(ConfigMessages messages, Player player, String type, Runnable runnable) {
        boolean bypassDelay = player.hasPermission("settlements.bypass.teleport-delay");
        if (bypassDelay && !player.hasPermission("settlements.bypass.teleport-cooldown")) {
            long cooldown = TELEPORT_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L)
                    + Utils.getTeleportCooldown(type) * 1000L
                    - System.currentTimeMillis();
            if (cooldown > 0) {
                messages.getError("teleport-cooldown").tell(player, "cooldown", UtilTime.format(cooldown));
                return true;
            } else {
                TELEPORT_COOLDOWNS.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        return ExecuteMoveUtil.getInstance().execute(player, runnable, bypassDelay ? 0 : Utils.getTeleportDelay(type), "Teleport", messages);
    }

    public static void teleportToSpawn(Player target) {
        String command = Config.Teleport.SPAWN_COMMAND;
        if (!command.isEmpty()) {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.parsePlaceholders(command, "player", target.getName()))) {
                SettlementsPlugin.getInstance().getLogging().warn("Could not execute spawn teleport command? Is " + command + " a valid command?");
            }
        } else {
            target.teleportAsync(Bukkit.getWorlds().getFirst().getSpawnLocation());
        }
    }

    public static boolean parseBoolean(String arg) throws IllegalArgumentException {
        switch (arg.toLowerCase()) {
            case "true", "on", "allow", "yes" -> {
                return true;
            }
            case "false", "off", "deny", "disallow", "no" -> {
                return false;
            }
        }
        throw new IllegalArgumentException(arg + " is not a valid boolean!");
    }

    public static String[] toArray(Map<String, String> map) {
        String[] array = new String[map.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            array[i] = e.getKey();
            i++;
            array[i] = e.getValue();
            i++;
        }
        return array;
    }

    public static Map<String, String> toMap(String... args) {
        return addAll(new LinkedHashMap<>(), args);
    }

    public static Map<String, String> addAll(Map<String, String> map, String... args) {
        for (int i = 0; i+1 < args.length; i+=2) {
            map.put(args[i], args[i+1]);
        }
        return map;
    }

    public static String withSuffix(long count) {
        if (count < 1000L) {
            return String.valueOf(count);
        }
        int exp = (int) (Math.log(count) / Math.log(1000.0));
        return SUFFIX_FORMAT.format(((long) Math.floor(count / Math.pow(1000.0, exp) * 100)) / 100d) + (SUFFIXES.length >= exp ? SUFFIXES[exp - 1] : "k^" + exp);
    }

    public static boolean isHostile(Entity entity) {
        return entity instanceof Monster || SPECIAL_HOSTILE.contains(entity.getType());
    }

    public static boolean isHostile(EntityType entityType) {
        return entityType.getEntityClass() != null && Monster.class.isAssignableFrom(entityType.getEntityClass()) || SPECIAL_HOSTILE.contains(entityType);
    }

    public static boolean isPassive(Entity entity) {
        return entity instanceof Animals || entity instanceof WaterMob || entity instanceof Ambient || SPECIAL_PASSIVE.contains(entity.getType());
    }

    public static <T> T getOrDefault(Map<String, Object> map, String key, T def) {
        String[] parts = key.split("\\.");
        for (int i = 0; i + 1 < parts.length; i++) {
            map = (Map<String, Object>) map.getOrDefault(parts[i], Collections.emptyMap());
        }
        return (T) map.getOrDefault(parts[parts.length - 1], def);
    }

    public static <T> T poll(Collection<T> coll) {
        if (coll.isEmpty()) {
            return null;
        }
        Iterator<T> it = coll.iterator();
        T r = it.next();
        it.remove();
        return r;
    }

    public static String filterAllCurses(String message) {
        return ConfigManager.getInstance().getToggleableCurses().filterCurses(ConfigManager.getInstance().getForcedCurses().filterCurses(message));
    }

    public static boolean containsAnyCurses(String message) {
        return ConfigManager.getInstance().getToggleableCurses().containsCurses(message) || ConfigManager.getInstance().getForcedCurses().containsCurses(message);
    }

    public static long getLong(Object o) {
        return switch (o) {
            case null -> 0;
            case Long l -> l;
            case Number number -> number.longValue();
            default -> Long.parseLong(String.valueOf(o));
        };
    }

    public static int getInt(Object o) {
        return switch (o) {
            case null -> 0;
            case Integer i -> i;
            case Number number -> number.intValue();
            default -> Integer.parseInt(String.valueOf(o));
        };
    }

    public static double getDouble(Object o) {
        return switch (o) {
            case null -> 0;
            case Double v -> v;
            case Number number -> number.doubleValue();
            default -> Double.parseDouble(String.valueOf(o));
        };
    }

    public static float getFloat(Object o) {
        return switch (o) {
            case null -> 0;
            case Float v -> v;
            case Number number -> number.floatValue();
            default -> Float.parseFloat(String.valueOf(o));
        };
    }

    public static boolean getBoolean(Object o) {
        return switch (o) {
            case null -> false;
            case Boolean b -> (boolean) o;
            case Number number -> number.longValue() != 0;
            default -> Boolean.parseBoolean(String.valueOf(o));
        };
    }

    public static String getPlayerName(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            return player.getName();
        }

        Resident resident = ResidentManager.getInstance().getResidentIfPresent(playerId);
        if (resident != null) {
            return resident.getName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        if (offlinePlayer.getName() != null) {
            return offlinePlayer.getName();
        }

        return playerId.toString();
    }

    public static <R> void addAmount(Map<R, Long> m, R r, Long amount) {
        m.put(r, m.getOrDefault(r, (long) 0) + amount);
    }
}

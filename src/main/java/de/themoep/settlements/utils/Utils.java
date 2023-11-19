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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import uk.firedev.poleislib.Loggers;
import uk.firedev.poleislib.builders.TitleBuilder;
import uk.firedev.poleislib.libs.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import uk.firedev.poleislib.utils.ColorUtils;
import uk.firedev.poleislib.utils.ComponentUtils;
import uk.firedev.poleislib.utils.StringUtils;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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
            EntityType.SNOWMAN,
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
            Loggers.logException(e, SettlementsPlugin.getInstance().getLogger());
        }
        return 0;
    }

    public static int getTeleportCooldown(String type) {
        try {
            Field f = Config.Teleport.Cooldown.class.getField(type.toUpperCase());
            return (int) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Loggers.logException(e, SettlementsPlugin.getInstance().getLogger());
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
     * @return The MyScheduledTask that was started or null if we are already on an async thread
     */
    public static MyScheduledTask runAsync(Runnable run) {
        return runAsync(run, false);
    }

    /**
     * Make sure to run something not on the primary server thread.
     * @param run What to run
     * @param force Whether we should force run this async (only works if the plugin is still running)
     * @return The MyScheduledTask that was started or null if we are already on an async thread
     */
    public static MyScheduledTask runAsync(Runnable run, boolean force) {
        if ((force || Bukkit.isPrimaryThread()) && SettlementsPlugin.getInstance().isRunning() && SettlementsPlugin.getInstance().isEnabled()) {
            return SettlementsPlugin.getScheduler().runTaskAsynchronously(run);
        } else {
            run.run();
        }
        return null;
    }

    /**
     * Make sure to run something on the primary server thread.
     * A new task is started if this is not the primary thread, if not the main one is used!
     * @param run What to run
     * @return The MyScheduledTask that was started or null if we are already on the main thread
     */
    public static MyScheduledTask runSync(Runnable run) {
        if (!Bukkit.isPrimaryThread()) {
            return SettlementsPlugin.getScheduler().runTask(run);
        } else {
            run.run();
        }
        return null;
    }

    /**
     * Run something in the next tick on the primary server thread.
     * @param run What to run
     * @return The MyScheduledTask that was started
     */
    public static MyScheduledTask runNextTick(Runnable run) {
        return SettlementsPlugin.getScheduler().runTask(run);
    }

    /**
     * Run something later on the primary server thread.
     * @param run   What to run
     * @param ticks The ticks to wait
     * @return The MyScheduledTask that was started
     */
    public static MyScheduledTask runLater(Runnable run, long ticks) {
        return SettlementsPlugin.getScheduler().runTaskLater(run, ticks);
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
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Doing an operation on the main thread that should be done async!", new Throwable());
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
        return teleport(messages, player, type, () -> SettlementsPlugin.getInstance().getServerAdapter().getChunkAtAsync(loc).thenAccept(c -> {
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
            SettlementsPlugin.getInstance().getServerAdapter().teleportAsync(player, loc).thenAccept(success -> {
                String path = success ? "success" : "fail";
                messages.getMessage(path).tell(player, replacements);
                new TitleBuilder()
                        .withTitle(messages.getMessage(path + "-title.title").getComponent())
                        .withSubtitle(messages.getMessage(path + "-title.subtitle").getComponent())
                        .send(player, replacements);
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
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not execute spawn teleport command? Is " + command + " a valid command?");
            }
        } else {
            SettlementsPlugin.getInstance().getServerAdapter().teleportAsync(target, Bukkit.getWorlds().get(0).getSpawnLocation());
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
        if (o == null) {
            return 0;
        } else if (o instanceof Long) {
            return (Long) o;
        } else if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        return Long.parseLong(String.valueOf(o));
    }

    public static int getInt(Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        return Integer.parseInt(String.valueOf(o));
    }

    public static double getDouble(Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return Double.parseDouble(String.valueOf(o));
    }

    public static float getFloat(Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        }
        return Float.parseFloat(String.valueOf(o));
    }

    public static boolean getBoolean(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof Boolean) {
            return (boolean) o;
        } else if (o instanceof Number) {
            return ((Number) o).longValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(o));
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

    public static boolean callEvent(Event event) {
        SettlementsPlugin.getInstance().getServer().getPluginManager().callEvent(event);
        if (event instanceof Cancellable) {
            return ((Cancellable) event).isCancelled();
        }
        return false;
    }

    public static <R> void addAmount(Map<R, Long> m, R r, Long amount) {
        m.put(r, m.getOrDefault(r, (long) 0) + amount);
    }
}

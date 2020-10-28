package me.crz.minecraft.kingdoms.utils;

import io.papermc.lib.PaperLib;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.chat.ChatColorType;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Utils {
    public final static DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###");
    public final static DecimalFormat SUFFIX_FORMAT = new DecimalFormat("#.##");
    private final static char[] SUFFIXES = {'k', 'M', 'B', 'T', 'P', 'E'};
    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat();
    public static final SimpleDateFormat ISO_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    public static final UUID ZERO_UUID = new UUID(0, 0);
    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();
    private static Map<String, Integer> LEVEL_CHUNKS = new HashMap<>();
    private static Map<String, Integer> LEVEL_OUTPOSTS = new HashMap<>();
    private static Map<String, Integer> LEVEL_UPKEEP = new HashMap<>();
    private static final Set<EntityType> SPECIAL_HOSTILE = EnumSet.of(
            EntityType.SHULKER,
            EntityType.SLIME,
            EntityType.PHANTOM
    );
    private static final Set<EntityType> SPECIAL_PASSIVE = EnumSet.of(
            EntityType.SNOWMAN,
            EntityType.VILLAGER,
            EntityType.SQUID,
            EntityType.IRON_GOLEM
    );
    private static final BlockFace[] CARDINAL_DIRECTIONS = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public Utils() {
        super();
    }

    public static boolean isWorldClaimable(World world) {
        return !Config.Settlement.Claiming.DISABLED_WORLDS.contains(world.getName()) || EmpireManager.getInstance().getEmpireFromWorld(world) != null;
    }

    public static int getUpkeepCost(VillageState state, String worldName, int level) {
        int defaultCost = getUpkeepCost(state, level);
        return LEVEL_UPKEEP.computeIfAbsent(state.name() + "_" + worldName + "_" + level,
                key -> ConfigManager.getInstance().getLevelConfig().getInt(
                        state.name().toLowerCase() + ".level-" + level + ".per-world-upkeep." + worldName,
                        defaultCost
                ));
    }

    public static int getUpkeepCost(VillageState state, int level) {
        return LEVEL_UPKEEP.computeIfAbsent(state.name() + "_" + level,
                key -> ConfigManager.getInstance().getLevelConfig().getInt(state.name().toLowerCase() + ".level-" + level + ".claim-upkeep"));
    }

    public static int getTeleportDelay(String type) {
        try {
            Field f = Config.Teleport.Delay.class.getField(type.toUpperCase());
            return (int) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTeleportCooldown(String type) {
        try {
            Field f = Config.Teleport.Cooldown.class.getField(type.toUpperCase());
            return (int) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static ChatColor getChannelColor(ChatChannel channel, ChatColorType type) {
        switch (type) {
            case PREFIX:
                return ColorUtils.getColor(ConfigManager.getInstance().getConfig().getString("chat.channel-prefix-colors." + channel.getKey()), ChatColor.WHITE);
            case MESSAGE:
                return ColorUtils.getColor(ConfigManager.getInstance().getConfig().getString("chat.channel-colors." + channel.getKey()), ChatColor.WHITE);
        }
        return ChatColor.WHITE;
    }

    public static boolean hasPlotMode(PlotMode mode, String type) {
        return ConfigManager.getInstance().getConfig().isSet("modes." + type + "." + mode.getKey());
    }

    public static boolean getPlotMode(PlotMode mode, String type) {
        return ConfigManager.getInstance().getConfig().getBoolean("modes." + type + "." + mode.getKey(), mode.getDefault());
    }

    public static boolean playerHasInvite(UUID playerId, Village joinVillage) {
        return InviteCache.getPendingVillageInvites().containsKey(playerId) && InviteCache.getPendingVillageInvites().get(playerId).contains(joinVillage.getId());
    }

    public static boolean villageHasInvite(Village village, Empire joinEmpire) {
        return InviteCache.getPendingEmpireInvites().containsKey(village.getId()) && InviteCache.getPendingEmpireInvites().get(village.getId()).contains(joinEmpire.getId());
    }

    public static int getLevelChunks(VillageState state, int level) {
        return LEVEL_CHUNKS.computeIfAbsent(state.name() + "_" + level,
                key -> ConfigManager.getInstance().getLevelConfig().getInt(state.name().toLowerCase() + ".level-" + level + ".chunks"));
    }

    public static int getLevelOutposts(VillageState state, int level) {
        return LEVEL_OUTPOSTS.computeIfAbsent(state.name() + "_" + level,
                key -> ConfigManager.getInstance().getLevelConfig().getInt(state.name().toLowerCase() + ".level-" + level + ".outposts"));
    }

    public static int getMaxDrunkeness(ChatChannel channel) {
        return ConfigManager.getInstance().getConfig().getInt("brewery.block-channels." + channel.getKey(), 9001);
    }

    public static void addDisabledWorld(World world) {
        Collection<String> temp = Config.Settlement.Claiming.DISABLED_WORLDS;
        if (!temp.contains(world.getName())) {
            temp.add(world.getName());
            ConfigManager.getInstance().save();
        }
    }

    public static void removeDisabledWorld(World world) {
        Collection<String> temp = Config.Settlement.Claiming.DISABLED_WORLDS;
        if (temp.contains(world.getName())) {
            temp.remove(world.getName());
            ConfigManager.getInstance().save();
        }
    }

    public static Collection<SimpleChunkLocation> getChunksAround(Player player, int radius) {
        return getChunksAround(new SimpleChunkLocation(player.getLocation()), radius);
    }

    public static Collection<SimpleChunkLocation> getChunksAround(SimpleChunkLocation location, int radius) {
        Collection<SimpleChunkLocation> chunksAround = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= +radius; z++) {
                if (x != 0 || z != 0) {
                    chunksAround.add(location.copy().add(x, z));
                }
            }
        }
        return chunksAround;
    }

    public static Collection<SimpleChunkLocation> getChunksAdjacent(SimpleChunkLocation location) {
        Collection<SimpleChunkLocation> chunksAdjacent = new HashSet<>();
        for (BlockFace direction : CARDINAL_DIRECTIONS) {
            chunksAdjacent.add(location.copy().add(direction.getModX(), direction.getModZ()));
        }
        return chunksAdjacent;
    }

    /**
     * Make sure to run something not on the primary server thread.
     * A new task is started if this is the primary thread, if not the current one is used!
     * @param run What to run
     * @return The BukkitTask that was started or null if we are already on an async thread
     */
    public static BukkitTask runAsync(Runnable run) {
        return runAsync(run, false);
    }

    /**
     * Make sure to run something not on the primary server thread.
     * @param run What to run
     * @param force Whether or not we should force run this async (only works if the plugin is still running)
     * @return The BukkitTask that was started or null if we are already on an async thread
     */
    public static BukkitTask runAsync(Runnable run, boolean force) {
        if ((force || Bukkit.isPrimaryThread()) && KingdomsPlugin.getInstance().isRunning()) {
            return Bukkit.getScheduler().runTaskAsynchronously(KingdomsPlugin.getInstance(), run);
        } else {
            run.run();
        }
        return null;
    }

    /**
     * Make sure to run something on the primary server thread.
     * A new task is started if this is not the primary thread, if not the main one is used!
     * @param run What to run
     * @return The BukkitTask that was started or null if we are already on the main thread
     */
    public static BukkitTask runSync(Runnable run) {
        if (!Bukkit.isPrimaryThread()) {
            return Bukkit.getScheduler().runTask(KingdomsPlugin.getInstance(), run);
        } else {
            run.run();
        }
        return null;
    }

    /**
     * Run something in the next tick on the primary server thread.
     * @param run What to run
     * @return The BukkitTask that was started
     */
    public static BukkitTask runNextTick(Runnable run) {
        return Bukkit.getScheduler().runTask(KingdomsPlugin.getInstance(), run);
    }

    /**
     * Run something later on the primary server thread.
     * @param run   What to run
     * @param ticks The ticks to wait
     * @return The BukkitTask that was started
     */
    public static BukkitTask runLater(Runnable run, long ticks) {
        return Bukkit.getScheduler().runTaskLater(KingdomsPlugin.getInstance(), run, ticks);
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
        KingdomsPlugin.getTimer().schedule(task, unit.toMillis(amount));
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
        KingdomsPlugin.getTimer().scheduleAtFixedRate(task, unit.toMillis(delay), unit.toMillis(period));
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
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static <R extends Enum<R>> R parseEnum(String name, R defaultValue) {
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), name);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void catchSync() {
        if (KingdomsPlugin.getInstance().isRunning() && Bukkit.isPrimaryThread()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Doing an operation on the main thread that should be done async!", new Throwable());
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

    public static String getIcon(Resident resident, String type) {
        String icon = null;
        if (resident.hasResourcePack()) {
            icon = ConfigManager.getInstance().getIconRpMapping().getIcon(type, true);
        }
        return icon != null ? icon : "";
    }

    /**
     * Teleports a player. Checks cooldown and delay bypass permissions
     * @return false if teleport occurred instantly and no wait task was started
     */
    public static boolean teleport(ConfigMessages messages, Player player, Location loc, String type, String messagePath, String... replacements) {
        boolean bypassDelay = player.hasPermission("kingdoms.bypass.teleport-delay");
        if (bypassDelay && !player.hasPermission("kingdoms.bypass.teleport-cooldown")) {
            long cooldown = TELEPORT_COOLDOWNS.getOrDefault(player.getUniqueId(), 0L)
                    + Utils.getTeleportCooldown(type) * 1000
                    - System.currentTimeMillis();
            if (cooldown > 0) {
                messages.getError("teleport-cooldown").tell(player, "cooldown", UtilTime.format(cooldown));
                return true;
            } else {
                TELEPORT_COOLDOWNS.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        return ExecuteMoveUtil.getInstance().execute(player, () -> {
            PaperLib.getChunkAtAsync(loc).thenAccept(c -> {
                if (c == null) {
                    Message.get("error.destination-obstructed").tell(player);
                    KingdomsPlugin.logDebug("Could not load chunk at " + loc);
                    return;
                }
                Block block = loc.getBlock();
                Block up;
                while (block.getY() < 256 && ((up = block.getRelative(BlockFace.UP)).isPassable() || !block.isPassable())) {
                    block = up;
                }
                Block down;
                while (!(down = block.getRelative(BlockFace.DOWN)).getType().isSolid() && down.getY() > 0) {
                    block = down;
                }
                if (!down.getType().isSolid()) {
                    Message.get("error.destination-obstructed").tell(player);
                    KingdomsPlugin.logDebug("Location " + loc + " is has no floor");
                    return;
                }

                player.sendBlockChange(down.getLocation(), down.getBlockData());
                loc.setY(block.getY());
                PaperLib.teleportAsync(player, loc).thenAccept(success -> {
                    String path = messagePath + "." + (success ? "success" : "fail");
                    Message.get(path).tell(player, replacements);
                    new TitleBuilder()
                            .withTitle(Message.get(path + "-title.title"))
                            .withSubtitle(Message.get(path + "-title.subtitle"))
                            .withActionbar(Message.get(path + "-title.actionbar"))
                            .send(player, replacements);
                    if (!success) {
                        KingdomsPlugin.logDebug("Failed to teleport " + player.getName() + " to " + loc);
                    }
                });
            });
        }, bypassDelay ? 0 : Utils.getTeleportDelay(type), "Teleport", messagePath);
    }

    public static void teleportToSpawn(Player target) {
        String command = Config.Teleport.SPAWN_COMMAND;
        if (!command.isEmpty()) {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtils.replacePlaceholders(command, "player", target.getName()))) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not execute spawn teleport command? Is " + command + " a valid command?");
            }
        } else {
            PaperLib.teleportAsync(target, Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }

    public static boolean parseBoolean(String arg) throws IllegalArgumentException {
        switch (arg.toLowerCase()) {
            case "true":
            case "on":
            case "allow":
            case "yes":
                return true;
            case "false":
            case "off":
            case "deny":
            case "disallow":
            case "no":
                return false;
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

    public static boolean isHostile(LivingEntity entity) {
        return entity instanceof Monster || SPECIAL_HOSTILE.contains(entity.getType());
    }

    public static boolean isHostile(EntityType entityType) {
        return entityType.getEntityClass() != null && Monster.class.isAssignableFrom(entityType.getEntityClass()) || SPECIAL_HOSTILE.contains(entityType);
    }

    public static boolean isPassive(Entity entity) {
        return entity instanceof Animals || entity instanceof WaterMob || entity instanceof Ambient || SPECIAL_PASSIVE.contains(entity.getType());
    }

    public static WeatherState getWeatherState(World world) {
        if (world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE)) {
            return WeatherState.NORMAL;
        }
        if (world.isThundering()) {
            return WeatherState.THUNDER;
        }
        if (world.hasStorm()) {
            return WeatherState.RAIN;
        }
        return WeatherState.CLEAR;
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

    public static BlockFace rotateClockWise(BlockFace face) {
        int index = Arrays.asList(CARDINAL_DIRECTIONS).indexOf(face);
        if (index + 1 < CARDINAL_DIRECTIONS.length) {
            return CARDINAL_DIRECTIONS[index + 1];
        } else {
            return CARDINAL_DIRECTIONS[0];
        }
    }

    public static BlockFace getBlockFace(String direction) {
        for (BlockFace face : CARDINAL_DIRECTIONS) {
            if (face.name().startsWith(direction.toUpperCase())) {
                return face;
            }
        }
        return null;
    }

    public static String filterAllCurses(String message) {
        return ConfigManager.getInstance().getToggleableCurses().filterCurses(ConfigManager.getInstance().getForcedCurses().filterCurses(message));
    }

    public static boolean containsAnyCurses(String message) {
        return ConfigManager.getInstance().getToggleableCurses().containsCurses(message) || ConfigManager.getInstance().getForcedCurses().containsCurses(message);
    }
}

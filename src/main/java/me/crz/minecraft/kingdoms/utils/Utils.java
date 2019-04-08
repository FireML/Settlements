package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

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
            EntityType.SLIME,
            EntityType.PHANTOM
    );
    private static final BlockFace[] ADJACENT = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };
    private static Set<String> disabledClaimingWorlds = null;

    public Utils() {
        super();
    }

    public static ItemStack getHead(UUID playerId, String name) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(name);
        skull.setPlayerProfile(Bukkit.createProfile(playerId));
        item.setItemMeta(skull);
        return item;
    }

    public static boolean isWorldClaimable(World world) {
        return !getDisabledClaimingWorlds().contains(world.getName()) || EmpireManager.getInstance().getEmpireFromWorld(world) != null;
    }

    public static Collection<String> getDisabledClaimingWorlds() {
        if (disabledClaimingWorlds != null) {
            return disabledClaimingWorlds;
        }
        FileConfiguration config = KingdomsPlugin.getInstance().getConfig();
        if (config.contains("disabled-claiming-worlds")) {
            return (disabledClaimingWorlds = new HashSet<>(config.getStringList("disabled-claiming-worlds")));
        }
        disabledClaimingWorlds = new HashSet<>();
        List<String> temp = new ArrayList<>();
        config.set("disabled-claiming-worlds", temp);
        KingdomsPlugin.getInstance().saveConfig();
        return disabledClaimingWorlds;
    }

    public static int getEmpireCreationCost() {
        if (ConfigManager.getInstance().getConfig().isInt("empire-creation-cost")) {
            return ConfigManager.getInstance().getConfig().getInt("empire-creation-cost");
        }
        return 0;
    }

    public static int getVillageCreationCost() {
        if (ConfigManager.getInstance().getConfig().isInt("village-creation-cost")) {
            return ConfigManager.getInstance().getConfig().getInt("village-creation-cost");
        }
        return 0;
    }

    public static int getOutpostCreationCost() {
        if (ConfigManager.getInstance().getConfig().isInt("outpost-creation-cost")) {
            return ConfigManager.getInstance().getConfig().getInt("outpost-creation-cost");
        }
        return 0;
    }

    public static int getVillageMaxTax() {
        if (ConfigManager.getInstance().getConfig().isInt("village-max-tax")) {
            return ConfigManager.getInstance().getConfig().getInt("village-max-tax");
        }
        return -1;
    }

    public static int getEmpireMaxTax() {
        if (ConfigManager.getInstance().getConfig().isInt("empire-max-tax")) {
            return ConfigManager.getInstance().getConfig().getInt("empire-max-tax");
        }
        return -1;
    }

    public static int getUpkeepCost(VillageState state, int level) {
        return LEVEL_UPKEEP.computeIfAbsent(state.name() + "_" + level,
                key -> ConfigManager.getInstance().getLevelConfig().getInt(state.name().toLowerCase() + ".level-" + level + ".claim-upkeep"));
    }

    public static int getClaimEnemyDistance() {
        if (ConfigManager.getInstance().getConfig().isInt("claim-enemy-distance")) {
            return ConfigManager.getInstance().getConfig().getInt("claim-enemy-distance");
        }
        return 0;
    }

    public static int getCloseClaimsGraceDays() {
        if (ConfigManager.getInstance().getConfig().isInt("close-claims-grace-days")) {
            return ConfigManager.getInstance().getConfig().getInt("close-claims-grace-days");
        }
        return 0;
    }

    public static boolean getCloseClaimsUnclaimsOutposts() {
        if (ConfigManager.getInstance().getConfig().isBoolean("close-claims-unclaims-outposts")) {
            return ConfigManager.getInstance().getConfig().getBoolean("close-claims-unclaims-outposts");
        }
        return false;
    }

    public static boolean isFireSpreadAllowed() {
        if (ConfigManager.getInstance().getConfig().isBoolean("allow-firespread-outside-claim")) {
            return ConfigManager.getInstance().getConfig().getBoolean("allow-firespread-outside-claim");
        }
        return false;
    }

    public static int getTeleportDelay(String type) {
        if (ConfigManager.getInstance().getConfig().isInt("teleport-delay." + type)) {
            return ConfigManager.getInstance().getConfig().getInt("teleport-delay." + type);
        }
        return 0;
    }

    public static int getTeleportCooldown(String type) {
        if (ConfigManager.getInstance().getConfig().isInt("teleport-cooldown." + type)) {
            return ConfigManager.getInstance().getConfig().getInt("teleport-cooldown." + type);
        }
        return 0;
    }

    public static int getEntriesPerPage() {
        if (ConfigManager.getInstance().getConfig().isInt("entries-per-page")) {
            return ConfigManager.getInstance().getConfig().getInt("entries-per-page");
        }
        return 20;
    }

    public static int getPlayersInShow() {
        if (ConfigManager.getInstance().getConfig().isInt("players-in-show")) {
            return ConfigManager.getInstance().getConfig().getInt("players-in-show");
        }
        return 40;
    }

    public static String getSpawnCommand() {
        if (ConfigManager.getInstance().getConfig().isString("spawn-command")) {
            return ConfigManager.getInstance().getConfig().getString("spawn-command");
        }
        return "";
    }

    public static int getEmpireWorldRadius() {
        if (ConfigManager.getInstance().getConfig().isInt("empires-world.border-radius")) {
            return ConfigManager.getInstance().getConfig().getInt("empires-world.border-radius");
        }
        return 500;
    }

    public static int getEmpireWorldCooldown() {
        if (ConfigManager.getInstance().getConfig().isInt("empires-world.cooldown")) {
            return ConfigManager.getInstance().getConfig().getInt("empires-world.cooldown");
        }
        return 30;
    }

    public static int getEmpireWorldRegenDuration() {
        if (ConfigManager.getInstance().getConfig().isInt("empires-world.regen-duration")) {
            return ConfigManager.getInstance().getConfig().getInt("empires-world.regen-duration");
        }
        return 0;
    }

    public static boolean shouldResetChannelOnLogin() {
        if (ConfigManager.getInstance().getConfig().isBoolean("reset-channel-on-login")) {
            return ConfigManager.getInstance().getConfig().getBoolean("reset-channel-on-login");
        }
        return true;
    }

    public static Sound getChatNotifySound() {
        if (ConfigManager.getInstance().getConfig().isString("chat.notify.sound")) {
            try {
                return Sound.valueOf(ConfigManager.getInstance().getConfig().getString("chat.notify.sound").toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    }

    public static int getChatNotifyCooldown() {
        if (ConfigManager.getInstance().getConfig().isInt("chat.notify.cooldown")) {
            return ConfigManager.getInstance().getConfig().getInt("chat.notify.cooldown");
        }
        return 15;
    }

    public static boolean playerIsChief(Player player, Village village) {
        return village != null && village.getChiefId().equals(player.getUniqueId());
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

    public static void addDisabledWorld(World world) {
        Collection<String> temp = getDisabledClaimingWorlds();
        if (!temp.contains(world.getName())) {
            temp.add(world.getName());
            KingdomsPlugin.getInstance().getConfig().set("disabled-claiming-worlds", new ArrayList<>(temp));
            KingdomsPlugin.getInstance().saveConfig();
        }
    }

    public static void removeDisabledWorld(World world) {
        Collection<String> temp = getDisabledClaimingWorlds();
        if (temp.contains(world.getName())) {
            temp.remove(world.getName());
            KingdomsPlugin.getInstance().getConfig().set("disabled-claiming-worlds", new ArrayList<>(temp));
            KingdomsPlugin.getInstance().saveConfig();
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
        for (BlockFace direction : ADJACENT) {
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
        if (Bukkit.isPrimaryThread()) {
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
        nextUpkeep.setTimeZone(TimeZone.getTimeZone(KingdomsPlugin.getInstance().getConfig().getString("upkeep.timezone")));
        if (nextUpkeep.get(Calendar.HOUR_OF_DAY) >= KingdomsPlugin.getInstance().getConfig().getInt("upkeep.hour")) {
            nextUpkeep.set(Calendar.DAY_OF_YEAR, nextUpkeep.get(Calendar.DAY_OF_YEAR) + 1);
        }
        nextUpkeep.set(Calendar.HOUR_OF_DAY, KingdomsPlugin.getInstance().getConfig().getInt("upkeep.hour"));
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
            player.teleportAsync(loc).thenAccept(success -> {
                Message.get(messagePath + "." + (success ? "success" : "fail")).tell(player, replacements);
            });
        }, bypassDelay ? 0 : Utils.getTeleportDelay(type), "Teleport", messagePath);
    }

    public static void teleportToSpawn(Player target) {
        String command = getSpawnCommand();
        if (!command.isEmpty()) {
            if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtils.replacePlaceholders(command, "player", target.getName()))) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not execute spawn teleport command? Is " + command + " a valid command?");
            }
        } else {
            target.teleportAsync(Bukkit.getWorlds().get(0).getSpawnLocation());
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
        Map<String, String> map = new LinkedHashMap<>();
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
}

package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.World;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

public class Utils {
    public final static DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###");
    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat();
    private static Map<String, Integer> LEVEL_CHUNKS = new HashMap<>();
    private static Map<String, Integer> LEVEL_OUTPOSTS = new HashMap<>();

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

    public static boolean playerIsElder(Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.ELDER);
    }

    public static boolean isWorldClaimable(World world) {
        return !getDisabledClaimingWorlds().contains(world.getName());
    }

    public static Collection<String> getDisabledClaimingWorlds() {
        FileConfiguration config = KingdomsPlugin.getInstance().getConfig();
        if (config.contains("disabled-claiming-worlds")) {
            return new HashSet<>(config.getStringList("disabled-claiming-worlds"));
        }
        List<String> temp = new ArrayList<>();
        config.set("disabled-claiming-worlds", temp);
        KingdomsPlugin.getInstance().saveConfig();
        return temp;
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

    public static int getUpkeepCost() {
        if (ConfigManager.getInstance().getConfig().isInt("upkeep.cost-per-claim")) {
            return ConfigManager.getInstance().getConfig().getInt("upkeep.cost-per-claim");
        }
        return 0;
    }

    public static int getClaimEnemyDistance() {
        if (ConfigManager.getInstance().getConfig().isInt("claim-enemy-distance")) {
            return ConfigManager.getInstance().getConfig().getInt("claim-enemy-distance");
        }
        return 0;
    }

    public static int getUnAllyGraceDays() {
        if (ConfigManager.getInstance().getConfig().isInt("unally-grace-days")) {
            return ConfigManager.getInstance().getConfig().getInt("unally-grace-days");
        }
        return 0;
    }

    public static boolean getUnallyUnclaimsOutposts() {
        if (ConfigManager.getInstance().getConfig().isBoolean("unally-unclaims-outposts")) {
            return ConfigManager.getInstance().getConfig().getBoolean("unally-unclaims-outposts");
        }
        return false;
    }

    public static int getTeleportDelay(String type) {
        if (ConfigManager.getInstance().getConfig().isInt("teleport-delay." + type)) {
            return ConfigManager.getInstance().getConfig().getInt("teleport-delay." + type);
        }
        return 0;
    }

    public static int getKingdomsWorldLevel() {
        if (ConfigManager.getInstance().getConfig().isInt("kingdoms-world-level")) {
            return ConfigManager.getInstance().getConfig().getInt("kingdoms-world-level");
        }
        return 0;
    }

    public static String getSpawnCommand() {
        if (ConfigManager.getInstance().getConfig().isString("spawn-command")) {
            return ConfigManager.getInstance().getConfig().getString("spawn-command");
        }
        return "spawn {player}";
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
        String world = location.getWorld();
        int baseX = location.getX();
        int baseZ = location.getZ();
        Collection<SimpleChunkLocation> chunksAround = new HashSet<>();
        for (int x = baseX - radius; x <= baseX + radius; x++) {
            for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                if (x != baseX || z != baseZ) {
                    chunksAround.add(new SimpleChunkLocation(world, x, z));
                }
            }
        }
        return chunksAround;
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
        nextUpkeep.set(Calendar.HOUR_OF_DAY, KingdomsPlugin.getInstance().getConfig().getInt("upkeep.hour"));
        nextUpkeep.set(Calendar.MINUTE, 0);
        nextUpkeep.set(Calendar.SECOND, 0);
        nextUpkeep.set(Calendar.MILLISECOND, 1);
        nextUpkeep.set(Calendar.DAY_OF_YEAR, nextUpkeep.get(Calendar.DAY_OF_YEAR) + 1);
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

    public static void teleport(Player player, Location loc, String type, String messagePath, String... replacements) {
        int delay = Utils.getTeleportDelay(type);
        if (delay > 0) {
            Message.get(messagePath + ".waiting").tell(player, "seconds", String.valueOf(delay));
            ExecuteMoveUtil.getInstance().execute(player, () -> {
                player.teleport(loc);
                Message.get(messagePath + ".success").tell(player, replacements);
            }, delay, "Teleport");
        } else {
            player.teleport(loc);
            Message.get(messagePath + ".success").tell(player, replacements);
        }
    }
}

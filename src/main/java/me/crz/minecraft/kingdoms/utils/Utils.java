package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private static Map<String, Integer> LEVEL_CHUNKS = new HashMap<>();

    public Utils() {
        super();
    }

    public static ItemStack getHead(OfflinePlayer player, String name) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(name);
        skull.setOwningPlayer(player);
        item.setItemMeta(skull);
        return item;
    }

    public static boolean playerIsElder(Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.ELDER);
    }

    public static boolean isWorldClaimable(World world) {
        return !getDisabledClaimingWorlds().contains(world.getName());
    }

    public static List<String> getDisabledClaimingWorlds() {
        FileConfiguration config = KingdomsPlugin.getInstance().getConfig();
        if (config.contains("disabled-claiming-worlds")) {
            return config.getStringList("disabled-claiming-worlds");
        }
        List<String> temp = new ArrayList<>();
        config.set("disabled-claiming-worlds", temp);
        KingdomsPlugin.getInstance().saveConfig();
        return temp;
    }

    public static int getVillageCreationCost() {
        if (ConfigManager.getInstance().getConfig().isInt("village-creation-cost")) {
            return ConfigManager.getInstance().getConfig().getInt("village-creation-cost");
        }
        return 0;
    }

    public static boolean playerIsChief(Player player, Village village) {
        return village != null && village.getChief().equals(player.getUniqueId());
    }

    public static boolean playerIsBuilder(Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.BUILDER);
    }

    public static boolean playerInSameVillage(Village sender, Village other) {
        return sender != null && sender.equals(other);
    }

    public static boolean playerIsRecruiter(Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.RECRUITER);
    }

    public static boolean playerCanKick(Player player, Village village) {
        return playerIsElder(player) || playerIsRecruiter(player) || village.getChief().equals(player.getUniqueId());
    }

    public static boolean playerHasInvite(Player player, String joinVillageName) {
        return TownCache.getPendingTownInvites().containsKey(player.getUniqueId()) && TownCache.getPendingTownInvites().get(player.getUniqueId()).equals(joinVillageName);
    }

    public static int getChunkMax(int level, VillageState state) {
        return getLevelChunks(state, level);
    }

    private static int getLevelChunks(VillageState state, int level) {
        return LEVEL_CHUNKS.computeIfAbsent(state.name() + "_" + level,
                key -> ConfigManager.getInstance().getLevelConfig().getInt(state.name().toLowerCase() + ".level-" + level + ".chunks"));
    }

    public static void addDisabledWorld(World world) {
        List<String> temp = getDisabledClaimingWorlds();
        if (!temp.contains(world.getName())) {
            temp.add(world.getName());
            KingdomsPlugin.getInstance().getConfig().set("disabled-claiming-worlds", temp);
            KingdomsPlugin.getInstance().saveConfig();
        }
    }

    public static void removeDisabledWorld(World world) {
        List<String> temp = getDisabledClaimingWorlds();
        if (temp.contains(world.getName())) {
            temp.remove(world.getName());
            KingdomsPlugin.getInstance().getConfig().set("disabled-claiming-worlds", temp);
            KingdomsPlugin.getInstance().saveConfig();
        }
    }

    public static int getChunkMaxOutpost(int level, VillageState state) {
        if (state == VillageState.VILLAGE) {
            if (level <= 3) {
                return 1;
            }
            if (level <= 5) {
                return 2;
            }
        }
        if (state == VillageState.KINGDOM) {
            if (level <= 2) {
                return 4;
            }
            if (level <= 5) {
                return 6;
            }
            if (level <= 8) {
                return 8;
            }
            if (level <= 10) {
                return 12;
            }
        }
        return 4;
    }

    public static Collection<Chunk> getChunksAroundPlayer(Player player, int radius) {
        World world = player.getWorld();
        int baseX = player.getLocation().getChunk().getX();
        int baseZ = player.getLocation().getChunk().getZ();
        Collection<Chunk> chunksAroundPlayer = new HashSet<>();
        for (int x = baseX - radius; x <= baseX + radius; x++) {
            for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                chunksAroundPlayer.add(chunk);
            }
        }
        return chunksAroundPlayer;
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
}

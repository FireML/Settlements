package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bukkit.World;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.OfflinePlayer;

public class Utils {
    private static final int CHUNK_VILLAGE_LEVEL_ONE = getChunkFromConfig("village-level-one-chunks");
    private static final int CHUNK_VILLAGE_LEVEL_TWO = getChunkFromConfig("village-level-two-chunks");
    private static final int CHUNK_VILLAGE_LEVEL_THREE = getChunkFromConfig("village-level-three-chunks");
    private static final int CHUNK_VILLAGE_LEVEL_FOUR = getChunkFromConfig("village-level-four-chunks");
    private static final int CHUNK_VILLAGE_LEVEL_FIVE = getChunkFromConfig("village-level-five-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_ONE = getChunkFromConfig("kingdom-level-one-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_TWO = getChunkFromConfig("kingdom-level-two-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_THREE = getChunkFromConfig("kingdom-level-three-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_FOUR = getChunkFromConfig("kingdom-level-four-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_FIVE = getChunkFromConfig("kingdom-level-five-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_SIX = getChunkFromConfig("kingdom-level-six-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_SEVEN = getChunkFromConfig("kingdom-level-seven-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_EIGHT = getChunkFromConfig("kingdom-level-eight-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_NINE = getChunkFromConfig("kingdom-level-nine-chunks");
    private static final int CHUNK_KINGDOM_LEVEL_TEN = getChunkFromConfig("kingdom-level-ten-chunks");

    public Utils() {
        super();
    }

    public static ItemStack getHead(final OfflinePlayer player, final String name) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(name);
        skull.setOwningPlayer(player);
        item.setItemMeta(skull);
        return item;
    }

    public static boolean playerIsElder(final Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.ELDER);
    }

    public static boolean isWorldClaimable(final World world) {
        return !getDisabledClaimingWorlds().contains(world.getName());
    }

    public static List<String> getDisabledClaimingWorlds() {
        final FileConfiguration config = ConfigManager.getInstance().getConfig();
        if (config.contains("disabled-claiming-worlds")) {
            return config.getStringList("disabled-claiming-worlds");
        }
        final List<String> temp = new ArrayList<>();
        config.set("disabled-claiming-worlds", temp);
        ConfigManager.getInstance().save(ConfigManager.getInstance().configf, config);
        return temp;
    }

    public static int getVillageCreationCost() {
        if (ConfigManager.getInstance().getConfig().contains("village-creation-cost")) {
            return ConfigManager.getInstance().getConfig().getInt("village-creation-cost");
        }
        return 0;
    }

    public static boolean playerIsChief(final Player player, final Village village) {
        return village != null && village.getChief().equals(player.getUniqueId());
    }

    public static boolean playerIsBuilder(final Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.BUILDER);
    }

    public static boolean playerInSameVillage(final Village sender, final Village other) {
        return other != null && sender.getName().equals(other.getName());
    }

    public static boolean playerIsRecruiter(final Player player) {
        return ResidentManager.getInstance().getResident(player).getVillageRank().equals(VillageRank.RECRUITER);
    }

    public static boolean playerCanKick(final Player player, final Village village) {
        return playerIsElder(player) || playerIsRecruiter(player) || village.getChief().equals(player.getUniqueId());
    }

    public static boolean playerHasInvite(final Player player, final String joinVillageName) {
        return TownCache.getPendingTownInvites().containsKey(player.getUniqueId()) && TownCache.getPendingTownInvites().get(player.getUniqueId()).equals(joinVillageName);
    }

    private static int getChunkFromConfig(final String levelPath) {
        return ConfigManager.getInstance().getConfig().getInt(levelPath);
    }

    public static int getChunkMax(final int level, final VillageState state) {
        if (state == VillageState.VILLAGE) {
            switch (level) {
                case 1:
                    return CHUNK_VILLAGE_LEVEL_ONE;
                case 2:
                    return CHUNK_VILLAGE_LEVEL_TWO;
                case 3:
                    return CHUNK_VILLAGE_LEVEL_THREE;
                case 4:
                    return CHUNK_VILLAGE_LEVEL_FOUR;
                case 5:
                    return CHUNK_VILLAGE_LEVEL_FIVE;
            }
        } else if (state == VillageState.KINGDOM) {
            switch (level) {
                case 1:
                    return CHUNK_KINGDOM_LEVEL_ONE;
                case 2:
                    return CHUNK_KINGDOM_LEVEL_TWO;
                case 3:
                    return CHUNK_KINGDOM_LEVEL_THREE;
                case 4:
                    return CHUNK_KINGDOM_LEVEL_FOUR;
                case 5:
                    return CHUNK_KINGDOM_LEVEL_FIVE;
                case 6:
                    return CHUNK_KINGDOM_LEVEL_SIX;
                case 7:
                    return CHUNK_KINGDOM_LEVEL_SEVEN;
                case 8:
                    return CHUNK_KINGDOM_LEVEL_EIGHT;
                case 9:
                    return CHUNK_KINGDOM_LEVEL_NINE;
                case 10:
                    return CHUNK_KINGDOM_LEVEL_TEN;
            }
        }
        return 0;
    }

    public static void addDisabledWorld(final World world) {
        final FileConfiguration config = ConfigManager.getInstance().getConfig();
        final List<String> temp = getDisabledClaimingWorlds();
        if (!temp.contains(world.getName())) {
            temp.add(world.getName());
            config.set("disabled-claiming-worlds", temp);
            ConfigManager.getInstance().save(ConfigManager.getInstance().configf, config);
        }
    }

    public static void removeDisabledWorld(final World world) {
        final FileConfiguration config = ConfigManager.getInstance().getConfig();
        final List<String> temp = getDisabledClaimingWorlds();
        if (temp.contains(world.getName())) {
            temp.remove(world.getName());
            config.set("disabled-claiming-worlds", temp);
            ConfigManager.getInstance().save(ConfigManager.getInstance().configf, config);
        }
    }

    public static int getChunkMaxOutpost(final int level, final VillageState state) {
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

    public static Collection<Chunk> getChunksAroundPlayer(final Player player, int radius) {
        final World world = player.getWorld();
        final int baseX = player.getLocation().getChunk().getX();
        final int baseZ = player.getLocation().getChunk().getZ();
        final Collection<Chunk> chunksAroundPlayer = new HashSet<>();
        for (int x = baseX - radius; x <= baseX + radius; x++) {
            for (int z = baseZ - radius; z <= baseZ + radius; z++) {
                final Chunk chunk = world.getChunkAt(x, z);
                chunksAroundPlayer.add(chunk);
            }
        }
        return chunksAroundPlayer;
    }
}

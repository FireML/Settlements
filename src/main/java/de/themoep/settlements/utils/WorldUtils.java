package de.themoep.settlements.utils;

import de.themoep.settlements.world.SimpleChunkLocation;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class WorldUtils {
    private static final BlockFace[] CARDINAL_DIRECTIONS = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public static Collection<SimpleChunkLocation> getChunksAround(Player player, int radius) {
        return getChunksAround(new SimpleChunkLocation(player.getLocation()), radius);
    }

    public static Collection<SimpleChunkLocation> getChunksAround(SimpleChunkLocation location, int radius) {
        Collection<SimpleChunkLocation> chunksAround = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
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

    public static WeatherState getWeatherState(World world) {
        if (world.getGameRuleValue(GameRules.ADVANCE_WEATHER)) {
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

    public static long getBlockLocationHash(Block block) {
        int hash = block.getWorld().getUID().hashCode();
        hash = hash * 31 + block.getX();
        hash = hash * 31 + block.getY();
        hash = hash * 31 + block.getZ();
        return hash;
    }
}

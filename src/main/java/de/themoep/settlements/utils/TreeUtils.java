package de.themoep.settlements.utils;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;

import java.util.*;

public class TreeUtils {

    /**
     * Get all blocks of a tree
     * @param block The start block to search from, needs to be a log!
     * @return The tree or null if there is none
     */
    public static List<Block> getTree(Block block) {
        if (!Tag.LOGS.isTagged(block.getType())
                && (!Tag.LEAVES.isTagged(block.getType()) || ((Leaves) block.getState().getBlockData()).isPersistent()))
            // No logs or leaves, no tree!
            return null;

        TreeSpecies species = getSpecies(block);
        if (species == null) {
            return null;
        }
        Material log = getLogs(species);
        Material leaves = getLeaves(species);

        final Set<Block> tree = new LinkedHashSet<>();
        final LinkedList<Block> queue = new LinkedList<>();

        int treeLeaves = 0;
        int treeLogs = 0;

        queue.addLast(block);
        tree.add(block);

        boolean searchForLogs = true;

        while (!queue.isEmpty()) {
            final Block current = queue.removeFirst();
            boolean foundLog = log != current.getType();
            for (int[] mod : getBlockFaces(species)) {
                if (mod.length != 3) {
                    continue;
                }
                final Block next = current.getRelative(mod[0], mod[1], mod[2]);
                if (block.getLocation().distanceSquared(next.getLocation()) > 256) {
                    // Maximum range exceeded => stop walking
                    continue;
                }

                if (!tree.contains(next)) {
                    if (leaves == next.getType()) {
                        if (!((Leaves) next.getState().getBlockData()).isPersistent()) {
                            // There are leaves that are decayable so it's probably a tree! \o/
                            treeLeaves++;
                            queue.addLast(next);
                            tree.add(next);
                        }
                    } else if (searchForLogs && log == next.getType()) {
                        // queue next point
                        foundLog = true;
                        treeLogs++;
                        queue.addLast(next);
                        tree.add(next);
                    } // if
                } // if
            } // for
            searchForLogs = foundLog;
        } // while

        if (treeLogs == 0 || treeLeaves == 0) {
            // A tree has logs and leaves, this is missing some
            return null;
        }

        return new ArrayList<>(tree);
    }

    private static Collection<int[]> getBlockFaces(TreeSpecies species) {
        List<int[]> mods = new ArrayList<>();
        if (species == TreeSpecies.ACACIA || species == TreeSpecies.DARK_OAK || species == TreeSpecies.GENERIC) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        mods.add(new int[]{x, y, z});
                    }
                }
            }
        } else {
            for (int i = 0; i < 6; i++) {
                BlockFace face = BlockFace.values()[i];
                mods.add(new int[]{face.getModX(), face.getModY(), face.getModZ()});
            }
        }
        return mods;
    }

    /**
     * Get the logs of a TreeSpecies
     * @param species The species
     * @return The logs or null if the species doesn't have them or is unknown
     */
    public static Material getLogs(TreeSpecies species) {
        switch (species) {
            case ACACIA:
                return Material.ACACIA_LOG;
            case BIRCH:
                return Material.BIRCH_LOG;
            case DARK_OAK:
                return Material.DARK_OAK_LOG;
            case GENERIC:
                return Material.OAK_LOG;
            case JUNGLE:
                return Material.JUNGLE_LOG;
            case REDWOOD:
                return Material.SPRUCE_LOG;
            default:
                return null;
        }
    }

    /**
     * Get the leaves of a TreeSpecies
     * @param species The species
     * @return The leaves or null if the species doesn't have them or is unknown
     */
    public static Material getLeaves(TreeSpecies species) {
        switch (species) {
            case ACACIA:
                return Material.ACACIA_LEAVES;
            case BIRCH:
                return Material.BIRCH_LEAVES;
            case DARK_OAK:
                return Material.DARK_OAK_LEAVES;
            case GENERIC:
                return Material.OAK_LEAVES;
            case JUNGLE:
                return Material.JUNGLE_LEAVES;
            case REDWOOD:
                return Material.SPRUCE_LEAVES;
            default:
                return null;
        }
    }

    /**
     * Get the TreeSpecies from a block
     * @param block The block tog et it from
     * @return The TreeSpecies or null if the block can't belong to a tree
     */
    public static TreeSpecies getSpecies(Block block) {
        switch (block.getType()) {
            case ACACIA_BOAT:
            case ACACIA_BUTTON:
            case ACACIA_DOOR:
            case ACACIA_FENCE:
            case ACACIA_FENCE_GATE:
            case ACACIA_LEAVES:
            case ACACIA_LOG:
            case ACACIA_PLANKS:
            case ACACIA_PRESSURE_PLATE:
            case ACACIA_SAPLING:
            case ACACIA_SIGN:
            case ACACIA_SLAB:
            case ACACIA_STAIRS:
            case ACACIA_TRAPDOOR:
            case ACACIA_WALL_SIGN:
            case ACACIA_WOOD:
                return TreeSpecies.ACACIA;
            case BIRCH_BOAT:
            case BIRCH_BUTTON:
            case BIRCH_DOOR:
            case BIRCH_FENCE:
            case BIRCH_FENCE_GATE:
            case BIRCH_LEAVES:
            case BIRCH_LOG:
            case BIRCH_PLANKS:
            case BIRCH_PRESSURE_PLATE:
            case BIRCH_SAPLING:
            case BIRCH_SIGN:
            case BIRCH_SLAB:
            case BIRCH_STAIRS:
            case BIRCH_TRAPDOOR:
            case BIRCH_WALL_SIGN:
            case BIRCH_WOOD:
                return TreeSpecies.BIRCH;
            case DARK_OAK_BOAT:
            case DARK_OAK_BUTTON:
            case DARK_OAK_DOOR:
            case DARK_OAK_FENCE:
            case DARK_OAK_FENCE_GATE:
            case DARK_OAK_LEAVES:
            case DARK_OAK_LOG:
            case DARK_OAK_PLANKS:
            case DARK_OAK_PRESSURE_PLATE:
            case DARK_OAK_SAPLING:
            case DARK_OAK_SIGN:
            case DARK_OAK_SLAB:
            case DARK_OAK_STAIRS:
            case DARK_OAK_TRAPDOOR:
            case DARK_OAK_WALL_SIGN:
            case DARK_OAK_WOOD:
                return TreeSpecies.DARK_OAK;
            case JUNGLE_BOAT:
            case JUNGLE_BUTTON:
            case JUNGLE_DOOR:
            case JUNGLE_FENCE:
            case JUNGLE_FENCE_GATE:
            case JUNGLE_LEAVES:
            case JUNGLE_LOG:
            case JUNGLE_PLANKS:
            case JUNGLE_PRESSURE_PLATE:
            case JUNGLE_SAPLING:
            case JUNGLE_SIGN:
            case JUNGLE_SLAB:
            case JUNGLE_STAIRS:
            case JUNGLE_TRAPDOOR:
            case JUNGLE_WALL_SIGN:
            case JUNGLE_WOOD:
                return TreeSpecies.JUNGLE;
            case OAK_BOAT:
            case OAK_BUTTON:
            case OAK_DOOR:
            case OAK_FENCE:
            case OAK_FENCE_GATE:
            case OAK_LEAVES:
            case OAK_LOG:
            case OAK_PLANKS:
            case OAK_PRESSURE_PLATE:
            case OAK_SAPLING:
            case OAK_SIGN:
            case OAK_SLAB:
            case OAK_STAIRS:
            case OAK_TRAPDOOR:
            case OAK_WALL_SIGN:
            case OAK_WOOD:
                return TreeSpecies.GENERIC;
            case SPRUCE_BOAT:
            case SPRUCE_BUTTON:
            case SPRUCE_DOOR:
            case SPRUCE_FENCE:
            case SPRUCE_FENCE_GATE:
            case SPRUCE_LEAVES:
            case SPRUCE_LOG:
            case SPRUCE_PLANKS:
            case SPRUCE_PRESSURE_PLATE:
            case SPRUCE_SAPLING:
            case SPRUCE_SIGN:
            case SPRUCE_SLAB:
            case SPRUCE_STAIRS:
            case SPRUCE_TRAPDOOR:
            case SPRUCE_WALL_SIGN:
            case SPRUCE_WOOD:
                return TreeSpecies.REDWOOD;
            default:
                return null;
        }
    }
}

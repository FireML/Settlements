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
        return switch (species) {
            case ACACIA -> Material.ACACIA_LOG;
            case BIRCH -> Material.BIRCH_LOG;
            case DARK_OAK -> Material.DARK_OAK_LOG;
            case GENERIC -> Material.OAK_LOG;
            case JUNGLE -> Material.JUNGLE_LOG;
            case REDWOOD -> Material.SPRUCE_LOG;
        };
    }

    /**
     * Get the leaves of a TreeSpecies
     * @param species The species
     * @return The leaves or null if the species doesn't have them or is unknown
     */
    public static Material getLeaves(TreeSpecies species) {
        return switch (species) {
            case ACACIA -> Material.ACACIA_LEAVES;
            case BIRCH -> Material.BIRCH_LEAVES;
            case DARK_OAK -> Material.DARK_OAK_LEAVES;
            case GENERIC -> Material.OAK_LEAVES;
            case JUNGLE -> Material.JUNGLE_LEAVES;
            case REDWOOD -> Material.SPRUCE_LEAVES;
        };
    }

    /**
     * Get the TreeSpecies from a block
     * @param block The block tog et it from
     * @return The TreeSpecies or null if the block can't belong to a tree
     */
    public static TreeSpecies getSpecies(Block block) {
        return switch (block.getType()) {
            case ACACIA_BOAT, ACACIA_BUTTON, ACACIA_DOOR, ACACIA_FENCE, ACACIA_FENCE_GATE, ACACIA_LEAVES, ACACIA_LOG, ACACIA_PLANKS, ACACIA_PRESSURE_PLATE, ACACIA_SAPLING, ACACIA_SIGN, ACACIA_SLAB, ACACIA_STAIRS, ACACIA_TRAPDOOR, ACACIA_WALL_SIGN, ACACIA_WOOD ->
                    TreeSpecies.ACACIA;
            case BIRCH_BOAT, BIRCH_BUTTON, BIRCH_DOOR, BIRCH_FENCE, BIRCH_FENCE_GATE, BIRCH_LEAVES, BIRCH_LOG, BIRCH_PLANKS, BIRCH_PRESSURE_PLATE, BIRCH_SAPLING, BIRCH_SIGN, BIRCH_SLAB, BIRCH_STAIRS, BIRCH_TRAPDOOR, BIRCH_WALL_SIGN, BIRCH_WOOD ->
                    TreeSpecies.BIRCH;
            case DARK_OAK_BOAT, DARK_OAK_BUTTON, DARK_OAK_DOOR, DARK_OAK_FENCE, DARK_OAK_FENCE_GATE, DARK_OAK_LEAVES, DARK_OAK_LOG, DARK_OAK_PLANKS, DARK_OAK_PRESSURE_PLATE, DARK_OAK_SAPLING, DARK_OAK_SIGN, DARK_OAK_SLAB, DARK_OAK_STAIRS, DARK_OAK_TRAPDOOR, DARK_OAK_WALL_SIGN, DARK_OAK_WOOD ->
                    TreeSpecies.DARK_OAK;
            case JUNGLE_BOAT, JUNGLE_BUTTON, JUNGLE_DOOR, JUNGLE_FENCE, JUNGLE_FENCE_GATE, JUNGLE_LEAVES, JUNGLE_LOG, JUNGLE_PLANKS, JUNGLE_PRESSURE_PLATE, JUNGLE_SAPLING, JUNGLE_SIGN, JUNGLE_SLAB, JUNGLE_STAIRS, JUNGLE_TRAPDOOR, JUNGLE_WALL_SIGN, JUNGLE_WOOD ->
                    TreeSpecies.JUNGLE;
            case OAK_BOAT, OAK_BUTTON, OAK_DOOR, OAK_FENCE, OAK_FENCE_GATE, OAK_LEAVES, OAK_LOG, OAK_PLANKS, OAK_PRESSURE_PLATE, OAK_SAPLING, OAK_SIGN, OAK_SLAB, OAK_STAIRS, OAK_TRAPDOOR, OAK_WALL_SIGN, OAK_WOOD ->
                    TreeSpecies.GENERIC;
            case SPRUCE_BOAT, SPRUCE_BUTTON, SPRUCE_DOOR, SPRUCE_FENCE, SPRUCE_FENCE_GATE, SPRUCE_LEAVES, SPRUCE_LOG, SPRUCE_PLANKS, SPRUCE_PRESSURE_PLATE, SPRUCE_SAPLING, SPRUCE_SIGN, SPRUCE_SLAB, SPRUCE_STAIRS, SPRUCE_TRAPDOOR, SPRUCE_WALL_SIGN, SPRUCE_WOOD ->
                    TreeSpecies.REDWOOD;
            default -> null;
        };
    }
}

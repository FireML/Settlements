package me.crz.minecraft.kingdoms.utils;

import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.EnumSet;
import java.util.Set;

public class MaterialUtils {
    public static final Set<Material> SHULKER_BOXES = EnumSet.of(
            Material.SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX
    );
    public static final Set<Material> CONTAINERS = EnumSet.of(
            Material.TRAPPED_CHEST,
            Material.CHEST,
            Material.FURNACE
    );
    public static final Set<Material> PRESSURE_PLATES = EnumSet.of(
            Material.STONE_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE
    );
    public static final Set<Material> FENCE_GATES = EnumSet.of(
            Material.ACACIA_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.SPRUCE_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE,
            Material.OAK_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE
    );
    public static final Set<Material> BUCKETS = EnumSet.of(
            Material.BUCKET,
            Material.WATER_BUCKET,
            Material.LAVA_BUCKET,
            Material.TROPICAL_FISH_BUCKET,
            Material.COD_BUCKET,
            Material.PUFFERFISH_BUCKET,
            Material.SALMON_BUCKET,
            Material.MILK_BUCKET
    );
    public static final Set<Material> MINECARTS = EnumSet.of(
            Material.MINECART,
            Material.CHEST_MINECART,
            Material.COMMAND_BLOCK_MINECART,
            Material.FURNACE_MINECART,
            Material.HOPPER_MINECART,
            Material.TNT_MINECART
    );
    public static final Set<Material> BEDS = EnumSet.of(
            Material.RED_BED,
            Material.BLACK_BED,
            Material.BLUE_BED,
            Material.LIGHT_GRAY_BED,
            Material.BROWN_BED,
            Material.CYAN_BED,
            Material.GRAY_BED,
            Material.GREEN_BED,
            Material.LIGHT_BLUE_BED,
            Material.MAGENTA_BED,
            Material.LIME_BED,
            Material.ORANGE_BED,
            Material.PINK_BED,
            Material.PURPLE_BED,
            Material.WHITE_BED,
            Material.YELLOW_BED
    );

    static {
        CONTAINERS.addAll(SHULKER_BOXES);
        PRESSURE_PLATES.addAll(Tag.WOODEN_PRESSURE_PLATES.getValues());
    }
}

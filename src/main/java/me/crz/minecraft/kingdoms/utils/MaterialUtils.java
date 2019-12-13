package me.crz.minecraft.kingdoms.utils;

import com.destroystokyo.paper.MaterialTags;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public class MaterialUtils {
    public static final Set<Material> MINECARTS = EnumSet.of(
            Material.MINECART,
            Material.CHEST_MINECART,
            Material.COMMAND_BLOCK_MINECART,
            Material.FURNACE_MINECART,
            Material.HOPPER_MINECART,
            Material.TNT_MINECART
    );
    public static final Set<Material> HOES = EnumSet.of(
            Material.DIAMOND_HOE,
            Material.GOLDEN_HOE,
            Material.IRON_HOE,
            Material.STONE_HOE,
            Material.WOODEN_HOE
    );
    public static final Set<Material> FARM_BLOCKS = EnumSet.of(
            Material.WHEAT,
            Material.POTATOES,
            Material.CARROTS,
            Material.BEETROOTS,
            Material.NETHER_WART,
            Material.MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.KELP,
            Material.CACTUS,
            Material.CHORUS_FLOWER,
            Material.COCOA,
            Material.SUGAR_CANE,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.VINE,
            Material.CHORUS_FLOWER,
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.SWEET_BERRY_BUSH
    );
    public static final Set<Material> FARM_BLOCKS_DESTROY = EnumSet.of(
            Material.PUMPKIN,
            Material.MELON,
            Material.BROWN_MUSHROOM_BLOCK,
            Material.RED_MUSHROOM_BLOCK,
            Material.MUSHROOM_STEM,
            Material.CHORUS_PLANT
    );
    public static final Set<Material> FARM_ITEMS = EnumSet.of(
            Material.WHEAT,
            Material.WHEAT_SEEDS,
            Material.CARROT,
            Material.POTATO,
            Material.PUMPKIN,
            Material.PUMPKIN_SEEDS,
            Material.MELON,
            Material.MELON_SEEDS,
            Material.BEETROOT,
            Material.BEETROOT_SEEDS,
            Material.NETHER_WART,
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.CHORUS_FRUIT,
            Material.KELP,
            Material.KELP_PLANT,
            Material.CHORUS_FRUIT,
            Material.BAMBOO,
            Material.BAMBOO_SAPLING,
            Material.SWEET_BERRIES
    );
    static {
        FARM_BLOCKS.addAll(Tag.SAPLINGS.getValues());
        FARM_BLOCKS_DESTROY.addAll(Tag.LOGS.getValues());
        FARM_BLOCKS_DESTROY.addAll(Tag.LEAVES.getValues());
    }

    public static boolean placesBlock(ItemStack item) {
        if (item.getType().isBlock()) {
            return true;
        }

        switch (item.getType()) {
            case BEETROOT_SEEDS:
            case MELON_SEEDS:
            case WHEAT_SEEDS:
            case PUMPKIN_SEEDS:
            case STRING:
            case ARMOR_STAND:
            case ITEM_FRAME:
            case PAINTING:
            case WATER_BUCKET:
            case LAVA_BUCKET:
                return true;
        }

        return Tag.ITEMS_BOATS.isTagged(item.getType())
                || MINECARTS.contains(item.getType())
                || MaterialTags.FISH_BUCKETS.isTagged(item);
    }

    public static Material getMaterial(@NotNull String matStr) {
        Validate.notNull(matStr, "Material string cannot be null!");
        try {
            return Material.valueOf(matStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            Material mat = Material.matchMaterial(matStr);
            if (mat == null) {
                switch (matStr.toUpperCase()) {
                    case "WALL_SIGN":
                        return Material.OAK_WALL_SIGN;
                    case "SIGN":
                        return Material.OAK_SIGN;
                }
            }
            return mat;
        }
    }
}

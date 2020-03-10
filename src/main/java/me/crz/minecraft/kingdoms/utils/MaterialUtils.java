package me.crz.minecraft.kingdoms.utils;

import com.destroystokyo.paper.MaterialTags;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
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
    public static final Set<Material> LOGS = EnumSet.of(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.ACACIA_WOOD,
            Material.BIRCH_WOOD,
            Material.DARK_OAK_WOOD,
            Material.JUNGLE_WOOD,
            Material.OAK_WOOD,
            Material.SPRUCE_WOOD
    );
    public static final Map<Material, FarmingRule> FARMING_RULES = new EnumMap<>(Material.class);
    static {
        FarmingRule farmlandRule = new FarmingRule(Material.FARMLAND);
        FARMING_RULES.put(Material.WHEAT_SEEDS, farmlandRule);
        FARMING_RULES.put(Material.CARROT, farmlandRule);
        FARMING_RULES.put(Material.POTATO, farmlandRule);
        FARMING_RULES.put(Material.PUMPKIN_SEEDS, farmlandRule);
        FARMING_RULES.put(Material.MELON_SEEDS, farmlandRule);
        FARMING_RULES.put(Material.BEETROOT_SEEDS, farmlandRule);
        FARMING_RULES.put(Material.NETHER_WART, new FarmingRule(Material.SOUL_SAND));
        FARMING_RULES.put(Material.CHORUS_PLANT, new FarmingRule(Material.END_STONE, Material.CHORUS_PLANT));
        FARMING_RULES.put(Material.CHORUS_FLOWER, new FarmingRule(Material.END_STONE, Material.CHORUS_PLANT));
        FARMING_RULES.put(Material.KELP_PLANT, new FarmingRule(new Material[] {
                Material.DIRT,
                Material.COARSE_DIRT,
                Material.SAND,
                Material.GRAVEL,
                Material.STONE,
                Material.GRANITE,
                Material.DIORITE,
                Material.ANDESITE
        }, Material.WATER));
        FARMING_RULES.put(Material.SUGAR_CANE, new FarmingRule(Material.SAND, Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL));
        FARMING_RULES.put(Material.COCOA_BEANS, new FarmingRule(Material.JUNGLE_LOG, Material.STRIPPED_JUNGLE_LOG, Material.JUNGLE_WOOD, Material.STRIPPED_JUNGLE_WOOD));
        FARMING_RULES.put(Material.SWEET_BERRIES, new FarmingRule(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL, Material.FARMLAND));
        FARM_BLOCKS.addAll(Tag.SAPLINGS.getValues());

        FarmingRule saplingRule = new FarmingRule(Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.PODZOL);
        Tag.SAPLINGS.getValues().forEach(s -> FARMING_RULES.put(s, saplingRule));
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
            case SWEET_BERRIES:
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

    public static boolean isPlanting(Material toPlant, Material onTop, Material inside) {
        return FARMING_RULES.containsKey(toPlant) && FARMING_RULES.get(toPlant).matches(onTop, inside);
    }

    private static class FarmingRule {
        private Set<Material> onTop = EnumSet.noneOf(Material.class);
        private Set<Material> inside = EnumSet.noneOf(Material.class);

        public FarmingRule(Material... onTop) {
            Collections.addAll(this.onTop, onTop);
        }

        public FarmingRule(Material[] onTop, Material... inside) {
            Collections.addAll(this.onTop, onTop);
            Collections.addAll(this.inside, inside);
        }

        public boolean matches(Material onTop, Material inside) {
            return (this.onTop.isEmpty() || this.onTop.contains(onTop))
                    && (this.inside.contains(inside) || (this.inside.isEmpty() && inside == Material.AIR));
        }
    }
}

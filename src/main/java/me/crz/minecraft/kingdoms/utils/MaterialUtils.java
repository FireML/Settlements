package me.crz.minecraft.kingdoms.utils;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

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
                return true;
        }

        return Tag.ITEMS_BOATS.isTagged(item.getType())
                || MINECARTS.contains(item.getType());
    }
}

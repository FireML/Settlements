package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ItemUtils {

    /**
     * Make an item glowing by applying the damage_all enchant and the hide_enchant flag
     * @param item      The item to set glowing
     * @param glowing   Enable or disable glowing
     * @return The item
     */
    public static ItemStack setGlowing(ItemStack item, boolean glowing) {
        ItemMeta meta = item.getItemMeta();
        if (glowing) {
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(Enchantment.DAMAGE_ALL);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Hide all item flags from an item
     * @param item The item to hide the flags from
     * @return The item
     */
    public static ItemStack hideAllFlags(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Create an ItemStack from a material which can be used as a GUI icon (with all flags hidden)
     * @param material The material
     * @return The new item with all flags hidden
     */
    public static ItemStack toIcon(Material material) {
        return hideAllFlags(new ItemStack(material));
    }

    /**
     * Get a head of a certain player by his UUID and name
     * @param playerId  The player's UUID
     * @param name      The palyer's name
     * @return The head item
     */
    public static ItemStack getHead(UUID playerId, String name) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(name);
        SettlementsPlugin.getInstance().getServerAdapter().setProfile(skull, playerId, name);
        item.setItemMeta(skull);
        return item;
    }
}

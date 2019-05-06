package me.crz.minecraft.kingdoms.integration;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.block.Block;

public interface ShopProvider {
    boolean isShop(Block block);

    static boolean isShopCheck(Block block) {
        for (ShopProvider shopProvider : KingdomsPlugin.getInstance().getShopProviders()) {
            if (shopProvider.isShop(block)) {
                return true;
            }
        }
        return false;
    }
}

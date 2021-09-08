package de.themoep.settlements.integration;

import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.block.Block;

public interface ShopProvider {
    boolean isShop(Block block);

    static boolean isShopCheck(Block block) {
        for (ShopProvider shopProvider : SettlementsPlugin.getInstance().getShopProviders()) {
            if (shopProvider.isShop(block)) {
                return true;
            }
        }
        return false;
    }
}

package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ConfirmationGui extends CInventory {

    public ConfirmationGui(ConfigMessages configMessages, String name, Runnable confirmedAction, Runnable cancelAction, String... replacements) {
        super(configMessages.getText(name + ".title", replacements), InventorySize.ONE);

        addItem(3, new ClickableItem() {
            @Override
            public ItemStack getItem(Player p0) {
                return new CItem(Material.GREEN_WOOL).setName(configMessages.getText(name + ".confirm", replacements)).build();
            }

            @Override
            public boolean click(Player p0, ClickType p1) {
                confirmedAction.run();
                return true;
            }
        });
        addItem(5, new ClickableItem() {
            @Override
            public ItemStack getItem(Player p0) {
                return new CItem(Material.RED_WOOL).setName(configMessages.getText(name + ".cancel", replacements)).build();
            }

            @Override
            public boolean click(Player p0, ClickType p1) {
                cancelAction.run();
                return true;
            }
        });
    }
}

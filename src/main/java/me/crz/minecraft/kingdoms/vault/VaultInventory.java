package me.crz.minecraft.kingdoms.vault;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

public class VaultInventory {
    private Inventory inv;
    private final int inventorySize = 54;
    public static final String inventoryName = ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Vault";

    public VaultInventory(final Player player) {
        super();
        inv = Bukkit.createInventory(null, inventorySize, inventoryName);
    }

    public Inventory getInventory() {
        return inv;
    }
}

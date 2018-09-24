package me.crz.minecraft.kingdoms.vault;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

public class VaultInventory
{
    private Inventory inv;
    private final int inventorySize = 54;
    public static final String inventoryName;
    
    static {
        inventoryName = new StringBuilder().append(ChatColor.DARK_BLUE).append(ChatColor.BOLD).append("Vault").toString();
    }
    
    public VaultInventory(final Player player) {
        super();
        inv = Bukkit.createInventory((InventoryHolder)null, 54, inventoryName);
    }
    
    public Inventory getInventory() {
        return inv;
    }
}

package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class VaultInventory extends KingdomsGUI {
    private final Player player;
    private Inventory inv;
    private final int inventorySize = 54;

    public VaultInventory(Player player) {
        super("vault");
        this.player = player;
        inv = Bukkit.createInventory(null, inventorySize, getText("title"));
    }

    public Inventory getInventory() {
        return inv;
    }

    public void open() {
        player.openInventory(inv);
    }
}

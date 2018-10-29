package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.village.KingdomsGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class EmpireVaultInventory extends KingdomsGUI {
    private final Player player;
    private Inventory inv;
    private final int inventorySize = 54;

    public EmpireVaultInventory(Player player) {
        super("empirevault");
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

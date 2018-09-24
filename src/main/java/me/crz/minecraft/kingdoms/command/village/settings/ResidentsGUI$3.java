package me.crz.minecraft.kingdoms.command.village.settings;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.OfflinePlayer;
import java.util.List;
import java.util.Arrays;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class ResidentsGUI$3 extends ClickableItem {
    final /* synthetic */ ResidentsGUI this$0;
    private final /* synthetic */ Resident val$res;
    
    ResidentsGUI$3(final ResidentsGUI this$0, final Resident val$res) {
        this$0 = this$0;
        val$res = val$res;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        final OfflinePlayer op = Bukkit.getOfflinePlayer(val$res.getUUID());
        return new CItem(Material.SKULL_ITEM).setName("&c&l" + op.getName()).setLore((List)Arrays.<String>asList(ResidentsGUI.c("&7&lRole"), ResidentsGUI.c("&b&l" + ResidentsGUI.cfc(val$res.getVillageRank().name())))).build();
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        return true;
    }
}
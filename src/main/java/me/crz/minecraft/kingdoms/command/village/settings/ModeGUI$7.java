package me.crz.minecraft.kingdoms.command.village.settings;

import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.List;
import java.util.Arrays;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class ModeGUI$7 extends ClickableItem {
    final /* synthetic */ ModeGUI this$0;
    
    ModeGUI$7(final ModeGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            return null;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        final String yon = (village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not Avalable";
        if (yon.equals("Not Avalable")) {
            return null;
        }
        return new CItem(Material.ENDER_PEARL).setName("&b&lTeleporter").setLore((List)Arrays.<String>asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + yon))).build();
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            return false;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (!village.getChief().equals(p.getUniqueId())) {
            return false;
        }
        String yon = (village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not Avalable";
        if (yon.equals("Not Avalable")) {
            return false;
        }
        village.setTeleporter(!village.isTeleporter());
        yon = ((village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not Avalable");
        MessageUtils.tell(p, "&7Teleporter State &7 changed to &6" + yon);
        gui.build(p);
        return true;
    }
}
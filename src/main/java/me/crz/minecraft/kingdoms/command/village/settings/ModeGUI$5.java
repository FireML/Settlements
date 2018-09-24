package me.crz.minecraft.kingdoms.command.village.settings;

import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.List;
import java.util.Arrays;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class ModeGUI$5 extends ClickableItem {
    final /* synthetic */ ModeGUI this$0;
    
    ModeGUI$5(final ModeGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
            return new CItem(Material.TNT).setName("&b&lExplosions").setLore((List)Arrays.<String>asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + ModeGUI.cfc(this$0.yesOrNo(village.isExplosions()))))).build();
        }
        return null;
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            return false;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (village.getChief().equals(p.getUniqueId())) {
            village.setExplosions(!village.isExplosions());
            MessageUtils.tell(p, "&7Explosion State &7 changed to &6" + this$0.yesOrNo(village.isExplosions()));
            gui.build(p);
            return true;
        }
        return false;
    }
}
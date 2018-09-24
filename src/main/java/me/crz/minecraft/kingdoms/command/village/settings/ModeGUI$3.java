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

class ModeGUI$3 extends ClickableItem {
    final /* synthetic */ ModeGUI this$0;
    
    ModeGUI$3(final ModeGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
            return new CItem(Material.SKULL_ITEM).setName("&b&lMobSpawning").setLore((List)Arrays.<String>asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + ModeGUI.cfc(this$0.yesOrNo(village.isMobSpawning()))))).build();
        }
        return null;
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            return false;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (village.getChief().equals(p.getUniqueId())) {
            village.setMobSpawning(!village.isMobSpawning());
            MessageUtils.tell(p, "&7MobSpawning State &7 changed to &6" + this$0.yesOrNo(village.isMobSpawning()));
            gui.build(p);
            return true;
        }
        return false;
    }
}
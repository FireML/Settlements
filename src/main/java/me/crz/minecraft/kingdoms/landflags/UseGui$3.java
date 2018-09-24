package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.village.BuildState;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class UseGui$3 extends ClickableItem {
    final /* synthetic */ UseGui this$0;
    private final /* synthetic */ Use val$u;
    
    UseGui$3(final UseGui this$0, final Use val$u) {
        this$0 = this$0;
        val$u = val$u;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
            final CItem item = val$u.getItem();
            item.setLore((List)new ArrayList(Arrays.<String>asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + UseGui.cfc(village.getUseState(val$u).name())))));
            return item.build();
        }
        return null;
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            return false;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (village.getChief().equals(p.getUniqueId())) {
            village.setUseState(val$u, BuildState.next(village.getUseState(val$u)));
            MessageUtils.tell(p, "&7Use state &6" + val$u.name().toLowerCase() + "&7 changed to &6" + village.getUseState(val$u));
            gui.build(p);
            return true;
        }
        return false;
    }
}
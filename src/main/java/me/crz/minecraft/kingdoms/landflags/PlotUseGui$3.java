package me.crz.minecraft.kingdoms.landflags;

import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class PlotUseGui$3 extends ClickableItem {
    final /* synthetic */ PlotUseGui this$0;
    private final /* synthetic */ Use val$u;
    
    PlotUseGui$3(final PlotUseGui this$0, final Use val$u) {
        this$0 = this$0;
        val$u = val$u;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(p.getLocation());
            final CItem item = val$u.getItem();
            item.setLore((List)new ArrayList(Arrays.<String>asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + PlotUseGui.cfc(land.getUseState(val$u).name())))));
            return item.build();
        }
        return null;
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
            return false;
        }
        final OwnedLand land = LandManager.getInstance().getLandFromLocation(p.getLocation());
        if (land.getPlotOwner().equals(p.getUniqueId().toString())) {
            land.setUseState(val$u, PlotBuildState.next(land.getUseState(val$u)));
            LandManager.getInstance().saveLand(land);
            MessageUtils.tell(p, "&7Use state &6" + val$u.name().toLowerCase() + "&7 changed to &6" + land.getUseState(val$u));
            gui.build(p);
            return true;
        }
        return false;
    }
}
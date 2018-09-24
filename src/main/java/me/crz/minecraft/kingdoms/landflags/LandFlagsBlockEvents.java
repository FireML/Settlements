package me.crz.minecraft.kingdoms.landflags;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.event.block.BlockBreakEvent;

public class LandFlagsBlockEvents extends LandFlagBase
{
    public LandFlagsBlockEvents() {
        super();
    }
    
    @EventHandler
    public void on(final BlockBreakEvent e) {
        final Pair<Boolean, String> responce = this.getCanBuildAll(e.getPlayer(), e.getBlock().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (e.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK) && Arrays.<Material>asList(Material.BOAT_ACACIA, Material.BOAT_BIRCH, Material.BOAT_DARK_OAK, Material.BOAT_JUNGLE, Material.BOAT, Material.BOAT_SPRUCE, Material.ITEM_FRAME, Material.PAINTING, Material.ARMOR_STAND).contains(e.getPlayer().getItemInHand().getType())) {
            final Pair<Boolean, String> responce = this.getCanBuildAll(e.getPlayer(), e.getClickedBlock().getLocation());
            if (!(boolean)responce.getLeft()) {
                MessageUtils.tell(e.getPlayer(), responce.getRight());
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void on(final BlockPlaceEvent e) {
        final Pair<Boolean, String> responce = this.getCanBuildAll(e.getPlayer(), e.getBlock().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            final Player p = (Player)e.getRemover();
            final Pair<Boolean, String> responce = this.getCanBuildAll(p, e.getEntity().getLocation());
            if (!(boolean)responce.getLeft()) {
                MessageUtils.tell(p, responce.getRight());
                e.setCancelled(true);
                return;
            }
        }
        if (e.getRemover() instanceof Arrow) {
            if (!this.isLandClaimed(e.getEntity().getLocation())) {
                return;
            }
            e.setCancelled(true);
        }
    }
}

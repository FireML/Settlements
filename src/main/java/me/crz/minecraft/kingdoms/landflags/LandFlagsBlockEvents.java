package me.crz.minecraft.kingdoms.landflags;

import org.bukkit.Tag;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.event.block.BlockBreakEvent;

public class LandFlagsBlockEvents extends LandFlagBase {
    public LandFlagsBlockEvents() {
        super();
    }

    @EventHandler
    public void on(final BlockBreakEvent e) {
        final Pair<Boolean, String> responce = this.getCanBuildAll(e.getPlayer(), e.getBlock().getLocation());
        if (!responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().getEquipment().getItemInMainHand() != null) {
            Material mat = e.getPlayer().getEquipment().getItemInMainHand().getType();
            if (Tag.ITEMS_BOATS.isTagged(mat) || mat == Material.ITEM_FRAME || mat == Material.PAINTING || mat == Material.ARMOR_STAND) {
                final Pair<Boolean, String> response = this.getCanBuildAll(e.getPlayer(), e.getClickedBlock().getLocation());
                if (!response.getLeft()) {
                    MessageUtils.tell(e.getPlayer(), response.getRight());
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void on(final BlockPlaceEvent e) {
        final Pair<Boolean, String> response = this.getCanBuildAll(e.getPlayer(), e.getBlock().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            final Player p = (Player) e.getRemover();
            final Pair<Boolean, String> response = this.getCanBuildAll(p, e.getEntity().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(p, response.getRight());
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

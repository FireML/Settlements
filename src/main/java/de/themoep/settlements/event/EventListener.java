package de.themoep.settlements.event;

import de.themoep.settlements.customevents.PlayerMoveBlockEvent;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (EventManager.getInstance().getActiveBar() != null) {
            EventManager.getInstance().getActiveBar().addPlayer(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        EventManager.getInstance().handle(event.getPlayer(), Event.Action.PLACE, event.getBlockPlaced().getType().toString(), event.getItemInHand().getType().toString());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (shouldBreakCount(event.getPlayer().getEquipment().getItemInMainHand(), event.getBlock())) {
            EventManager.getInstance().handle(event.getPlayer(), Event.Action.BREAK, event.getBlock().getType().toString());
        }
    }

    /**
     * Check if a certain block break should be counted towards the event.
     * This is done in order to avoid cheating with silk touch
     * @param tool  The tool used to break the block
     * @param block The block broken
     * @return <tt>true</tt> if the event should count, <tt>false</tt> if not
     */
    private boolean shouldBreakCount(ItemStack tool, Block block) {
        for (ItemStack drop : block.getDrops()) {
            if (drop.getType() == block.getType()) {
                return true;
            }
        }

        return !tool.getEnchantments().containsKey(Enchantment.SILK_TOUCH);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getCurrentItem() != null) {
            EventManager.getInstance().handle((Player) event.getWhoClicked(), Event.Action.CRAFT, event.getCurrentItem().getType().toString());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            EventManager.getInstance().handle(event.getEntity().getKiller(), Event.Action.KILL, event.getEntityType().toString());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveBlockEvent event) {
        Player player = event.getPlayer();
        String type = "walk";
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getFrom().getBlockZ()) {
            if (event.getFrom().getBlockY() > event.getFrom().getBlockY()) {
                type = "fall";
            } else if (event.getFrom().getBlockY() > event.getFrom().getBlockY()) {
                type = "rise";
            } else {
                return;
            }
        } else if (player.isSneaking()) {
            type = "sneak";
        } else if (player.isSprinting()) {
            type = "run";
        }

        if (player.isInsideVehicle() && player.getVehicle() != null) {
            type = player.getVehicle().getType().toString();
        } else {
            if (player.isRiptiding()) {
                type = "riptide";
            } else if (player.isGliding()) {
                type = "elytra";
            } else if (player.isSwimming()) {
                type = "swim";
            }
        }
        EventManager.getInstance().handle(event.getPlayer(), Event.Action.DISTANCE, type);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            EventManager.getInstance().handle(event.getPlayer(), Event.Action.DISTANCE, (int) event.getFrom().distance(event.getTo()), "enderpearl");
        }
    }
}

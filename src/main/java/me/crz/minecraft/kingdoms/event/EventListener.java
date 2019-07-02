package me.crz.minecraft.kingdoms.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;

public class EventListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        EventManager.getInstance().handle(event.getPlayer(), Event.Action.PLACE, event.getBlockPlaced().getType().toString(), event.getItemInHand().getType().toString());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        EventManager.getInstance().handle(event.getPlayer(), Event.Action.BREAK, event.getBlock().getType().toString());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getCurrentItem() != null) {
            EventManager.getInstance().handle((Player) event.getWhoClicked(), Event.Action.CRAFT, event.getCurrentItem().getType().toString());
        }
    }
}

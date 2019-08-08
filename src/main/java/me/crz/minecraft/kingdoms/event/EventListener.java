package me.crz.minecraft.kingdoms.event;

import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

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
}

package me.crz.minecraft.kingdoms.landflags;

import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import java.util.Arrays;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.spigotmc.event.entity.EntityMountEvent;

public class LandFlagsUseEvents extends LandFlagBase
{
    public LandFlagsUseEvents() {
        super();
    }
    
    @EventHandler
    public void on(final EntityMountEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player p = (Player)e.getEntity();
            final Pair<Boolean, String> responce = this.canUse(p, Use.RIDE, e.getMount().getLocation());
            if (!(boolean)responce.getLeft()) {
                MessageUtils.tell(p, responce.getRight());
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void on(final PlayerPortalEvent e) {
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.PORTAL, e.getFrom());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerBedEnterEvent e) {
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.BED, e.getBed().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerPickupItemEvent e) {
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.ITEM, e.getPlayer().getLocation());
        if (!(boolean)responce.getLeft()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerFishEvent e) {
        if (e.getPlayer() == null || e.getHook() == null) {
            return;
        }
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.FISH, e.getHook().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerArmorStandManipulateEvent e) {
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.ARMOR_STAND, e.getRightClicked().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerLeashEntityEvent e) {
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.LEASH, e.getEntity().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerShearEntityEvent e) {
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.SHEER, e.getEntity().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerInteractEntityEvent e) {
        final Player p = e.getPlayer();
        if (e.getRightClicked() instanceof Cow && p.getItemInHand().getType().equals((Object)Material.BUCKET)) {
            final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.MILK, e.getRightClicked().getLocation());
            if (!(boolean)responce.getLeft()) {
                MessageUtils.tell(e.getPlayer(), responce.getRight());
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (e.getAction().equals((Object)Action.PHYSICAL) || e.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK) || e.getAction().equals((Object)Action.LEFT_CLICK_BLOCK)) {
            Use use = Use.INTERACT;
            if (e.getAction().equals((Object)Action.PHYSICAL) && e.getClickedBlock().getType().name().contains("PRESSURE_PLATE")) {
                use = Use.PRESSURE_PLATE;
            }
            if (e.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK) || e.getAction().equals((Object)Action.LEFT_CLICK_BLOCK)) {
                if (Arrays.<Material>asList(Material.FURNACE).contains(e.getClickedBlock().getType())) {
                    use = Use.FURNACE;
                }
                if (Arrays.<Material>asList(Material.CHEST, Material.TRAPPED_CHEST).contains(e.getClickedBlock().getType())) {
                    use = Use.CHEST;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.HOPPER)) {
                    use = Use.HOPPER;
                }
                if (e.getClickedBlock().getType().name().contains("SHULKER_BOX")) {
                    use = Use.SHULKER_BOX;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.DROPPER)) {
                    use = Use.DROPPER;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.ITEM_FRAME)) {
                    use = Use.ITEM_FRAME;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.BEACON)) {
                    use = Use.BECON;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.CAKE)) {
                    use = Use.CAKE;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.BREWING_STAND)) {
                    use = Use.BREWING_STAND;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.DISPENSER)) {
                    use = Use.DISPENSER;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.JUKEBOX)) {
                    use = Use.JUKEBOX;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.WORKBENCH)) {
                    use = Use.CRAFTING_TABLE;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.LEVER)) {
                    use = Use.LEVER;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.REDSTONE_COMPARATOR)) {
                    use = Use.REPEATERS;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.ARMOR_STAND)) {
                    use = Use.ARMOR_STAND;
                }
                if (e.getClickedBlock().getType().name().contains("BUTTON")) {
                    use = Use.BUTTON;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.ANVIL)) {
                    use = Use.ANVIL;
                }
                if (e.getClickedBlock().getType().equals((Object)Material.ENCHANTMENT_TABLE)) {
                    use = Use.ENCHANTMENT_TABLE;
                }
                if (e.getClickedBlock().getType().name().contains("TRAPDOOR")) {
                    use = Use.TRAP_DOOR;
                }
                if (Arrays.<Material>asList(Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.IRON_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR, Material.WOOD_DOOR).contains(e.getClickedBlock().getType())) {
                    use = Use.DOOR;
                }
                if (Arrays.<Material>asList(Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE, Material.FENCE_GATE).contains(e.getClickedBlock().getType())) {
                    use = Use.GATE;
                }
            }
            if (use != Use.INTERACT) {
                final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), use, e.getClickedBlock().getLocation());
                if (!(boolean)responce.getLeft()) {
                    if (use != Use.PRESSURE_PLATE) {
                        MessageUtils.tell(e.getPlayer(), responce.getRight());
                    }
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void on(final PlayerBucketFillEvent e) {
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.BUCKET, e.getBlockClicked().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerBucketEmptyEvent e) {
        final Pair<Boolean, String> responce = this.canUse(e.getPlayer(), Use.BUCKET, e.getBlockClicked().getLocation());
        if (!(boolean)responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }
}

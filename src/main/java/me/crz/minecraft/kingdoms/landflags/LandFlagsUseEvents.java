package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

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

public class LandFlagsUseEvents extends LandFlagBase {
    public LandFlagsUseEvents() {
        super();
    }

    @EventHandler
    public void on(final EntityMountEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player p = (Player) e.getEntity();
            final Pair<Boolean, String> response = this.canUse(p, Use.RIDE, e.getMount().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(p, response.getRight());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(final PlayerPortalEvent e) {
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.PORTAL, e.getFrom());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerBedEnterEvent e) {
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BED, e.getBed().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerPickupItemEvent e) {
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.ITEM, e.getPlayer().getLocation());
        if (!response.getLeft()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerFishEvent e) {
        if (e.getPlayer() == null || e.getHook() == null) {
            return;
        }
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.FISH, e.getHook().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerArmorStandManipulateEvent e) {
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.ARMOR_STAND, e.getRightClicked().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerLeashEntityEvent e) {
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.LEASH, e.getEntity().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerShearEntityEvent e) {
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.SHEER, e.getEntity().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerInteractEntityEvent e) {
        final Player p = e.getPlayer();
        if (e.getRightClicked() instanceof Cow && p.getItemInHand().getType() == Material.BUCKET) {
            final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.MILK, e.getRightClicked().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(e.getPlayer(), response.getRight());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(final PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL || e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Use use = Use.INTERACT;
            if (e.getAction() == Action.PHYSICAL && MaterialUtils.PRESSURE_PLATES.contains(e.getClickedBlock().getType())) {
                use = Use.PRESSURE_PLATE;
            }
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                use = Use.getByMaterial(e.getClickedBlock().getType());
            }
            if (use != Use.INTERACT) {
                final Pair<Boolean, String> response = this.canUse(e.getPlayer(), use, e.getClickedBlock().getLocation());
                if (!response.getLeft()) {
                    if (use != Use.PRESSURE_PLATE) {
                        MessageUtils.tell(e.getPlayer(), response.getRight());
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void on(final PlayerBucketFillEvent e) {
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BUCKET, e.getBlockClicked().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(final PlayerBucketEmptyEvent e) {
        final Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BUCKET, e.getBlockClicked().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }
}

package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.essentials.events.PassiveCreatureCombustEvent;
import me.crz.minecraft.kingdoms.essentials.events.PassiveCreatureDamageEvent;
import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

public class LandFlagsUseEvents implements LandFlagBase, Listener {

    @EventHandler
    public void on(EntityMountEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Pair<Boolean, String> response = this.canUse(p, Use.RIDE, e.getMount().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(p, response.getRight());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(PlayerPortalEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.PORTAL, e.getFrom());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerBedEnterEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BED, e.getBed().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerPickupItemEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.ITEM, e.getPlayer().getLocation());
        if (!response.getLeft()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerFishEvent e) {
        if (e.getPlayer() == null || e.getHook() == null) {
            return;
        }
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.FISH, e.getHook().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerArmorStandManipulateEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.ARMOR_STAND, e.getRightClicked().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerLeashEntityEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.LEASH, e.getEntity().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerShearEntityEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.SHEER, e.getEntity().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (e.getRightClicked() instanceof Cow && p.getItemInHand().getType() == Material.BUCKET) {
            Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.MILK, e.getRightClicked().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(e.getPlayer(), response.getRight());
                e.setCancelled(true);
            }
        } else if (e.getRightClicked() instanceof ItemFrame) {
            Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.ITEM_FRAME, e.getRightClicked().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(e.getPlayer(), response.getRight());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Use use;
            if (e.getAction() == Action.PHYSICAL && MaterialUtils.PRESSURE_PLATES.contains(e.getClickedBlock().getType())) {
                use = Use.PRESSURE_PLATE;
            } else {
                use = Use.getByMaterial(e.getClickedBlock().getType());
            }
            if (use == null) {
                use = Use.INTERACT;
            }
            Pair<Boolean, String> response = this.canUse(e.getPlayer(), use, e.getClickedBlock().getLocation());
            if (!response.getLeft()) {
                if (use != Use.PRESSURE_PLATE) {
                    MessageUtils.tell(e.getPlayer(), response.getRight());
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void on(PlayerBucketFillEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BUCKET, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerBucketEmptyEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BUCKET, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame) {
            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                Pair<Boolean, String> response = this.canUse(p, Use.ITEM_FRAME, e.getEntity().getLocation());
                if (!response.getLeft()) {
                    MessageUtils.tell(p, response.getRight());
                    e.setCancelled(true);
                }
            } else if (e.getDamager() instanceof Projectile) {
                if (this.isLandClaimed(e.getEntity().getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPassiveDamage(PassiveCreatureDamageEvent e) {
        if (e.getEntity() == null) {
            return;
        }
        if (e.getDamager() == null) {
            return;
        }
        Pair<Boolean, String> response = this.canUse(e.getDamager(), Use.ANIMAL, e.getEntity().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getDamager(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPassiveCombust(PassiveCreatureCombustEvent e) {
        if (e.getEntity() == null) {
            return;
        }
        if (e.getCombuster() == null) {
            return;
        }
        if (!this.canUse(e.getCombuster(), Use.ANIMAL, e.getEntity().getLocation()).getLeft()) {
            e.setCancelled(true);
        }
    }
}

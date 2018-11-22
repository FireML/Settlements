package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.essentials.events.PassiveCreatureDamageEvent;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class LandFlagsBlockEvents implements LandFlagBase, Listener {
    public LandFlagsBlockEvents() {
        super();
    }

    @EventHandler
    public void on(BlockBreakEvent e) {
        Pair<Boolean, String> responce = this.getCanBuildAll(e.getPlayer(), e.getBlock().getLocation());
        if (!responce.getLeft()) {
            MessageUtils.tell(e.getPlayer(), responce.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().getEquipment().getItemInMainHand() != null) {
            Material mat = e.getPlayer().getEquipment().getItemInMainHand().getType();
            if (Tag.ITEMS_BOATS.isTagged(mat) || mat == Material.ITEM_FRAME || mat == Material.PAINTING || mat == Material.ARMOR_STAND) {
                Pair<Boolean, String> response = this.getCanBuildAll(e.getPlayer(), e.getClickedBlock().getLocation());
                if (!response.getLeft()) {
                    MessageUtils.tell(e.getPlayer(), response.getRight());
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void on(BlockPlaceEvent e) {
        Pair<Boolean, String> response = this.getCanBuildAll(e.getPlayer(), e.getBlock().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            Player p = (Player) e.getRemover();
            Pair<Boolean, String> response = this.getCanBuildAll(p, e.getEntity().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(p, response.getRight());
                e.setCancelled(true);
            }
        } else if (e.getRemover() instanceof Projectile) {
            if (this.isLandClaimed(e.getEntity().getLocation())) {
                e.setCancelled(true);
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
        if (!getCanBuildAll(e.getDamager(), e.getEntity().getLocation()).getLeft()) {
            e.setCancelled(true);
            e.getDamager().sendActionBar(getText("cannot-attack-animals"));
        }
    }

    @EventHandler
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            if (e.getEntity() instanceof Player) {
                if (!getCanBuildAll((Player) e.getEntity(), e.getEntity().getLocation()).getLeft()) {
                    e.setCancelled(true);
                }
            } else if (Utils.isHostile((LivingEntity) e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.getEntity() instanceof LivingEntity && Utils.isHostile((LivingEntity) e.getEntity())) {
            e.setCancelled(true);
        }
    }
}

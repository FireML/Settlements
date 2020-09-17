package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.customevents.PassiveCreatureCombustEvent;
import me.crz.minecraft.kingdoms.customevents.PassiveCreatureDamageEvent;
import me.crz.minecraft.kingdoms.integration.ShopProvider;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Tag;
import org.bukkit.block.Block;
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
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.spigotmc.event.entity.EntityMountEvent;

public class LandFlagsUseEvents implements LandFlagBase, Listener {

    @EventHandler(ignoreCancelled = true)
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

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerPortalEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.PORTAL, e.getFrom());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerBedEnterEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BED, e.getBed().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerPickupItemEvent e) {
        if (MaterialUtils.FARM_ITEMS.contains(e.getItem().getItemStack().getType())
                || MaterialUtils.FARM_BLOCKS.contains(e.getItem().getItemStack().getType())
                || MaterialUtils.FARM_BLOCKS_DESTROY.contains(e.getItem().getItemStack().getType())) {
            if (this.canUse(e.getPlayer(), Tag.SAPLINGS.isTagged(e.getItem().getItemStack().getType()) ? Use.TREES : Use.FARMING, e.getPlayer().getLocation()).getLeft()) {
                return;
            }
        }
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.ITEM, e.getPlayer().getLocation());
        if (!response.getLeft()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
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

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerArmorStandManipulateEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.ARMOR_STAND, e.getRightClicked().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerLeashEntityEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.LEASH, e.getEntity().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerShearEntityEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.SHEER, e.getEntity().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (e.getRightClicked() instanceof Cow
                && (e.getHand() == EquipmentSlot.HAND
                        ? p.getEquipment().getItemInMainHand().getType()
                        : p.getEquipment().getItemInOffHand()) == Material.BUCKET) {
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void on(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && (e.getAction() == Action.PHYSICAL
                || e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK)) {
            Use use = null;
            if (e.getAction() == Action.PHYSICAL && Tag.PRESSURE_PLATES.isTagged(e.getClickedBlock().getType())) {
                use = Use.PRESSURE_PLATE;
            } else {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    for (ShopProvider provider : KingdomsPlugin.getInstance().getShopProviders()) {
                        if (provider.isShop(e.getClickedBlock())) {
                            use = Use.SHOP;
                            break;
                        }
                    }
                }
                if (e.getAction() != Action.LEFT_CLICK_BLOCK && use == null && e.getClickedBlock() != null && !e.getClickedBlock().isEmpty() && !e.getClickedBlock().isLiquid()) {
                    use = Use.getByMaterial(e.getClickedBlock().getType());
                    if ((e.getItem() != null && MaterialUtils.isPlanting(e.getItem().getType(), e.getClickedBlock().getType(), e.getClickedBlock().getRelative(e.getBlockFace()).getType()))
                            || (e.getMaterial() == Material.BONE_MEAL && MaterialUtils.FARM_BLOCKS.contains(e.getClickedBlock().getType()))
                            || (MaterialUtils.HOES.contains(e.getMaterial())
                                    && (e.getClickedBlock().getType() == Material.DIRT || e.getClickedBlock().getType() == Material.GRASS_BLOCK))) {
                        if (!this.getCanBuildAll(e.getPlayer(), e.getClickedBlock().getLocation()).getLeft()) {
                            use = Tag.SAPLINGS.isTagged(e.getItem().getType()) || Tag.SAPLINGS.isTagged(e.getClickedBlock().getType()) ? Use.TREES : Use.FARMING;
                        }
                    } else if (use == null) {
                        use = Use.INTERACT;
                        if (e.getItem() != null) {
                            if (e.getItem().getType() == Material.BUCKET) {
                                Block block = e.getPlayer().getTargetBlockExact(6, FluidCollisionMode.SOURCE_ONLY);
                                if (block != null && block.isLiquid()) {
                                    use = null;
                                }
                            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && MaterialUtils.placesBlock(e.getItem())) { // handle as build
                                use = null;
                            }
                        }
                    }
                }
            }
            if (use != null) {
                Pair<Boolean, String> response = this.canUse(e.getPlayer(), use, e.getClickedBlock().getLocation());
                if (!response.getLeft()) {
                    if (use != Use.PRESSURE_PLATE && e.getHand() == EquipmentSlot.HAND
                            && (use != Use.CHEST || LockManager.getInstance().getLock(e.getClickedBlock()) == null)) {
                        MessageUtils.tell(e.getPlayer(), response.getRight());
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerBucketFillEvent e) {
        if (e.getItemStack() != null && e.getItemStack().getType() == Material.MILK_BUCKET) {
            return;
        }
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BUCKET, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
            e.getPlayer().updateInventory();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerBucketEmptyEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.BUCKET, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), response.getRight());
            e.setCancelled(true);
            e.getPlayer().updateInventory();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame) {
            Player p = null;
            if (e.getDamager() instanceof Player) {
                p = (Player) e.getDamager();
            } else if (e.getDamager() instanceof Projectile) {
                if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
                    p = (Player) ((Projectile) e.getDamager()).getShooter();
                } else if (this.isLandClaimed(e.getEntity().getLocation())) {
                    e.setCancelled(true);
                }
            }
            if (p != null) {
                Pair<Boolean, String> response = this.canUse(p, Use.ITEM_FRAME, e.getEntity().getLocation());
                if (!response.getLeft()) {
                    MessageUtils.tell(p, response.getRight());
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
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

    @EventHandler(ignoreCancelled = true)
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

    @EventHandler(ignoreCancelled = true)
    public void onLecternBookTake(PlayerTakeLecternBookEvent e) {
        Pair<Boolean, String> response = this.canUse(e.getPlayer(), Use.LECTERN, e.getLectern().getLocation());
        if (!response.getLeft()) {
            e.setCancelled(true);
            MessageUtils.tell(e.getPlayer(), response.getRight());
        }
    }
}

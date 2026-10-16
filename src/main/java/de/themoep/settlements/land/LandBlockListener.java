package de.themoep.settlements.land;

import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.MaterialUtils;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Pair;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LandBlockListener implements LandAccessChecker, Listener {

    @EventHandler(ignoreCancelled = true)
    public void on(BlockBreakEvent e) {
        Pair<Boolean, String> response = this.getCanBuildAll(e.getPlayer(), e.getBlock().getLocation());
        if (response.getLeft()) {
            return;
        }

        Use useFlag = null;

        if ((MaterialUtils.FARM_BLOCKS.contains(e.getBlock().getType()) || MaterialUtils.FARM_BLOCKS_DESTROY.contains(e.getBlock().getType()))) {
            useFlag = Use.FARMING;
        }

        if (Tag.SAPLINGS.isTagged(e.getBlock().getType()) || MaterialUtils.LOGS.contains(e.getBlock().getType())) {
            useFlag = Use.TREES;
        } else if (Tag.LEAVES.isTagged(e.getBlock().getType()) && !((Leaves) e.getBlock().getBlockData()).isPersistent()) {
            useFlag = Use.TREES;
        }

        if (useFlag != null && this.canUse(e.getPlayer(), useFlag, e.getBlock().getLocation()).getLeft()) {
            return;
        }

        MessageUtils.tell(e.getPlayer(), ComponentUtils.parseComponent(response.getRight()));
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(TNTPrimeEvent e) {
        Player player = null;
        if (e.getCause() == TNTPrimeEvent.PrimeCause.PROJECTILE && e.getPrimingEntity() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player shooter) {
                player = shooter;
            } else if (source instanceof Entity || projectile.getShooter() == null) {
                // directly cancel any shooter that isn't a player
                OwnedLand land = LandManager.getInstance().getLand(e.getBlock().getLocation());
                if (land != null) {
                    e.setCancelled(true);
                }
            } else if (source instanceof BlockProjectileSource projsource) {
                OwnedLand land = LandManager.getInstance().getLand(e.getBlock().getLocation());
                if (land != null) {
                    OwnedLand originLand = LandManager.getInstance().getLand(projsource.getBlock().getLocation());
                    if (originLand == null || !land.isOwnedBy(originLand.getLandOwner())) {
                        e.setCancelled(true);
                    }
                }
            }
        } else if (e.getCause() == TNTPrimeEvent.PrimeCause.PLAYER && e.getPrimingEntity() instanceof Player primer) {
            player = primer;
        }
        if (player != null) {
            Pair<Boolean, String> response = this.getCanBuildAll(player, e.getBlock().getLocation());
            if (!response.getLeft()) {
                MessageUtils.tell(player, ComponentUtils.parseComponent(response.getRight()));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.hasItem()) {
            Material mat = e.getMaterial();
            if (Tag.ITEMS_BOATS.isTagged(mat) || MaterialUtils.MINECARTS.contains(mat)
                    || mat == Material.ITEM_FRAME || mat == Material.PAINTING
                    || mat == Material.ARMOR_STAND || mat == Material.END_CRYSTAL) {
                Pair<Boolean, String> response = this.getCanBuildAll(e.getPlayer(), e.getClickedBlock().getRelative(e.getBlockFace()).getLocation());
                if (!response.getLeft()) {
                    if (e.getHand() == EquipmentSlot.HAND) {
                        MessageUtils.tell(e.getPlayer(), ComponentUtils.parseComponent(response.getRight()));
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(BlockPlaceEvent e) {
        Use useFlag = null;

        if (MaterialUtils.FARM_BLOCKS.contains(e.getBlock().getType()) || e.getBlockPlaced().getType() == Material.FARMLAND) {
            useFlag = Use.FARMING;
        }

        if (Tag.SAPLINGS.isTagged(e.getBlock().getType())) {
            useFlag = Use.TREES;
        }

        if (useFlag != null && this.canUse(e.getPlayer(), useFlag, e.getBlock().getLocation()).getLeft()) {
            return;
        }

        Pair<Boolean, String> response = this.getCanBuildAll(e.getPlayer(), e.getBlock().getLocation());
        if (!response.getLeft()) {
            MessageUtils.tell(e.getPlayer(), ComponentUtils.parseComponent(response.getRight()));
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(HangingBreakByEntityEvent e) {
        if (shouldCancel(e.getEntity(), e.getRemover())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(EntityDamageByEntityEvent e) {
        if (shouldCancel(e.getEntity(), e.getDamager())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(EntityCombustByEntityEvent e) {
        if (shouldCancel(e.getEntity(), e.getCombuster())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(VehicleDamageEvent e) {
        if (shouldCancel(e.getVehicle(), e.getAttacker())) {
            e.setCancelled(true);
        }
    }

    private boolean shouldCancel(Entity damaged, Entity damager) {
        if (damaged instanceof Hanging || damaged instanceof ArmorStand
                || damaged instanceof Boat || damaged instanceof Minecart
                || damaged instanceof EnderCrystal) {
            Player p = null;
            if (damager instanceof Player playerDamager) {
                p = playerDamager;
            } else if (damager instanceof Projectile projectileDamager) {
                if (projectileDamager.getShooter() instanceof Player playerProjDamager) {
                    p = playerProjDamager;
                } else if (this.isLandClaimed(damaged.getLocation())) {
                    return true;
                }
            }
            if (p != null) {
                Pair<Boolean, String> response = this.getCanBuildAll(p, damaged.getLocation());
                if (!response.getLeft()) {
                    MessageUtils.tell(p, ComponentUtils.parseComponent(response.getRight()));
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            if (e.getEntity() instanceof Player) {
                if (!getCanBuildAll((Player) e.getEntity(), e.getBlock().getLocation()).getLeft()) {
                    e.setCancelled(true);
                }
            } else if (Utils.isHostile(e.getEntity())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.getEntity() instanceof LivingEntity && Utils.isHostile(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    private void handlePistonEvent(BlockPistonEvent event, List<Block> blocks) {
        Set<SimpleChunkLocation> chunks = new LinkedHashSet<>();
        chunks.add(new SimpleChunkLocation(event.getBlock().getLocation()));
        chunks.add(new SimpleChunkLocation(event.getBlock().getRelative(event.getDirection()).getLocation()));
        for (Block block : blocks) {
            chunks.add(new SimpleChunkLocation(block.getLocation()));
            chunks.add(new SimpleChunkLocation(block.getRelative(event.getDirection()).getLocation()));
        }
        if (chunks.size() <= 1) {
            return;
        }
        if (checkChunks(chunks)) {
            event.setCancelled(true);
        }
    }

    private boolean checkChunks(Collection<SimpleChunkLocation> chunks) {
        LandOwner<?, ?> landOwner = null;
        UUID plotOwner = null;
        List<SimpleChunkLocation> chunkList = new ArrayList<>(chunks);
        Collections.reverse(chunkList);
        for (SimpleChunkLocation chunk : chunkList) {
            OwnedLand land = LandManager.getInstance().getLand(chunk);
            if (land != null) {
                if (land.getLandOwner().getBuildState().above(BuildState.PUBLIC)) {
                    if (landOwner != null && !land.getLandOwner().equals(landOwner)) {
                        return true;
                    }
                    landOwner = land.getLandOwner();
                }
                if (land.isPlot()) {
                    if (land.getBuildState() != null && land.getBuildState().above(BuildState.PUBLIC)) {
                        if (plotOwner != null && !land.getPlotOwner().equals(plotOwner)) {
                            return true;
                        }
                        plotOwner = land.getPlotOwner();
                    }
                } else if (plotOwner != null) {
                    return true;
                }
            } else if (landOwner != null) {
                return true;
            }
        }
        return false;
    }
}

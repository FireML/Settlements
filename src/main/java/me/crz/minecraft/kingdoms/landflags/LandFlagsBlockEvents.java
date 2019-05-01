package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LandFlagsBlockEvents implements LandFlagBase, Listener {

    public LandFlagsBlockEvents() {
        super();
    }

    @EventHandler
    public void on(BlockBreakEvent e) {
        if ((MaterialUtils.FARM_BLOCKS.contains(e.getBlock().getType()) || MaterialUtils.FARM_BLOCKS_DESTROY.contains(e.getBlock().getType()))
                && this.canUse(e.getPlayer(), Use.FARMING, e.getBlock().getLocation()).getLeft()) {
            return;
        }

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
            if (Tag.ITEMS_BOATS.isTagged(mat) || MaterialUtils.MINECARTS.contains(mat)
                    || mat == Material.ITEM_FRAME || mat == Material.PAINTING || mat == Material.ARMOR_STAND) {
                Pair<Boolean, String> response = this.getCanBuildAll(e.getPlayer(), e.getClickedBlock().getRelative(e.getBlockFace()).getLocation());
                if (!response.getLeft()) {
                    MessageUtils.tell(e.getPlayer(), response.getRight());
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void on(BlockPlaceEvent e) {
        if ((MaterialUtils.FARM_BLOCKS.contains(e.getBlockPlaced().getType()) || e.getBlockPlaced().getType() == Material.FARMLAND)
                && this.canUse(e.getPlayer(), Use.FARMING, e.getBlock().getLocation()).getLeft()) {
            return;
        }

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
    public void onEntityBlockForm(EntityBlockFormEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            if (e.getEntity() instanceof Player) {
                if (!getCanBuildAll((Player) e.getEntity(), e.getBlock().getLocation()).getLeft()) {
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

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event, event.getBlocks());
    }

    @EventHandler
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
        LandOwner landOwner = null;
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
                    if (land.getBuildState().above(BuildState.PUBLIC)) {
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

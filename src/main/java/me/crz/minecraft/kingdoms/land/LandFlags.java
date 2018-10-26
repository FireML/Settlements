package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.essentials.events.HostileCreatureSpawnEvent;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveChunkEvent;
import me.crz.minecraft.kingdoms.landflags.LandFlagBase;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;

import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.essentials.events.PassiveCreatureDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;

import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.Base;

public class LandFlags extends Base implements LandFlagBase {
    private TitleBuilder tbPvp = new TitleBuilder();

    public LandFlags() {
        super();
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getCause().equals(BlockIgniteEvent.IgniteCause.SPREAD) && e.getIgnitingBlock() != null) {
            OwnedLand land = LandManager.getInstance().getLand(e.getIgnitingBlock().getLocation());
            if (land == null) {
                return;
            }
            if (!land.isFireSpread()) {
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
            e.getDamager().spigot().sendMessage(ChatMessageType.ACTION_BAR, Message.get("cannot-attack-animals").getComponents());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if ((e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
                && e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            OwnedLand land = LandManager.getInstance().getLand(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            if (e.getDamager().hasPermission("kindoms.staff")) {
                return;
            }
            if (!land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player) e.getDamager());
                return;
            }
        }
        if (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE
                && e.getEntity() instanceof Player && e.getDamager() instanceof Arrow && ((Arrow) e.getDamager()).getShooter() instanceof Player) {
            OwnedLand land = LandManager.getInstance().getLand(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            Village village = land.getOwnerVillage();
            if (village.getState() == VillageState.KINGDOM && !village.isPvp() && !land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player) ((Arrow) e.getDamager()).getShooter());
                return;
            }
            if (!land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player) ((Arrow) e.getDamager()).getShooter());
            }
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getCombuster() instanceof Arrow && ((Arrow) e.getCombuster()).getShooter() instanceof Player) {
            OwnedLand land = LandManager.getInstance().getLand(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            Village village = land.getOwnerVillage();
            if (village.getState() == VillageState.KINGDOM && !village.isPvp() && !land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player) ((Arrow) e.getCombuster()).getShooter());
                return;
            }
            if (!land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player) ((Arrow) e.getCombuster()).getShooter());
            }
        }
    }

    @EventHandler
    public void onEnttiyExplode(EntityExplodeEvent e) {
        for (Block block : e.blockList()) {
            OwnedLand land = LandManager.getInstance().getLand(block.getLocation());
            if (land == null) {
                return;
            }
            if (land.isExplosions()) {
                continue;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            OwnedLand land = LandManager.getInstance().getLand(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            if (!land.isExplosions()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHostileSpawn(HostileCreatureSpawnEvent e) {
        OwnedLand land = LandManager.getInstance().getLand(e.getLocation().getChunk());
        if (land != null && !land.isMobSpawning()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveChunkEvent event) {
        Player player = event.getPlayer();
        Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(player);
        OwnedLand originalLand = LandManager.getInstance().getLand(event.getFrom());
        OwnedLand destinationLand = LandManager.getInstance().getLand(event.getTo());
        boolean sameVillage = originalLand != null && destinationLand != null && originalLand.getOwnerVillage().equals(destinationLand.getOwnerVillage());

        if (!sameVillage && originalLand != null) {
            MessageUtils.tell(player, Message.get(originalLand.getOwnerVillage().getState().getKey() + ".leave-message"));
            if (originalLand.getOwnerVillage().equals(playerVillage) && originalLand.getOwnerVillage().isBuff()) {
                Base.getLandPerks().removeEffects(player, originalLand, true);
            }
        }

        if (destinationLand != null) {
            if (!sameVillage) {
                if (destinationLand.getOwnerVillage().isPvp()) {
                    MessageUtils.tell(player, Message.get(destinationLand.getOwnerVillage().getState().getKey() + ".enter-message").getReplacedComponents("pvp", Message.get("pvp-is-enabled").getComponents()));
                } else {
                    MessageUtils.tell(player, Message.get(destinationLand.getOwnerVillage().getState().getKey() + ".enter-message").getReplacedComponents("pvp", ""));
                }
                if (destinationLand.getOwnerVillage().equals(playerVillage) && destinationLand.getOwnerVillage().isBuff()) {
                    Base.getLandPerks().giveEffects(player, destinationLand, true);
                }
            }
            Utils.runAsync(() -> {
                TitleBuilder tb = new TitleBuilder();
                StringBuilder actionBar = new StringBuilder();
                if (destinationLand.isPlot()) {
                    actionBar.append(Message.get("enter-plot").getReplacedText("owner", ResidentManager.getInstance().getResident(destinationLand.getPlotOwner()).getName()));
                }
                if (destinationLand.isForSale() && (destinationLand.getOwnerVillage().equals(playerVillage) || destinationLand.isEmbassy())) {
                    actionBar.append(Message.get("plot-is-for-sale").getText());
                }
                if (destinationLand.isEmbassy()) {
                    actionBar.append(Message.get("plot-is-embassy").getText());
                }
                tb.withActionbar(actionBar.toString());
                tb.send(player);
            });
        }
    }
}

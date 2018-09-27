package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.essentials.events.HostileCreatureSpawnEvent;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MojangAPI;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.essentials.events.PassiveCreatureDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;

import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.Base;

public class LandFlags extends Base {
    private TitleBuilder tbPvp = new TitleBuilder();

    public LandFlags() {
        super();
    }

    @EventHandler
    public void on(BlockIgniteEvent e) {
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
    public void on(PassiveCreatureDamageEvent e) {
        if (e.getEntity() == null) {
            return;
        }
        OwnedLand land = LandManager.getInstance().getLand(e.getEntity().getLocation());
        if (land == null) {
            return;
        }
        if (e.getDamager() == null) {
            return;
        }
        if (e.getDamager().isOp()) {
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        Resident res = ResidentManager.getInstance().getResident(e.getDamager());
        if (!village.getName().equals(res.getVillageName())) {
            e.setCancelled(true);
            TitleBuilder tb = new TitleBuilder();
            tb.withActionbar(MessageUtils.replaceColorCodes("&c&lYou cannot attack another villages HOSTILE!"));
            tb.send(e.getDamager());
        }
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent e) {
        if ((e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
                && e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            OwnedLand land = LandManager.getInstance().getLand(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            if (e.getDamager().isOp()) {
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
            Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
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
    public void on(EntityCombustByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getCombuster() instanceof Arrow && ((Arrow) e.getCombuster()).getShooter() instanceof Player) {
            OwnedLand land = LandManager.getInstance().getLand(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
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
    public void on(EntityExplodeEvent e) {
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
    public void on(EntityDamageEvent e) {
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
    public void on(PlayerMoveBlockEvent event) {
        StringBuilder addition = new StringBuilder();
        Player player = event.getPlayer();
        Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(player);
        OwnedLand originalLand = LandManager.getInstance().getLand(player.getLocation());
        OwnedLand destinationLand = LandManager.getInstance().getLand(event.getTo());
        if (!this.landIsClaimed(originalLand) && this.landIsClaimed(destinationLand)) {
            Village claimedVillage = VillageManager.getInstance().getVillageFromName(destinationLand.getOwnerVillage());
            TitleBuilder tb = new TitleBuilder();
            tb.withTimings(40, 60, 40);
            if (destinationLand.isPlot()) {
                addition.append("&7&lWelcome to&6&l ");
                addition.append(MojangAPI.getInstance().getName(destinationLand.getPlotOwner()));
                addition.append("&7&l\u2019s plot v");
            }
            if (destinationLand.isPvp()) {
                tb.withActionbar(MessageUtils.replaceColorCodes(String.valueOf(addition.toString()) + claimedVillage.getEnterMessage() + " - &C&lPvp is enabled!"));
            } else {
                tb.withActionbar(MessageUtils.replaceColorCodes(String.valueOf(addition.toString()) + claimedVillage.getEnterMessage()));
            }
            tb.send(player);
            if (playerVillage == null) {
                return;
            }
            if (this.playerOwnsLand(destinationLand, playerVillage.getName())) {
                Base.getLandPerks().giveEffects(player, playerVillage);
            }
        } else if (this.landIsClaimed(originalLand) && this.landIsClaimed(destinationLand)) {
            if (playerVillage == null) {
                return;
            }
            TitleBuilder tb = new TitleBuilder();
            if (destinationLand.isPlot()) {
                addition.append("&7&lWelcome to&6&l ");
                addition.append(MojangAPI.getInstance().getName(destinationLand.getPlotOwner()));
                addition.append("&7&l\u2019s plot ");
            }
            if (this.playerOwnsLand(destinationLand, playerVillage.getName())) {
                if (destinationLand.isForSale()) {
                    addition.append("&9&lLand is for sale ");
                }
            }
            tb.withActionbar(addition.toString());
            Village originalVillage = VillageManager.getInstance().getVillageFromName(originalLand.getOwnerVillage());
            Village destinationVillage = VillageManager.getInstance().getVillageFromName(destinationLand.getOwnerVillage());
            if (originalVillage != null && !originalVillage.getName().equals(destinationVillage.getName())) {
                if (playerVillage.isBuff()) {
                    if (this.playerOwnsLand(originalLand, playerVillage.getName()) && !this.playerOwnsLand(destinationLand, playerVillage.getName())) {
                        Base.getLandPerks().removeEffects(player, playerVillage);
                    } else if (this.playerOwnsLand(destinationLand, playerVillage.getName()) && !this.playerOwnsLand(originalLand, playerVillage.getName())) {
                        Base.getLandPerks().giveEffects(player, playerVillage);
                    }
                }
                if (destinationLand.isPvp()) {
                    tb.withActionbar(MessageUtils.replaceColorCodes(String.valueOf(addition.toString()) + " " + destinationVillage.getEnterMessage() + " - &C&lPvp is enabled!"));
                } else {
                    tb.withActionbar(MessageUtils.replaceColorCodes(String.valueOf(addition.toString()) + " " + destinationVillage.getEnterMessage()));
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(KingdomsPlugin.getInstance(), () -> {
                    new TitleBuilder().withActionbar(originalVillage.getLeaveMessage()).send(player);
                }, 2000L);
            }
            tb.send(player);
        } else if (this.landIsClaimed(originalLand) && !this.landIsClaimed(destinationLand)) {
            Village originalVillage = VillageManager.getInstance().getVillageFromName(originalLand.getOwnerVillage());
            if (playerVillage != null && playerVillage.isBuff() && this.playerOwnsLand(originalLand, playerVillage.getName())) {
                Base.getLandPerks().removeEffects(player, originalVillage);
            }
            TitleBuilder tb = new TitleBuilder();
            tb.withActionbar(MessageUtils.replaceColorCodes(originalVillage.getLeaveMessage()));
            tb.send(player);
        }
    }

    private boolean landIsClaimed(OwnedLand land) {
        return land != null;
    }

    private boolean playerOwnsLand(OwnedLand land, String villageName) {
        return land != null && land.getOwnerVillage().equals(villageName);
    }
}

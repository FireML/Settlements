package me.crz.minecraft.kingdoms.land;

import org.bukkit.plugin.Plugin;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MojangAPI;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import java.util.Iterator;
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
import java.util.Arrays;
import org.bukkit.entity.EntityType;
import java.util.List;
import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.Base;

public class LandFlags extends Base
{
    TitleBuilder tbPvp;
    List<EntityType> entities;
    StringBuilder addition;
    
    public LandFlags() {
        super();
        tbPvp = new TitleBuilder();
        entities = Arrays.<EntityType>asList(EntityType.BLAZE, EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.ZOMBIE, EntityType.WITHER_SKELETON, EntityType.WITCH, EntityType.VEX, EntityType.VINDICATOR, EntityType.SKELETON, EntityType.SLIME, EntityType.PIG_ZOMBIE);
    }
    
    @EventHandler
    public void on(final BlockIgniteEvent e) {
        if (e.getCause().equals((Object)BlockIgniteEvent.IgniteCause.SPREAD) && e.getIgnitingBlock() != null) {
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getIgnitingBlock().getLocation());
            if (land == null) {
                return;
            }
            if (!land.isFireSpread()) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void on(final PassiveCreatureDamageEvent e) {
        if (e.getEntity() == null) {
            return;
        }
        final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getEntity().getLocation());
        if (land == null) {
            return;
        }
        if (e.getDamager() == null) {
            return;
        }
        if (e.getDamager().isOp()) {
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        final Resident res = ResidentManager.getInstance().getResident(e.getDamager());
        if (!village.getName().equals(res.getVillageName())) {
            e.setCancelled(true);
            final TitleBuilder tb = new TitleBuilder();
            tb.withActionbar(MessageUtils.replaceColorCodes("&c&lYou cannot attack another villages entities!"));
            tb.send(e.getDamager());
        }
    }
    
    @EventHandler
    public void on(final EntityDamageByEntityEvent e) {
        if ((e.getCause().equals((Object)EntityDamageEvent.DamageCause.ENTITY_ATTACK) || e.getCause().equals((Object)EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) && e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            if (((Player)e.getDamager()).isOp()) {
                return;
            }
            if (!land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player)e.getDamager());
                return;
            }
        }
        if (e.getCause().equals((Object)EntityDamageEvent.DamageCause.PROJECTILE) && e.getEntity() instanceof Player && e.getDamager() instanceof Arrow && ((Arrow)e.getDamager()).getShooter() instanceof Player) {
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            final Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
            if (village.getState().equals(VillageState.KINGDOM) && !village.isPvp() && !land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player)((Arrow)e.getDamager()).getShooter());
                return;
            }
            if (!land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player)((Arrow)e.getDamager()).getShooter());
            }
        }
    }
    
    @EventHandler
    public void on(final EntityCombustByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getCombuster() instanceof Arrow && ((Arrow)e.getCombuster()).getShooter() instanceof Player) {
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            final Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
            if (village.getState().equals(VillageState.KINGDOM) && !village.isPvp() && !land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player)((Arrow)e.getCombuster()).getShooter());
                return;
            }
            if (!land.isPvp()) {
                e.setCancelled(true);
                tbPvp.send((Player)((Arrow)e.getCombuster()).getShooter());
            }
        }
    }
    
    @EventHandler
    public void on(final EntityExplodeEvent e) {
        for (final Block block : e.blockList()) {
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(block.getLocation());
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
    public void on(final EntityDamageEvent e) {
        if (e.getCause().equals((Object)EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || e.getCause().equals((Object)EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            if (!land.isExplosions()) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onHostileSpawn(final CreatureSpawnEvent e) {
        if (entities.contains(e.getEntityType()) && !LandManager.getInstance().getMobSpawning(e.getLocation().getChunk())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void on(final PlayerMoveBlockEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        addition = new StringBuilder();
        final Player player = event.getPlayer();
        final Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(player);
        final OwnedLand originalLand = LandManager.getInstance().getLandFromLocation(player.getLocation());
        final OwnedLand destinationLand = LandManager.getInstance().getLandFromLocation(event.getTo());
        if (!this.landIsClaimed(originalLand) && this.landIsClaimed(destinationLand)) {
            final Village claimedVillage = VillageManager.getInstance().getVillageFromName(destinationLand.getOwnerVillage());
            final TitleBuilder tb = new TitleBuilder();
            tb.withTimings(40, 60, 40);
            if (destinationLand.getPlotOwner() != "" && !destinationLand.getPlotOwner().isEmpty()) {
                addition.append("&7&lWelcome to&6&l ");
                addition.append(MojangAPI.getInstance().getName(destinationLand.getPlotOwner()));
                addition.append("&7&l\u2019s plot v");
            }
            if (destinationLand.isPvp()) {
                tb.withActionbar(MessageUtils.replaceColorCodes(String.valueOf(addition.toString()) + claimedVillage.getEnterMessage() + " - &C&lPvp is enabled!"));
            }
            else {
                tb.withActionbar(MessageUtils.replaceColorCodes(String.valueOf(addition.toString()) + claimedVillage.getEnterMessage()));
            }
            tb.send(player);
            if (playerVillage == null) {
                return;
            }
            if (this.playerOwnsLand(destinationLand, playerVillage.getName())) {
                Base.getLandPerks().giveEffects(player, playerVillage);
            }
        }
        else if (this.landIsClaimed(originalLand) && this.landIsClaimed(destinationLand)) {
            final Village originalVillage = VillageManager.getInstance().getVillageFromName(originalLand.getOwnerVillage());
            final Village destinationVillage = VillageManager.getInstance().getVillageFromName(destinationLand.getOwnerVillage());
            final TitleBuilder tb2 = new TitleBuilder();
            tb2.withTimings(40, 60, 40);
            if (this.playerOwnsLand(destinationLand, playerVillage.getName())) {
                if (destinationLand.getPlotOwner() != "" && !destinationLand.getPlotOwner().isEmpty()) {
                    addition.append("&7&lWelcome to&6&l ");
                    addition.append(MojangAPI.getInstance().getName(destinationLand.getPlotOwner()));
                    addition.append("&7&l\u2019s plot ");
                }
                if (destinationLand.isForSale()) {
                    addition.append("&9&lLand is for sale ");
                }
                tb2.withActionbar(addition.toString());
                return;
            }
            if (originalVillage == null) {
                return;
            }
            if (originalVillage.getName().equals(destinationVillage.getName())) {
                return;
            }
            if (playerVillage != null && playerVillage.isBuff()) {
                if (this.playerOwnsLand(originalLand, playerVillage.getName()) && !this.playerOwnsLand(destinationLand, playerVillage.getName())) {
                    Base.getLandPerks().removeEffects(player, playerVillage);
                }
                else if (this.playerOwnsLand(destinationLand, playerVillage.getName()) && !this.playerOwnsLand(originalLand, playerVillage.getName())) {
                    Base.getLandPerks().giveEffects(player, playerVillage);
                }
            }
            if (destinationLand.isPvp()) {
                tb2.withActionbar(MessageUtils.replaceColorCodes(String.valueOf(addition.toString()) + " " + destinationVillage.getEnterMessage() + " - &C&lPvp is enabled!"));
            }
            else {
                tb2.withActionbar(MessageUtils.replaceColorCodes(String.valueOf(addition.toString()) + " " + destinationVillage.getEnterMessage()));
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)KingdomsPlugin.getInstance(), (Runnable)new Runnable() {
                final /* synthetic */ LandFlags this$0;
                private final /* synthetic */ Village val$originalVillage;
                private final /* synthetic */ Player val$player;
                
                LandFlags$1() {
                    this$0 = this$0;
                    super();
                }
                
                @Override
                public void run() {
                    final TitleBuilder tb = new TitleBuilder();
                    tb.withTimings(40, 60, 40);
                    tb.withActionbar(originalVillage.getLeaveMessage());
                    tb.send(player);
                }
            }, 2000L);
        }
        else if (this.landIsClaimed(originalLand) && !this.landIsClaimed(destinationLand)) {
            final Village originalVillage = VillageManager.getInstance().getVillageFromName(originalLand.getOwnerVillage());
            if (playerVillage != null && playerVillage.isBuff() && this.playerOwnsLand(originalLand, playerVillage.getName())) {
                Base.getLandPerks().removeEffects(player, originalVillage);
            }
            final TitleBuilder tb = new TitleBuilder();
            tb.withTimings(40, 60, 40);
            tb.withActionbar(MessageUtils.replaceColorCodes(originalVillage.getLeaveMessage()));
            tb.send(player);
        }
    }
    
    private boolean landIsClaimed(final OwnedLand land) {
        return land != null;
    }
    
    private boolean playerOwnsLand(final OwnedLand land, final String villageName) {
        return land != null && land.getOwnerVillage().equals(villageName);
    }
}

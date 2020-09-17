package me.crz.minecraft.kingdoms.customevents;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.customevents.listeners.SpigotListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.EnumSet;
import java.util.Set;

public class CustomEventManager implements Listener {
    public static final Set<EntityType> SPECIAL_HOSTILE = EnumSet.of(
            EntityType.SLIME,
            EntityType.PHANTOM
    );
    public static final Set<EntityType> SPECIAL_PASSIVE = EnumSet.of(
            EntityType.SNOWMAN,
            EntityType.VILLAGER,
            EntityType.SQUID,
            EntityType.IRON_GOLEM
    );

    public CustomEventManager() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            Bukkit.getPluginManager().registerEvents(
                    (Listener) Class.forName(getClass().getPackage().getName() + ".PaperListener").newInstance(),
                    KingdomsPlugin.getInstance()
            );
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Bukkit.getPluginManager().registerEvents(new SpigotListener(), KingdomsPlugin.getInstance());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMoveBlock(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }
        final PlayerMoveBlockEvent pmbe = new PlayerMoveBlockEvent(e.getFrom(), e.getTo(), e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(pmbe);
        if (pmbe.isCancelled()) {
            e.setCancelled(true);
        }
        if (e.getFrom().getBlockX() >> 4 == e.getTo().getBlockX() >> 4 && e.getFrom().getBlockZ() >> 4 == e.getTo().getBlockZ() >> 4) {
            return;
        }
        PlayerMoveChunkEvent pmce = new PlayerMoveChunkEvent(e.getFrom().getChunk(), e.getTo().getChunk(), e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(pmce);
        if (pmce.isCancelled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHostileSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Monster || SPECIAL_HOSTILE.contains(e.getEntityType())) {
            HostileCreatureSpawnEvent hcse = new HostileCreatureSpawnEvent(e.getEntity(), e.getLocation(), e.getSpawnReason());
            Bukkit.getPluginManager().callEvent(hcse);
            if (hcse.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Animals || e.getEntity() instanceof WaterMob || e.getEntity() instanceof Ambient
                || SPECIAL_PASSIVE.contains(e.getEntityType())) {
            Player damager = null;
            if (e.getDamager() instanceof Player) {
                damager = (Player) e.getDamager();
            } else if (e.getDamager() instanceof Projectile) {
                if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
                    damager = (Player) ((Projectile) e.getDamager()).getShooter();
                } else if (((Projectile) e.getDamager()).getShooter() instanceof Mob
                        && ((Mob) ((Projectile) e.getDamager()).getShooter()).getTarget() instanceof Player) {
                    damager = (Player) ((Mob) ((Projectile) e.getDamager()).getShooter()).getTarget();
                }
            } else if (e.getDamager() instanceof Creeper && ((Creeper) e.getDamager()).getTarget() instanceof Player) {
                damager = (Player) ((Creeper) e.getDamager()).getTarget();
            }
            PassiveCreatureDamageEvent pcde = new PassiveCreatureDamageEvent(e.getEntity(), damager, e);
            Bukkit.getPluginManager().callEvent(pcde);
            if (pcde.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityCombustByEntity(EntityCombustByEntityEvent e) {
        if (e.getEntity() instanceof Animals || e.getEntity() instanceof WaterMob || e.getEntity() instanceof Ambient
                || SPECIAL_PASSIVE.contains(e.getEntityType())) {
            Player combuster = null;
            if (e.getCombuster() instanceof Player) {
                combuster = (Player) e.getCombuster();
            } else if (e.getCombuster() instanceof Projectile) {
                if (((Projectile) e.getCombuster()).getShooter() instanceof Player) {
                    combuster = (Player) ((Projectile) e.getCombuster()).getShooter();
                } else if (((Projectile) e.getCombuster()).getShooter() instanceof Mob
                        && ((Mob) ((Projectile) e.getCombuster()).getShooter()).getTarget() instanceof Player) {
                    combuster = (Player) ((Mob) ((Projectile) e.getCombuster()).getShooter()).getTarget();
                }
            } else if (e.getCombuster() instanceof Creeper && ((Creeper) e.getCombuster()).getTarget() instanceof Player) {
                combuster = (Player) ((Creeper) e.getCombuster()).getTarget();
            }
            PassiveCreatureCombustEvent pcce = new PassiveCreatureCombustEvent(e.getEntity(), combuster);
            Bukkit.getPluginManager().callEvent(pcce);
            if (pcce.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }
}

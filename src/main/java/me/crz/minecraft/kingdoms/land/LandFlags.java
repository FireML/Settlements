package me.crz.minecraft.kingdoms.land;

import co.aikar.timings.Timing;
import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.essentials.events.HostileCreaturePreSpawnEvent;
import me.crz.minecraft.kingdoms.essentials.events.HostileCreatureSpawnEvent;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveChunkEvent;
import me.crz.minecraft.kingdoms.landflags.LandFlagBase;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public class LandFlags extends Base implements LandFlagBase {
    private TitleBuilder tbPvp = new TitleBuilder();
    private final Timing mobCheckTiming;
    private BukkitTask mobCheckTask = null;
    private int mobCheckPeriod = 0;

    public LandFlags() {
        super();
        reload();
        mobCheckTiming = KingdomsPlugin.getInstance().getTiming("Mob spawning mode check");
    }

    public void reload() {
        int oldMobCheckPeriod = mobCheckPeriod;
        mobCheckPeriod = ConfigManager.getInstance().getConfig().getInt("mob-flag-check-period", 30);
        if (mobCheckPeriod != oldMobCheckPeriod) {
            if (mobCheckTask != null) {
                mobCheckTask.cancel();
            }
            if (mobCheckPeriod > 0) {
                mobCheckTask = Bukkit.getScheduler().runTaskTimer(KingdomsPlugin.getInstance(), () -> {
                    int amount = 0;
                    try (Timing ignored = mobCheckTiming.startTiming()) {
                        for (World world : Bukkit.getWorlds()) {
                            if (Utils.isWorldClaimable(world) && LandManager.getInstance().hasLand(world)) {
                                for (LivingEntity entity : world.getLivingEntities()) {
                                    if (Utils.isHostile(entity)) {
                                        OwnedLand land = LandManager.getInstance().getLand(entity.getLocation());
                                        if (land != null && !land.getMode(PlotMode.mobSpawning)) {
                                            entity.remove();
                                            amount++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (amount > 0) {
                        KingdomsPlugin.getInstance().getLogger().log(Level.INFO, "Removed " + amount + " hostile mob" + (amount > 1 ? "s" : "") + " that wandered into no-mobs-claims");
                    }
                }, mobCheckPeriod * 20, mobCheckPeriod * 20);
            }
        }
    }

    private boolean isAllowed(Location location, PlotMode mode) {
        if (!Utils.isWorldClaimable(location.getWorld())) {
            if (Utils.hasPlotMode(mode, "worlds." + location.getWorld().getName())) {
                return Utils.getPlotMode(mode, "worlds." + location.getWorld().getName());
            }
            return Utils.getPlotMode(mode, "unclaimable");
        }
        OwnedLand land = LandManager.getInstance().getLand(location);
        if (land != null) {
            return mode.get(land);
        }
        if (Utils.hasPlotMode(mode, "worlds." + location.getWorld().getName())) {
            return Utils.getPlotMode(mode, "worlds." + location.getWorld().getName());
        }
        return Utils.getPlotMode(mode, "unclaimed");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        OwnedLand land = LandManager.getInstance().getLand(event.getChunk());
        if (land != null && !land.getMode(PlotMode.mobSpawning)) {
            for (Entity entity : event.getChunk().getEntities()) {
                if (entity instanceof LivingEntity && Utils.isHostile((LivingEntity) entity)) {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getCause().equals(BlockIgniteEvent.IgniteCause.SPREAD) && e.getIgnitingBlock() != null) {
            if (isAllowed(e.getBlock().getLocation(), PlotMode.fireSpread)) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        if (isAllowed(e.getBlock().getLocation(), PlotMode.fireSpread)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player damager;
            if (e.getDamager() instanceof Player) {
                damager = (Player) e.getDamager();
            } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
                damager = (Player) ((Projectile) e.getDamager()).getShooter();
            } else {
                return;
            }

            if (isAllowed(e.getEntity().getLocation(), PlotMode.pvp) && isAllowed(damager.getLocation(), PlotMode.pvp)) {
                return;
            }
            if (damager.hasPermission("kingdoms.bypass.pvp")) {
                return;
            }
            e.setCancelled(true);
            tbPvp.send(damager);
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player combuster;
            if (e.getCombuster() instanceof Player) {
                combuster = (Player) e.getCombuster();
            } else if (e.getCombuster() instanceof Projectile && ((Projectile) e.getCombuster()).getShooter() instanceof Player) {
                combuster = (Player) ((Projectile) e.getCombuster()).getShooter();
            } else {
                return;
            }

            if (isAllowed(e.getEntity().getLocation(), PlotMode.pvp) && isAllowed(combuster.getLocation(), PlotMode.pvp)) {
                return;
            }
            if (combuster.hasPermission("kingdoms.bypass.pvp")) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTntPrime(TNTPrimeEvent e) {
        if (e.getReason() == TNTPrimeEvent.PrimeReason.EXPLOSION && !isAllowed(e.getBlock().getLocation(), PlotMode.explosions)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!isAllowed(e.getLocation(), PlotMode.explosions)) {
            e.setCancelled(true);
            return;
        }
        e.blockList().removeIf(block -> !isAllowed(block.getLocation(), PlotMode.explosions));
    }


    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        if (!isAllowed(e.getBlock().getLocation(), PlotMode.explosions)) {
            e.setCancelled(true);
            return;
        }
        e.blockList().removeIf(block -> !isAllowed(block.getLocation(), PlotMode.explosions));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (isAllowed(e.getEntity().getLocation(), PlotMode.explosions)) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent e) {
        if (e.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            if (isAllowed(e.getEntity().getLocation(), PlotMode.explosions)) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHostileSpawn(HostileCreaturePreSpawnEvent e) {
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM
                && !isAllowed(e.getLocation(), PlotMode.mobSpawning)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHostileSpawn(HostileCreatureSpawnEvent e) {
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM
                && !isAllowed(e.getLocation(), PlotMode.mobSpawning)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSpawnEggClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null
                && event.getItem() != null
                && event.getItem().getType().name().endsWith("SPAWN_EGG")
                && !isAllowed(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), PlotMode.mobSpawning)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveChunkEvent event) {
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getInstance().getResident(player);
        Village playerVillage = resident.getVillage();
        OwnedLand originalLand = LandManager.getInstance().getLand(event.getFrom());
        OwnedLand destinationLand = LandManager.getInstance().getLand(event.getTo());
        boolean sameVillage = originalLand != null && destinationLand != null && originalLand.getLandOwner().equals(destinationLand.getLandOwner());

        if (!sameVillage && originalLand != null) {
            MessageUtils.tellAdvanced(player, originalLand.getLandOwner().getStateKey() + ".leave" + (originalLand.getLandOwner().isAbandoned() ? "-abandoned" : ""),
                    "name", originalLand.getLandOwner().getName(),
                    "state", originalLand.getLandOwner().getStateTag()
            );
            if (originalLand.getLandOwner().equals(playerVillage) && originalLand.getLandOwner().getMode(PlotMode.buff)) {
                Base.getLandPerks().removeEffects(player, originalLand, true);
            }
        }

        if (destinationLand != null) {
            if (!sameVillage) {
                MessageUtils.tellAdvanced(player, destinationLand.getLandOwner().getStateKey() + ".enter" + (destinationLand.getLandOwner().isAbandoned() ? "-abandoned" : ""),
                        "pvp", destinationLand.getMode(PlotMode.pvp) ? Message.get("pvp-is-enabled").getText() : "",
                        "name", destinationLand.getLandOwner().getName(),
                        "state", destinationLand.getLandOwner().getStateTag()
                );
                if (destinationLand.getLandOwner().equals(playerVillage) && destinationLand.getLandOwner().getMode(PlotMode.buff)) {
                    Base.getLandPerks().giveEffects(player, destinationLand, true);
                }
            }
            if (originalLand != null && originalLand.isPlotOwner(destinationLand.getPlotOwner()) && destinationLand.hasLink() && !originalLand.isLinked(destinationLand)) {
                Utils.runAsync(() -> MessageUtils.tellAdvanced(player, (destinationLand.isEmbassy() ? "embassy" : "plot") + ".linked",
                        "name", destinationLand.getLandOwner().getName(),
                        "state", destinationLand.getLandOwner().getStateTag(),
                        "owner", ResidentManager.getInstance().getResident(destinationLand.getPlotOwner()).getName(),
                        "name", destinationLand.getName(),
                        "description", destinationLand.getDescription(),
                        "pvp", destinationLand.getMode(PlotMode.pvp) ? Message.get("pvp-is-enabled").getText() : ""
                ));
            } else if (originalLand == null || !originalLand.isPlotOwner(destinationLand.getPlotOwner()) || !originalLand.getName().equals(destinationLand.getName()) || !originalLand.getDescription().equals(destinationLand.getDescription())) {
                if (destinationLand.isPlot()) {
                    Utils.runAsync(() -> MessageUtils.tellAdvanced(player,  (destinationLand.isEmbassy() ? "embassy" : "plot") + ".owned" + (destinationLand.getDescription().isEmpty() ? "" : "-description"),
                            "name", destinationLand.getLandOwner().getName(),
                            "state", destinationLand.getLandOwner().getStateTag(),
                            "owner", ResidentManager.getInstance().getResident(destinationLand.getPlotOwner()).getName(),
                            "name", destinationLand.getName(),
                            "description", destinationLand.getDescription(),
                            "pvp", destinationLand.getMode(PlotMode.pvp) ? Message.get("pvp-is-enabled").getText() : ""
                    ));
                } else if (destinationLand.isForSale() && (destinationLand.getLandOwner().isMember(resident) || destinationLand.isEmbassy())) {
                    MessageUtils.tellAdvanced(player, (destinationLand.isEmbassy() ? "embassy" : "plot") + ".for-sale" + (destinationLand.getDescription().isEmpty() ? "" : "-description"),
                            "name", destinationLand.getLandOwner().getName(),
                            "state", destinationLand.getLandOwner().getStateTag(),
                            "name", destinationLand.getName(),
                            "description", destinationLand.getDescription(),
                            "pvp", destinationLand.getMode(PlotMode.pvp) ? Message.get("pvp-is-enabled").getText() : "",
                            "amount", Utils.CURRENCY_FORMAT.format(destinationLand.getPrice())
                    );
                } else if (!destinationLand.getDescription().isEmpty()) {
                    MessageUtils.tellAdvanced(player, (destinationLand.isEmbassy() ? "embassy" : "plot") + ".description",
                            "name", destinationLand.getLandOwner().getName(),
                            "state", destinationLand.getLandOwner().getStateTag(),
                            "name", destinationLand.getName(),
                            "description", destinationLand.getDescription(),
                            "pvp", destinationLand.getMode(PlotMode.pvp) ? Message.get("pvp-is-enabled").getText() : "",
                            "amount", Utils.CURRENCY_FORMAT.format(destinationLand.getPrice())
                    );
                }
            }
        }
    }
}

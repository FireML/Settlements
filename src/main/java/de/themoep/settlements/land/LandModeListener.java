package de.themoep.settlements.land;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.customevents.CreaturePreSpawnEvent;
import de.themoep.settlements.customevents.PlayerMoveChunkEvent;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.land.events.LandChangeEvent;
import de.themoep.settlements.land.events.LandEnterEvent;
import de.themoep.settlements.land.events.LandLeaveEvent;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.TitleBuilder;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import uk.firedev.poleislib.libs.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;

import java.util.logging.Level;

public class LandModeListener implements LandAccessChecker {
    private final TitleBuilder tbPvp = new TitleBuilder();
    private MyScheduledTask mobCheckTask = null;
    private long mobCheckPeriod = 0;

    public LandModeListener() {
        reload();
    }

    public void reload() {
        if (mobCheckPeriod != Config.Claiming.MOB_FLAG_CHECK_PERIOD) {
            mobCheckPeriod = Config.Claiming.MOB_FLAG_CHECK_PERIOD;
            if (mobCheckTask != null) {
                mobCheckTask.cancel();
            }
            if (mobCheckPeriod > 0) {
                mobCheckTask = SettlementsPlugin.getScheduler().runTaskTimer(() -> {
                    int amount = 0;
                    for (World world : Bukkit.getWorlds()) {
                        if (Utils.isWorldClaimable(world) && LandManager.getInstance().hasLand(world)) {
                            for (LivingEntity entity : world.getLivingEntities()) {
                                if (Utils.isHostile(entity)) {
                                    OwnedLand land = LandManager.getInstance().getLand(entity.getLocation());
                                    if (land != null && !land.getMode(PlotMode.mobSpawning)) {
                                        SettlementsPlugin.getScheduler().runTask(entity, entity::remove);
                                        amount++;
                                    }
                                }
                            }
                        }
                    }
                    if (amount > 0) {
                        SettlementsPlugin.getInstance().getLogger().log(Level.INFO, "Removed " + amount + " hostile mob" + (amount > 1 ? "s" : "") + " that wandered into no-mobs-claims");
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
                if (entity instanceof LivingEntity && Utils.isHostile(entity)) {
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
    public void onEnterLand(LandEnterEvent e) {
        BossBar bb = ConfigManager.getInstance().getBossBar();
        Player p = e.getPlayer();
        OwnedLand to = e.getTo();
        if (to.getMode(PlotMode.pvp) && !bb.getPlayers().contains(p)) {
            bb.addPlayer(p);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.02F, 1);
            Message.get("pvp-claim.enter").tell(p);
        }
    }

    @EventHandler
    public void onLeaveLand(LandLeaveEvent e) {
        BossBar bb = ConfigManager.getInstance().getBossBar();
        Player p = e.getPlayer();
        if (bb.getPlayers().contains(p)) {
            bb.removePlayer(p);
            Message.get("pvp-claim.exit").tell(p);
        }
    }

    @EventHandler
    public void onChangeLand(LandChangeEvent e) {
        BossBar bb = ConfigManager.getInstance().getBossBar();
        Player p = e.getPlayer();
        OwnedLand to = e.getTo();
        if (!to.getMode(PlotMode.pvp) && bb.getPlayers().contains(p)) {
            bb.removePlayer(p);
            Message.get("pvp-claim.exit").tell(p);
        } else if (to.getMode(PlotMode.pvp) && !bb.getPlayers().contains(p)) {
            bb.addPlayer(p);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.02F, 1);
            Message.get("pvp-claim.enter").tell(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        BossBar bb = ConfigManager.getInstance().getBossBar();
        Player p = e.getPlayer();
        OwnedLand land = LandManager.getInstance().getLand(p.getLocation().getChunk());
        if (land == null || !land.getMode(PlotMode.pvp)) {
            if (bb.getPlayers().contains(p)) {
                bb.removePlayer(p);
            }
        } else {
            if (!bb.getPlayers().contains(p)) {
                bb.addPlayer(p);
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.02F, 1);
                MessageUtils.tellAdvanced(p, land.getLandOwner().getStateKey() + ".enterPvP",
                        "state", land.getLandOwner().getStateTag()
                );
            }
        }
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
            if (damager.hasPermission("settlements.bypass.pvp")) {
                return;
            }
            e.setCancelled(true);
            tbPvp.send(damager);
        }
    }

    @EventHandler
    public void onEntityDamagePlayer(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player && Utils.isHostile(e.getDamager())) {
            OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
            if (land != null && !land.getMode(PlotMode.mobSpawning)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileDamagePlayer(ProjectileHitEvent e) {
        if (e.getHitEntity() instanceof Player player && !(e.getEntity().getShooter() instanceof Player)) {
            OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
            if (land != null && !land.getMode(PlotMode.mobSpawning)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDamagedByMagic(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && e.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
            OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
            if (land != null && !land.getMode(PlotMode.mobSpawning)) {
                e.setCancelled(true);
            }
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
            if (combuster.hasPermission("settlements.bypass.pvp")) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTntPrime(TNTPrimeEvent e) {
        if (e.getCause() == TNTPrimeEvent.PrimeCause.EXPLOSION && !isAllowed(e.getBlock().getLocation(), PlotMode.explosions)) {
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
    public void onCreaturePreSpawn(CreaturePreSpawnEvent e) {
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            if (Utils.isHostile(e.getEntityType())) {
                if (!isAllowed(e.getLocation(), PlotMode.mobSpawning)) {
                    e.setCancelled(true);
                }
            } else if (e.getEntityType() != EntityType.ARMOR_STAND) {
                if (!isAllowed(e.getLocation(), PlotMode.passiveSpawning)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            if (Utils.isHostile(e.getEntity())) {
                if (!isAllowed(e.getLocation(), PlotMode.mobSpawning)) {
                    e.setCancelled(true);
                }
            } else if (e.getEntityType() != EntityType.ARMOR_STAND) {
                if (!isAllowed(e.getLocation(), PlotMode.passiveSpawning)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSpawnEggClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null
                && event.getItem() != null
                && event.getItem().getType().name().endsWith("_SPAWN_EGG")) {
            String material = event.getItem().getType().name();
            try {
                EntityType type = EntityType.valueOf(material.substring(0, material.length() - "_SPAWN_EGG".length()));
                event.setCancelled(!isAllowed(
                        event.getClickedBlock().getLocation(),
                        Utils.isHostile(type) ? PlotMode.mobSpawning : PlotMode.passiveSpawning
                ));
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.logDebug("Unable to get entity type of " + material + ": " + e.getMessage());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveChunkEvent event) {
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getInstance().getResident(player);
        Settlement playerSettlement = resident.getSettlement();
        Empire playerEmpire = resident.hasEmpire() ? playerSettlement.getEmpire() : null;
        OwnedLand originalLand = LandManager.getInstance().getLand(event.getFrom());
        OwnedLand destinationLand = LandManager.getInstance().getLand(event.getTo());
        boolean sameSettlement = originalLand != null && destinationLand != null && originalLand.getLandOwner().equals(destinationLand.getLandOwner());

        if (!sameSettlement && originalLand != null) {
            MessageUtils.tellAdvanced(player, originalLand.getLandOwner().getStateKey() + ".leave" + (originalLand.getLandOwner().isAbandoned() ? "-abandoned" : ""),
                    "name", originalLand.getLandOwner().getName(),
                    "state", originalLand.getLandOwner().getStateTag()
            );
            if (originalLand.getLandOwner().getMode(PlotMode.buff)
                    && (originalLand.isOwnedBy(playerSettlement) || originalLand.isOwnedBy(playerEmpire))) {
                SettlementsPlugin.getInstance().getLandPerks().removeEffects(player, originalLand, true);
            }
        }

        if (destinationLand != null) {
            if (!sameSettlement) {
                MessageUtils.tellAdvanced(player, destinationLand.getLandOwner().getStateKey() + ".enter" + (destinationLand.getLandOwner().isAbandoned() ? "-abandoned" : ""),
                        "pvp", destinationLand.getMode(PlotMode.pvp) ? Message.get("pvp-is-enabled").getText() : "",
                        "name", destinationLand.getLandOwner().getName(),
                        "state", destinationLand.getLandOwner().getStateTag()
                );
                if (destinationLand.getLandOwner().getMode(PlotMode.buff)
                        && (destinationLand.isOwnedBy(playerSettlement) || destinationLand.isOwnedBy(playerEmpire))) {
                    SettlementsPlugin.getInstance().getLandPerks().giveEffects(player, destinationLand, true);
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
                } else if (!destinationLand.getDescription().isEmpty() || !destinationLand.getName().isEmpty()) {
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

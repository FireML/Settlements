package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.command.empire.EmpireMode;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.essentials.events.HostileCreaturePreSpawnEvent;
import me.crz.minecraft.kingdoms.essentials.events.HostileCreatureSpawnEvent;
import me.crz.minecraft.kingdoms.essentials.events.PassiveCreatureDamageEvent;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveChunkEvent;
import me.crz.minecraft.kingdoms.landflags.LandFlagBase;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class LandFlags extends Base implements LandFlagBase {
    private TitleBuilder tbPvp = new TitleBuilder();

    public LandFlags() {
        super();
    }

    private boolean isAllowed(Location location, PlotMode mode) {
        OwnedLand land = LandManager.getInstance().getLand(location);
        return land == null || mode.get(land);
    }

    private boolean isAllowed(Location location, EmpireMode mode) {
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(location.getWorld());
        return empire == null || mode.get(empire);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getCause().equals(BlockIgniteEvent.IgniteCause.SPREAD) && e.getIgnitingBlock() != null) {
            if (isAllowed(e.getBlock().getLocation(), PlotMode.firespread) && isAllowed(e.getBlock().getLocation(), EmpireMode.firespread)) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        if (isAllowed(e.getBlock().getLocation(), PlotMode.firespread) && isAllowed(e.getBlock().getLocation(), EmpireMode.firespread)) {
            return;
        }
        e.setCancelled(true);
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
        if (e.getEntity() instanceof Player) {
            Player damager;
            if (e.getDamager() instanceof Player) {
                damager = (Player) e.getDamager();
            } else if (e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player) {
                damager = (Player) ((Projectile) e.getDamager()).getShooter();
            } else {
                return;
            }

            if (isAllowed(e.getEntity().getLocation(), PlotMode.pvp) && isAllowed(e.getEntity().getLocation(), EmpireMode.pvp)) {
                return;
            }
            if (damager.hasPermission("kindoms.staff")) {
                return;
            }
            e.setCancelled(true);
            tbPvp.send((Player) e.getDamager());
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getCombuster() instanceof Projectile && ((Projectile) e.getCombuster()).getShooter() instanceof Player) {
            OwnedLand land = LandManager.getInstance().getLand(e.getEntity().getLocation());
            if (land == null) {
                return;
            }
            if (isAllowed(e.getEntity().getLocation(), PlotMode.pvp) && isAllowed(e.getEntity().getLocation(), EmpireMode.pvp)) {
                return;
            }
            Player damager = (Player) ((Projectile) e.getCombuster()).getShooter();
            if (damager.hasPermission("kindoms.staff")) {
                return;
            }
            e.setCancelled(true);
            tbPvp.send(damager);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (!isAllowed(e.getLocation(), PlotMode.explosions) || !isAllowed(e.getLocation(), EmpireMode.explosions)) {
            e.setCancelled(true);
            return;
        }
        e.blockList().removeIf(block -> !isAllowed(block.getLocation(), PlotMode.explosions) || !isAllowed(block.getLocation(), EmpireMode.explosions));
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(BlockExplodeEvent e) {
        if (!isAllowed(e.getBlock().getLocation(), PlotMode.explosions) || !isAllowed(e.getBlock().getLocation(), EmpireMode.explosions)) {
            e.setCancelled(true);
            return;
        }
        e.blockList().removeIf(block -> !isAllowed(block.getLocation(), PlotMode.explosions) || !isAllowed(block.getLocation(), EmpireMode.explosions));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (isAllowed(e.getEntity().getLocation(), PlotMode.explosions) && isAllowed(e.getEntity().getLocation(), EmpireMode.explosions)) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHostileSpawn(HostileCreaturePreSpawnEvent e) {
        OwnedLand land = LandManager.getInstance().getLand(e.getLocation().getChunk());
        if (land != null && !land.isMobSpawning()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHostileSpawn(HostileCreatureSpawnEvent e) {
        OwnedLand land = LandManager.getInstance().getLand(e.getLocation().getChunk());
        if (land != null && !land.isMobSpawning()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveChunkEvent event) {
        Player player = event.getPlayer();
        Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(player);
        OwnedLand originalLand = LandManager.getInstance().getLand(event.getFrom());
        OwnedLand destinationLand = LandManager.getInstance().getLand(event.getTo());
        boolean sameVillage = originalLand != null && destinationLand != null && originalLand.getLandOwner().equals(destinationLand.getLandOwner());

        if (!sameVillage && originalLand != null) {
            MessageUtils.tellAdvanced(player, originalLand.getLandOwner().getStateKey() + ".leave",
                    "name", originalLand.getLandOwner().getName(),
                    "state", originalLand.getLandOwner().getStateTag()
            );
            if (originalLand.getLandOwner().equals(playerVillage) && originalLand.getLandOwner().isBuff()) {
                Base.getLandPerks().removeEffects(player, originalLand, true);
            }
        }

        if (destinationLand != null) {
            if (!sameVillage) {
                MessageUtils.tellAdvanced(player, destinationLand.getLandOwner().getStateKey() + ".enter",
                        "pvp", destinationLand.isPvp() ? Message.get("pvp-is-enabled").getText() : "",
                        "name", destinationLand.getLandOwner().getName(),
                        "state", destinationLand.getLandOwner().getStateTag()
                );
                if (destinationLand.getLandOwner().equals(playerVillage) && destinationLand.getLandOwner().isBuff()) {
                    Base.getLandPerks().giveEffects(player, destinationLand, true);
                }
            }
            if (originalLand == null || !originalLand.isPlotOwner(destinationLand.getPlotOwner())) {
                if (destinationLand.isPlot()) {
                    Utils.runAsync(() -> MessageUtils.tellAdvanced(player,  (destinationLand.isEmbassy() ? "embassy" : "plot") + ".owned",
                            "name", destinationLand.getLandOwner().getName(),
                            "state", destinationLand.getLandOwner().getStateTag(),
                            "owner", ResidentManager.getInstance().getResident(destinationLand.getPlotOwner()).getName(),
                            "description", destinationLand.getDescription(),
                            "pvp", destinationLand.isPvp() ? Message.get("pvp-is-enabled").getText() : ""
                    ));
                } else if (destinationLand.isForSale() && (destinationLand.getLandOwner().equals(playerVillage) || destinationLand.isEmbassy())) {
                    MessageUtils.tellAdvanced(player, (destinationLand.isEmbassy() ? "embassy" : "plot") + ".for-sale",
                            "name", destinationLand.getLandOwner().getName(),
                            "state", destinationLand.getLandOwner().getStateTag(),
                            "description", destinationLand.getDescription(),
                            "pvp", destinationLand.isPvp() ? Message.get("pvp-is-enabled").getText() : "",
                            "amount", Utils.CURRENCY_FORMAT.format(destinationLand.getPrice())
                    );
                }
            }
        }
    }
}

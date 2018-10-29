package me.crz.minecraft.kingdoms.land;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.command.empire.EmpireMode;
import me.crz.minecraft.kingdoms.command.village.PlotMode;
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
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getCause().equals(BlockIgniteEvent.IgniteCause.SPREAD) && e.getIgnitingBlock() != null) {
            if (isAllowed(e.getBlock().getLocation(), PlotMode.firespread) && isAllowed(e.getBlock().getLocation(), EmpireMode.firespread)) {
                return;
            }
            e.setCancelled(true);
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

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        for (Block block : e.blockList()) {
            if (block == null || (isAllowed(block.getLocation(), PlotMode.explosions) && isAllowed(block.getLocation(), EmpireMode.explosions))) {
                continue;
            }
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onEntityExplode(BlockExplodeEvent e) {
        for (Block block : e.blockList()) {
            if (block == null || (isAllowed(block.getLocation(), PlotMode.explosions) && isAllowed(block.getLocation(), EmpireMode.explosions))) {
                continue;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (isAllowed(e.getEntity().getLocation(), PlotMode.explosions) && isAllowed(e.getEntity().getLocation(), EmpireMode.explosions)) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHostileSpawn(HostileCreaturePreSpawnEvent e) {
        OwnedLand land = LandManager.getInstance().getLand(e.getLocation().getChunk());
        if (land != null && !land.isMobSpawning()) {
            e.setCancelled(true);
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
            Message.get(originalLand.getOwnerVillage().getState().getKey() + ".leave-message").tell(player,
                    "name", originalLand.getOwnerVillage().getName(),
                    "state", originalLand.getOwnerVillage().getState().getTag()
            );
            if (originalLand.getOwnerVillage().equals(playerVillage) && originalLand.getOwnerVillage().isBuff()) {
                Base.getLandPerks().removeEffects(player, originalLand, true);
            }
        }

        if (destinationLand != null) {
            if (!sameVillage) {
                if (destinationLand.getOwnerVillage().isPvp()) {
                    MessageUtils.tell(player, Message.get(destinationLand.getOwnerVillage().getState().getKey() + ".enter-message")
                            .getReplacedComponents(ImmutableMap.of(
                                    "pvp", Message.get("pvp-is-enabled").getComponents(),
                                    "name", TextComponent.fromLegacyText(destinationLand.getOwnerVillage().getName()),
                                    "state", TextComponent.fromLegacyText(destinationLand.getOwnerVillage().getState().getTag())
                            ))
                    );
                } else {
                    Message.get(destinationLand.getOwnerVillage().getState().getKey() + ".enter-message").tell(player,
                            "pvp", "",
                            "name", destinationLand.getOwnerVillage().getName(),
                            "state", destinationLand.getOwnerVillage().getState().getTag()
                    );
                }
                if (destinationLand.getOwnerVillage().equals(playerVillage) && destinationLand.getOwnerVillage().isBuff()) {
                    Base.getLandPerks().giveEffects(player, destinationLand, true);
                }
            }
            Utils.runAsync(() -> {
                TitleBuilder tb = new TitleBuilder();
                StringBuilder actionBar = new StringBuilder();
                if (destinationLand.isPlot()) {
                    actionBar.append(Message.get("enter-plot").getReplacedText(
                            "owner", ResidentManager.getInstance().getResident(destinationLand.getPlotOwner()).getName(),
                            "description", destinationLand.getDescription()
                    ));
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

package de.themoep.settlements.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.world.SimpleChunkLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;

public class SettlementListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Resident res = ResidentManager.getInstance().getResident(event.getPlayer()); // should be cached
        if (res.getId() == null || res.getId().isNull()) {
            res.save();
        }
        long settlementTax = res.hasSettlement() ? res.getSettlement().getTax() : 0;
        long plotTaxes = 0;
        TaxDebtAction plotDebtAction = TaxDebtAction.NONE;
        for (OwnedLand land : LandManager.getInstance().getOwnedPlot(res.getUUID())) {
            if (!res.hasSettlement()
                    || !land.getSettlement().isMember(res)
                    || (!res.hasPermission(land.getSettlement().getKey() + ".tax.exempt")
                            && !land.getSettlement().isChief(res.getSettlement()))) {
                plotTaxes += land.getTax();
                if (land.getSettlement().getTaxDebtAction().ordinal() > plotDebtAction.ordinal()) {
                    plotDebtAction = land.getSettlement().getTaxDebtAction();
                }
            }
        }
        if (!SettlementsPlugin.getInstance().getEconomy().has(event.getPlayer(), plotTaxes + settlementTax)) {
            Message.get("plot-tax.debt-action." + plotDebtAction.getKey() + ".cant-pay").tell(event.getPlayer(), "amount", String.valueOf(plotTaxes));
        }
        if (res.hasSettlement()) {
            if (!res.hasPermission("settlement.tax.exempt") && !SettlementsPlugin.getInstance().getEconomy().has(event.getPlayer(), res.getSettlement().getTax())) {
                Message.get("tax.debt-action." + res.getSettlement().getTaxDebtAction().getKey() + ".cant-pay").tell(event.getPlayer(), "amount", String.valueOf(res.getSettlement().getTax()));
            }

            long upkeep = res.getSettlement().getUpkeep() - res.getSettlement().getTaxIncome();
            if (!res.getSettlement().has(upkeep)) {
                Message.get("upkeep.alert.cant-pay").tell(event.getPlayer(), "amount", String.valueOf(upkeep));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPlace(BlockPlaceEvent event) {
        handleNearbyLandOwners(event, event.getBlock().getLocation(), event.getBlock().getType(), event.getPlayer(), "place");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBreak(BlockBreakEvent event) {
        handleNearbyLandOwners(event, event.getBlock().getLocation(), event.getBlock().getType(), event.getPlayer(), "break");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPlaceHanging(HangingPlaceEvent event) {
        Material type = switch (event.getEntity().getType()) {
            case ITEM_FRAME -> Material.ITEM_FRAME;
            case PAINTING -> Material.PAINTING;
            default -> null;
        };
        handleNearbyLandOwners(event, event.getEntity().getLocation(), type, event.getPlayer(), "place");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBreakHanging(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Material type = switch (event.getEntity().getType()) {
                case ITEM_FRAME -> Material.ITEM_FRAME;
                case PAINTING -> Material.PAINTING;
                default -> null;
            };
            handleNearbyLandOwners(event, event.getEntity().getLocation(), type, (Player) event.getRemover(), "break");
        }
    }

    private void handleNearbyLandOwners(Cancellable event, Location location, Material block, Player player, String type) {
        SimpleChunkLocation chunkLocation = new SimpleChunkLocation(location);
        OwnedLand land = LandManager.getInstance().getLand(chunkLocation);
        if (land == null) {
            Collection<Settlement> landOwners = LandManager.getInstance().getNearbyLandOwners(chunkLocation);
            if (Config.Claiming.Close.Building.BLOCK_OUTSIDE && landOwners.isEmpty()
                    && !Config.Claiming.Close.Building.ALLOWED_BLOCKS.contains(block)
                    && !Config.Claiming.Close.Building.ALLOWED_WORLDS.contains(location.getWorld().getName())
                    && !player.hasPermission("settlements.bypass.buildingradius")) {
                Message.get("close-claims.player.block").tell(player);
                event.setCancelled(true);
                return;
            }

            Resident resident = ResidentManager.getInstance().getResident(player);
            Message notification = Message.get("close-claim.settlement-warning." + type);
            for (Settlement landOwner : landOwners) {
                if (landOwner == null) {
                    continue;
                }
                if (notification.exists()) {
                    for (Resident r : landOwner.getOnlineResidents()) {
                        if (!r.disabledCloseBuildingNotification(landOwner)) {
                            notification.tell(r,
                                    "name", landOwner.getName(),
                                    "settlement", landOwner.getName(),
                                    "state", landOwner.getStateTag()
                            );
                        }
                    }
                }
                if (!resident.disabledCloseBuildingNotification(landOwner)) {
                    Message.get("close-claims.player." + type).tell(player,
                            "name", landOwner.getName(),
                            "settlement", landOwner.getName(),
                            "state", landOwner.getStateTag()
                    );
                    if (Config.Claiming.Close.Building.BLOCK_INSIDE
                            && !Config.Claiming.Close.Building.ALLOWED_BLOCKS.contains(block)
                            && !player.hasPermission("settlements.bypass.buildingradius")) {
                        event.setCancelled(true);
                    }
                    break;
                }
            }
        }
    }
}

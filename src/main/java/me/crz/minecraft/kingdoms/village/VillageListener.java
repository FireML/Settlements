package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.TaxDebtAction;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class VillageListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Resident res = ResidentManager.getInstance().getResident(event.getPlayer()); // should be cached
        long villageTax = res.hasVillage() ? res.getVillage().getTax() : 0;
        long plotTaxes = 0;
        TaxDebtAction plotDebtAction = TaxDebtAction.NONE;
        for (OwnedLand land : LandManager.getInstance().getOwnedPlot(res.getUUID())) {
            if (!res.hasVillage()
                    || !land.getLandOwner().isMember(res)
                    || (!res.hasPermission(land.getLandOwner().getKey() + ".tax.exempt")
                            && !land.getLandOwner().isChief(res.getVillage()))) {
                plotTaxes += land.getTax();
                if (land.getLandOwner().getTaxDebtAction().ordinal() > plotDebtAction.ordinal()) {
                    plotDebtAction = land.getLandOwner().getTaxDebtAction();
                }
            }
        }
        if (!KingdomsPlugin.getInstance().econ.has(event.getPlayer(), plotTaxes + villageTax)) {
            Message.get("plot-tax.debt-action." + plotDebtAction.getKey() + ".cant-pay").tell(event.getPlayer(), "amount", String.valueOf(plotTaxes));
        }
        if (res.hasVillage()) {
            if (!res.hasPermission("village.tax.exempt") && !KingdomsPlugin.getInstance().econ.has(event.getPlayer(), res.getVillage().getTax())) {
                Message.get("tax.debt-action." + res.getVillage().getTaxDebtAction().getKey() + ".cant-pay").tell(event.getPlayer(), "amount", String.valueOf(res.getVillage().getTax()));
            }

            long upkeep = res.getVillage().getUpkeep() - res.getVillage().getTaxIncome();
            if (!res.getVillage().has(upkeep)) {
                Message.get("upkeep.alert.cant-pay").tell(event.getPlayer(), "amount", String.valueOf(upkeep));
            }
            if (res.hasEmpire()) {
                Empire empire = res.getVillage().getEmpire();
                long empireTax = empire.getTax();
                if (!empire.isCapital(res.getVillage()) && !res.getVillage().has(empireTax + upkeep)) {
                    Message.get("empire-tax.debt-action." + empire.getTaxDebtAction().getKey() + ".cant-pay").tell(event.getPlayer(), "amount", String.valueOf(empireTax));
                }
                if (res.getVillageRank().atLeast(VillageRank.getChief())) {
                    long empireUpkeep = empire.getUpkeep() - empire.getTaxIncome();
                    if (!empire.has(empireUpkeep)) {
                        Message.get("upkeep.empire-alert.cant-pay").tell(event.getPlayer(), "amount", String.valueOf(empireUpkeep));
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPlace(BlockPlaceEvent event) {
        notifyNearbyLandOwners(event.getBlock().getLocation(), event.getPlayer(), "place");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBreak(BlockBreakEvent event) {
        notifyNearbyLandOwners(event.getBlock().getLocation(), event.getPlayer(), "break");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPlaceHanging(HangingPlaceEvent event) {
        notifyNearbyLandOwners(event.getEntity().getLocation(), event.getPlayer(), "place");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBreakHanging(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            notifyNearbyLandOwners(event.getEntity().getLocation(), (Player) event.getRemover(), "break");
        }
    }

    private void notifyNearbyLandOwners(Location location, Player player, String type) {
        SimpleChunkLocation chunkLocation = new SimpleChunkLocation(location);
        OwnedLand land = LandManager.getInstance().getLand(chunkLocation);
        if (land == null) {
            Resident resident = ResidentManager.getInstance().getResident(player);
            for (LandOwner landOwner : LandManager.getInstance().getNearbyLandOwners(chunkLocation)) {
                if (resident.isReceivingCloseBuildingNotification(landOwner)) {
                    Message.get("close-claims.player." + type).tell(player,
                            "settlement", landOwner.getName(),
                            "state", landOwner.getStateTag(),
                            "time", String.valueOf(Config.Settlement.Claiming.Close.BuildingNotification.TOGGLE_TIME)
                    );
                    break;
                }
            }
        }
    }
}

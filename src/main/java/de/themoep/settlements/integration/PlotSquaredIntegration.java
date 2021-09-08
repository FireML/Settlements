package de.themoep.settlements.integration;

import com.plotsquared.bukkit.player.BukkitPlayerManager;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.village.VillageCommand;
import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.land.events.LandownerChiefChangeEvent;
import de.themoep.settlements.land.events.LandownerDisbandEvent;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.village.events.SettlementJoinEvent;
import de.themoep.settlements.village.events.SettlementLeaveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.logging.Level;

public class PlotSquaredIntegration implements Listener, ConfigMessages {

    private PlotArea plotArea = null;

    public PlotSquaredIntegration() {
        if (!Config.PlotSquared.PLOT_WORLD.isEmpty()) {
            if (!Config.PlotSquared.PLOT_AREA_ID.isEmpty()) {
                plotArea = PlotSquared.get().getPlotAreaManager().getPlotArea(Config.PlotSquared.PLOT_WORLD, Config.PlotSquared.PLOT_AREA_ID);
            } else {
                for (PlotArea area : PlotSquared.get().getPlotAreaManager().getAllPlotAreas()) {
                    if (area.getWorldName().equalsIgnoreCase(Config.PlotSquared.PLOT_WORLD)) {
                        plotArea = area;
                        break;
                    }
                }
            }
        } else {
            PlotArea[] areas = PlotSquared.get().getPlotAreaManager().getAllPlotAreas();
            plotArea = areas.length > 0 ? areas[0] : null;
        }

        if (plotArea != null) {
            SettlementsPlugin.logDebug("Using PlotArea " + plotArea.getId() + " in world " + plotArea.getWorldName());
        } else {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not find a PlotArea. Please configure it in PlotSquared and the config! PlotSquared integration will be disabled!");
            return;
        }

        Perk.registerAction("plot", (player, landOwner, enable) -> {
            if (player == null) {
                return;
            }
            if (plotArea != null) {
                Set<Plot> plots = plotArea.getPlots(landOwner.getChiefId());
                if (plots.isEmpty()) {
                    Plot plot = plotArea.getNextFreePlot(((BukkitPlayerManager) PlotSquared.platform().playerManager()).getPlayer(player), null);
                    if (plot == null) {
                        return;
                    }

                    plot.setOwnerAbs(landOwner.getChiefId());

                    DBFunc.createPlotSafe(plot, () -> {
                        for (Resident resident : landOwner.getResidents()) {
                            plot.addTrusted(resident.getUUID());
                        }
                        plotArea.addPlot(plot);
                        teleportToPlot(player, plot);
                    }, () -> getError("failed-to-claim").tell(player));
                    return;
                }
                teleportToPlot(player, plots.iterator().next());
            }
        }, (landOwner) -> {
            if (plotArea != null) {
                Set<Plot> plots = plotArea.getPlots(landOwner.getChiefId());
                if (!plots.isEmpty()) {
                    return "purchased";
                }
            }
            return "purchasable";
        });

        VillageCommand villageCommand = (VillageCommand) CommandManager.getInstance().getCommand("village");
        villageCommand.registerCommand(new VillageSubCommand(villageCommand, "mapart", "plotsquared") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                Set<Plot> plots = plotArea.getPlots(resident.getVillage().getChiefId());
                if (!plots.isEmpty()) {
                    teleportToPlot(resident.getPlayer(), plots.iterator().next());
                } else {
                    getMessage("no-plot").tell(resident, "state", resident.getVillage().getStateTag(), "base", base);
                }
                return true;
            }
        });

        SettlementsPlugin.getInstance().registerListener(this);
    }

    private void teleportToPlot(Player player, Plot plot) {
        Utils.teleport(this, player, "plotsquared", () -> plot.teleportPlayer(((BukkitPlayerManager) PlotSquared.platform().playerManager()).getPlayer(player), TeleportCause.PLUGIN, (success) -> {}));
    }

    public String getPlotSquaredVersion() {
        return PlotSquared.get().getVersion().versionString();
    }

    @EventHandler
    public void onJoin(SettlementJoinEvent event) {
        if (plotArea != null) {
            Set<Plot> plots = plotArea.getPlots(event.getSettlement().getChiefId());
            for (Plot plot : plots) {
                plot.addTrusted(event.getResident().getUUID());
            }
        }
    }

    @EventHandler
    public void onLeave(SettlementLeaveEvent event) {
        if (plotArea != null) {
            Set<Plot> plots = plotArea.getPlots(event.getSettlement().getChiefId());
            for (Plot plot : plots) {
                plot.removeTrusted(event.getResident().getUUID());
            }
        }
    }

    @EventHandler
    public void onDisband(LandownerDisbandEvent event) {
        if (plotArea != null) {
            Set<Plot> plots = plotArea.getPlots(event.getLandOwner().getChiefId());
            for (Plot plot : plots) {
                plotArea.removePlot(plot.getId());
            }
        }
    }

    @EventHandler
    public void onJoin(LandownerChiefChangeEvent event) {
        if (plotArea != null) {
            Set<Plot> plots = plotArea.getPlots(event.getOldChief().getUUID());
            for (Plot plot : plots) {
                plot.setOwner(event.getNewChief().getUUID());
                plot.addTrusted(event.getOldChief().getUUID());
            }
        }
    }

    @Override
    public String type() {
        return "integration";
    }

    @Override
    public String name() {
        return "plotsquared";
    }
}

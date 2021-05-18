package me.crz.minecraft.kingdoms.integration;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.land.events.LandownerDisbandEvent;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.village.events.SettlementJoinEvent;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.village.events.SettlementLeaveEvent;
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
                plotArea = PlotSquared.get().getPlotArea(Config.PlotSquared.PLOT_WORLD, Config.PlotSquared.PLOT_AREA_ID);
            } else {
                for (PlotArea area : PlotSquared.get().getPlotAreaManager().getAllPlotAreas()) {
                    if (area.getWorldName().equalsIgnoreCase(Config.PlotSquared.PLOT_WORLD)) {
                        plotArea = area;
                        break;
                    }
                }
            }
        } else {
            plotArea = PlotSquared.get().getFirstPlotArea();
        }

        if (plotArea != null) {
            KingdomsPlugin.logDebug("Using PlotArea " + plotArea.getId() + " in world " + plotArea.getWorldName());
        } else {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not find a PlotArea. Please configure it in the config! PlotSquared integration will be disabled!");
            return;
        }

        Perk.registerAction("plot", (player, landOwner, enable) -> {
            if (!enable || player == null) {
                return;
            }
            if (plotArea != null) {
                Set<Plot> plots = plotArea.getPlots(landOwner.getUUID());
                if (plots.isEmpty()) {
                    // TODO: Claim plot if not claimed
                    Plot plot = plotArea.getNextFreePlot(PlotSquared.imp().wrapPlayer(player), null);
                    if (plot == null) {
                        return;
                    }

                    plot.setOwnerAbs(landOwner.getUUID());

                    for (Resident resident : landOwner.getResidents()) {
                        plot.addTrusted(resident.getUUID());
                    }

                    DBFunc.createPlotSafe(plot, () -> teleportToPlot(player, plot), () -> getError("failed-to-claim").tell(player));
                    return;
                }
                teleportToPlot(player, plots.iterator().next());
            }
        }, null);
        KingdomsPlugin.getInstance().registerListener(this);
    }

    private void teleportToPlot(Player player, Plot plot) {
        Utils.teleport(this, player, "plotsquared", () -> {
            plot.teleportPlayer(PlotSquared.imp().wrapPlayer(player), TeleportCause.PLUGIN, (success) -> {
                String path = "teleport." + (success ? "success" : "fail");
                Message.get(path).tell(player);
                new TitleBuilder()
                        .withTitle(getMessage(path + "-title.title"))
                        .withSubtitle(getMessage(path + "-title.subtitle"))
                        .withActionbar(getMessage(path + "-title.actionbar"))
                        .send(player);
            });
        });
    }

    public String getPlotSquaredVersion() {
        return PlotSquared.get().getVersion().versionString();
    }

    @EventHandler
    public void onJoin(SettlementJoinEvent event) {
        if (plotArea != null) {
            Set<Plot> plots = plotArea.getPlots(event.getSettlement().getUUID());
            for (Plot plot : plots) {
                plot.addTrusted(event.getResident().getUUID());
            }
        }
    }

    @EventHandler
    public void onLeave(SettlementLeaveEvent event) {
        if (plotArea != null) {
            Set<Plot> plots = plotArea.getPlots(event.getSettlement().getUUID());
            for (Plot plot : plots) {
                plot.removeTrusted(event.getResident().getUUID());
            }
        }
    }

    @EventHandler
    public void onDisband(LandownerDisbandEvent event) {
        if (plotArea != null) {
            Set<Plot> plots = plotArea.getPlots(event.getLandOwner().getUUID());
            for (Plot plot : plots) {
                plot.deletePlot(null);
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

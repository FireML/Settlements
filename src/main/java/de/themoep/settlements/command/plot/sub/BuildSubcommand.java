package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.BuildStateArgument;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class BuildSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("build")
            .then(
                Commands.literal("reset")
                    .executes(ctx -> {
                        Player player = ctx.getSource().getPlayerOrThrow();
                        OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                        if (land == null) {
                            ConfigMessage.getError("not-a-claim").send(player);
                            return 1;
                        }
                        if (!LandUtils.checkPlotAdmin(player, land)) {
                            return 1;
                        }
                        if (land.isPlot()) {
                            land.setBuildState(null);
                            LandManager.getInstance().saveLand(land);
                            ConfigMessage.get("command.plot.build.reset")
                                .replace("{parent}", land.getSettlement().getStateTag())
                                .replace("{state}", land.getSettlement().getBuildState().getTag())
                                .replace("{type}", land.getBuildTag())
                                .send(player);
                        } else {
                            ConfigMessage.getError("not-a-plot").send(player);
                        }
                        return 1;
                    })
            )
            .then(
                Commands.argument("state", new BuildStateArgument())
                    .executes(ctx -> {
                        Player player = ctx.getSource().getPlayerOrThrow();
                        OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                        if (land == null) {
                            ConfigMessage.getError("not-a-claim").send(player);
                            return 1;
                        }
                        if (!LandUtils.checkPlotAdmin(player, land)) {
                            return 1;
                        }
                        BuildState state = ctx.getArgument("state", BuildState.class);
                        BuildState.AppliesTo appliesTo = land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.SETTLEMENT;
                        if (!state.appliesTo(appliesTo)) {
                            throw new IllegalStateException("Invalid build state for " + appliesTo.name() + ": " + state);
                        }
                        land.setBuildState(state);
                        LandManager.getInstance().saveLand(land);
                        ConfigMessage.get("command.land.build.success")
                            .replace("{state}", state.getTag())
                            .replace("{type}", land.getBuildTag())
                            .replace("{flagable}", land.getStateTag())
                            .send(player);
                        return 1;
                    })
            );
    }

}

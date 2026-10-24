package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class UnownSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("unown")
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
                if (!land.isPlot()) {
                    ConfigMessage.getError("not-a-claim").send(player);
                    return 1;
                }
                if (land.isPlotOwner(player)) {
                    ConfigMessage.get("command.plot.unown.success").send(player);
                } else {
                    ConfigMessage.get("command.plot.unown.success-other")
                        .replace("{owner}", land.getPlotOwnerResident().getName())
                        .send(player);
                }
                LandManager.getInstance().setPlotOwner(land, null);
                return 1;
            });
    }

}

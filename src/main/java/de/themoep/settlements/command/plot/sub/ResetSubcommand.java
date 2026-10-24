package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ResetSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("reset")
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
                for (PlotMode mode : PlotMode.values()) {
                    land.unsetMode(mode);
                }
                for (UseFlag use : UseFlagManager.getInstance().getUseFlags()) {
                    land.unsetUseState(use);
                }
                ConfigMessage.get("command.plot.reset.success").send(player);
                return 1;
            });
    }

}

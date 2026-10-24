package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class TaxSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("tax")
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
                ConfigMessage.get("command.plot.tax.current")
                    .replace("{tax}", land.getTax())
                    .send(player);
                return 1;
            })
            .then(
                Commands.argument("amount", IntegerArgumentType.integer(0, Config.Plot.MAX_TAX))
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
                            ConfigMessage.get("command.plot.tax.set.already-owned").send(player);
                            return 1;
                        }
                        int amount = ctx.getArgument("amount", int.class);
                        land.setTax(amount);
                        ConfigMessage.get("command.plot.tax.set.success")
                            .replace("{amount}", amount)
                            .send(player);
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                        return 1;
                    })
            );
    }

}

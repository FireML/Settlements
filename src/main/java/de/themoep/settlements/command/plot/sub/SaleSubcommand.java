package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class SaleSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("sale")
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
                if (land.isForSale()) {
                    ConfigMessage.get("command.plot.sale.current")
                        .replace("{price}", land.getPrice())
                        .send(player);
                } else {
                    ConfigMessage.getError("not-for-sale").send(player);
                }
                return 1;
            })
            .then(
                Commands.literal("off")
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
                            ConfigMessage.get("command.plot.sale.already-owned").send(player);
                            return 1;
                        }
                        if (!land.isForSale()) {
                            ConfigMessage.getError("not-for-sale").send(player);
                            return 1;
                        }
                        land.setForSale(false);
                        land.setEmbassy(false);
                        LandManager.getInstance().saveLand(land);
                        ConfigMessage.get("command.plot.sale.no-longer-for-sale").send(player);
                        return 1;
                    })
            )
            .then(
                Commands.argument("amount", IntegerArgumentType.integer(0))
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
                            ConfigMessage.get("command.plot.sale.already-owned").send(player);
                            return 1;
                        }
                        int amount = ctx.getArgument("amount", int.class);
                        land.setPrice(amount);
                        land.setForSale(true);
                        LandManager.getInstance().saveLand(land);
                        ConfigMessage.get("command.plot.sale.success")
                            .replace("{price}", amount)
                            .send(player);
                        return 1;
                    })
            );
    }

}

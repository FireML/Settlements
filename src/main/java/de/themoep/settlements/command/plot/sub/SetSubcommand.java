package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.command.arguments.EnumArgument;

public class SetSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("set")
            .then(embassy())
            .then(
                Commands.argument("mode", EnumArgument.enumArgument(PlotMode.class))
                    .then(on())
                    .then(off())
                    .then(inherit())
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> embassy() {
        return Commands.literal("embassy")
            .then(
                Commands.argument("amount", IntegerArgumentType.integer(0))
                    .executes(ctx -> {
                        Player player = ctx.getSource().getPlayerOrThrow();
                        Resident resident = ResidentManager.getInstance().getResident(player);
                        OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                        int amount = ctx.getArgument("amount", int.class);
                        if (land == null) {
                            ConfigMessage.getError("not-a-claim").send(player);
                            return 1;
                        }
                        if (!LandUtils.checkPlotAdmin(resident, land)) {
                            return 1;
                        }
                        if (land.containsHome() || land.isOutpost()) {
                            ConfigMessage.getError("cant-set-embassy").send(player);
                            return 1;
                        }
                        land.setEmbassy(true);
                        if (amount > 0) {
                            land.setForSale(true);
                            land.setPrice(amount);
                            ConfigMessage.get("command.plot.mode.embassy.success")
                                .replace("{value}", String.valueOf(land.getPrice()))
                                .send(player);
                        } else {
                            ConfigMessage.get("command.plot.mode.embassy.success-no-sale")
                                .send(player);
                        }
                        LandManager.getInstance().saveLand(land);
                        return 1;
                    })
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> on() {
        return Commands.literal("on")
            .executes(ctx -> {
                Player player = ctx.getSource().getPlayerOrThrow();
                Resident resident = ResidentManager.getInstance().getResident(player);
                OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                if (land == null) {
                    ConfigMessage.getError("not-a-claim").send(player);
                    return 1;
                }
                if (!LandUtils.checkPlotAdmin(resident, land)) {
                    return 1;
                }
                PlotMode mode = ctx.getArgument("mode", PlotMode.class);
                if (mode == PlotMode.embassy) {
                    throw new IllegalStateException("Embassy mode selected. This error should not be thrown.");
                }
                land.setMode(mode, true);
                ConfigMessage.get("command.plot.mode.success")
                    .replace("{mode}", mode.getTag())
                    .replace("{value}", true)
                    .send(resident);
                LandManager.getInstance().syncAndSave(land);
                return 1;
            });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> off() {
        return Commands.literal("on")
            .executes(ctx -> {
                Player player = ctx.getSource().getPlayerOrThrow();
                Resident resident = ResidentManager.getInstance().getResident(player);
                OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                if (land == null) {
                    ConfigMessage.getError("not-a-claim").send(player);
                    return 1;
                }
                if (!LandUtils.checkPlotAdmin(resident, land)) {
                    return 1;
                }
                PlotMode mode = ctx.getArgument("mode", PlotMode.class);
                if (mode == PlotMode.embassy) {
                    throw new IllegalStateException("Embassy mode selected. This error should not be thrown.");
                }
                land.setMode(mode, false);
                ConfigMessage.get("command.plot.mode.success")
                    .replace("{mode}", mode.getTag())
                    .replace("{value}", false)
                    .send(resident);
                LandManager.getInstance().syncAndSave(land);
                return 1;
            });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> inherit() {
        return Commands.literal("inherit")
            .executes(ctx -> {
                Player player = ctx.getSource().getPlayerOrThrow();
                Resident resident = ResidentManager.getInstance().getResident(player);
                OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                if (land == null) {
                    ConfigMessage.getError("not-a-claim").send(player);
                    return 1;
                }
                if (!LandUtils.checkPlotAdmin(resident, land)) {
                    return 1;
                }
                PlotMode mode = ctx.getArgument("mode", PlotMode.class);
                if (mode == PlotMode.embassy) {
                    throw new IllegalStateException("Embassy mode selected. This error should not be thrown.");
                }
                if (!mode.appliesTo(PlotMode.AppliesTo.LAND_OWNER)) {
                    ConfigMessage.get("command.plot.mode.no-parent")
                        .replace("{mode}", mode.getTag())
                        .replace("{parent}", land.getSettlement().getStateTag())
                        .send(resident);
                }
                land.unsetMode(mode);
                ConfigMessage.get("command.plot.mode.inherit")
                    .replace("{mode}", mode.getTag())
                    .replace("{value}", land.getSettlement().getMode(mode))
                    .replace("{parent}", land.getSettlement().getStateTag())
                    .send(resident);
                LandManager.getInstance().syncAndSave(land);
                return 1;
            });
    }

}

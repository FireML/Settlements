package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import de.themoep.settlements.command.argument.BuildStateArgument;
import de.themoep.settlements.command.argument.UseFlagArgument;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class PermissionsSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("permissions")
            .then(all())
            .then(flagArg());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> all() {
        return Commands.literal("all")
            .then(
                Commands.literal("inherit")
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
                        for (UseFlag flag : UseFlagManager.getInstance().getUseFlags()) {
                            land.unsetUseState(flag);
                        }
                        LandManager.getInstance().syncAndSave(land);
                        ConfigMessage.get("command.plot.perms.inherit-all")
                            .replace("{parent}", land.getSettlement().getStateTag())
                            .send(player);
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
                        for (UseFlag flag : UseFlagManager.getInstance().getUseFlags()) {
                            land.setUseState(flag, state);
                        }
                        LandManager.getInstance().syncAndSave(land);
                        ConfigMessage.get("command.plot.perms.success-all")
                            .replace("{state}", state.getTag())
                            .send(player);
                        return 1;
                    })
            );
    }

    private static RequiredArgumentBuilder<CommandSourceStack, UseFlag> flagArg() {
        return Commands.argument("flag", new UseFlagArgument())
            .then(
                Commands.literal("inherit")
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
                        UseFlag flag = ctx.getArgument("flag", UseFlag.class);
                        land.unsetUseState(flag);
                        LandManager.getInstance().syncAndSave(land);
                        ConfigMessage.get("command.plot.perms.inherit")
                            .replace("{flag}", flag.name())
                            .replace("{parent}", land.getSettlement().getStateTag())
                            .send(player);
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
                        UseFlag flag = ctx.getArgument("flag", UseFlag.class);
                        BuildState state = ctx.getArgument("state", BuildState.class);
                        land.setUseState(flag, state);
                        LandManager.getInstance().syncAndSave(land);
                        ConfigMessage.get("command.plot.perms.success")
                            .replace("{flag}", flag.name())
                            .replace("{state}", state.getTag())
                            .send(player);
                        return 1;
                    })
            );
    }

}

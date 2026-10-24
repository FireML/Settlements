package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.LandUtils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class SetDescriptionSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("setdescription")
            .then(
                Commands.argument("description", StringArgumentType.greedyString())
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
                        String description = ctx.getArgument("description", String.class);
                        if (description.length() > Config.Plot.DESCRIPTION_LENGTH) {
                            ConfigMessage.getError("string-too-long")
                                .replace("{input}", description)
                                .replace("{amount}", description.length())
                                .replace("{max}", Config.Plot.DESCRIPTION_LENGTH)
                                .send(player);
                            return 1;
                        }
                        land.setDescription(description);
                        LandManager.getInstance().syncAndSave(land);
                        ConfigMessage.get("command.plot.setdescription.success")
                            .replace("{description}", description)
                            .send(player);
                        return 1;
                    })
            );
    }

}

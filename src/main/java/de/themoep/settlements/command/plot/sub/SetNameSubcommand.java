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

public class SetNameSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("setname")
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
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
                        String name = ctx.getArgument("name", String.class);
                        if (name.length() > Config.Plot.NAME_LENGTH) {
                            ConfigMessage.getError("string-too-long")
                                .replace("{input}", name)
                                .replace("{amount}", name.length())
                                .replace("{max}", Config.Plot.NAME_LENGTH)
                                .send(player);
                            return 1;
                        }
                        land.setName(name);
                        LandManager.getInstance().syncAndSave(land);
                        ConfigMessage.get("command.plot.setname.success")
                            .replace("{name}", name)
                            .send(player);
                        return 1;
                    })
            );
    }

}

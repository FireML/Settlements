package de.themoep.settlements.command.settlement.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.utils.dialog.claim.ClaimMap;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ClaimMapSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("claimmap")
            .executes(ctx -> {
                Player player = ctx.getSource().getPlayerOrThrow();
                ClaimMap.open(player);
                return 1;
            });
    }

}

package de.themoep.settlements.command.settlement;

import com.mojang.brigadier.tree.LiteralCommandNode;
import de.themoep.settlements.command.settlement.sub.ClaimMapSubcommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class SettlementCommand {

    public static @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("settlement")
            .then(ClaimMapSubcommand.get())
            .build();
    }

}

package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.settlement.Settlement;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class TriggerUpkeepSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("triggerupkeep")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .executes(ctx -> {
                        Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                        settlement.doUpkeep();
                        ctx.getSource().getSender().sendPlainMessage("Manually triggered upkeep for Settlement " + settlement.getName());
                        return 1;
                    })
            );
    }

}

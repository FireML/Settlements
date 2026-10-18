package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.settlement.Settlement;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class FinishedMissionsSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("finishedmissions")
            .then(add())
            .then(remove())
            .then(set());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> add() {
        return Commands.literal("add")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                long amount = ctx.getArgument("amount", long.class);
                                settlement.addFinishedMissions(amount);
                                ctx.getSource().getSender().sendPlainMessage("Added " + amount + " finished missions to Settlement " + settlement.getName());
                                settlement.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> remove() {
        return Commands.literal("remove")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                long amount = ctx.getArgument("amount", long.class);
                                settlement.removeFinishedMissions(amount);
                                ctx.getSource().getSender().sendPlainMessage("Removed " + amount + " finished missions from Settlement " + settlement.getName());
                                settlement.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> set() {
        return Commands.literal("set")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                long amount = ctx.getArgument("amount", long.class);
                                settlement.addFinishedMissions(amount);
                                ctx.getSource().getSender().sendPlainMessage("Set Settlement " + settlement.getName() + "'s finished missions to " + amount);
                                settlement.save();
                                return 1;
                            })
                    )
            );
    }

}

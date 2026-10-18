package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementState;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.command.arguments.EnumArgument;

public class SetLevelSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("setlevel")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .then(
                        Commands.argument("state", EnumArgument.enumArgument(SettlementState.class))
                            .then(
                                Commands.argument("level", IntegerArgumentType.integer(1))
                                    .then(
                                        Commands.argument("triggerevents", BoolArgumentType.bool())
                                            .executes(ctx -> {
                                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                                SettlementState state = ctx.getArgument("state", SettlementState.class);
                                                int level = ctx.getArgument("level", int.class);
                                                boolean triggerEvents = ctx.getArgument("triggerevents", boolean.class);
                                                execute(
                                                    ctx.getSource().getSender(),
                                                    settlement,
                                                    state,
                                                    level,
                                                    triggerEvents
                                                );
                                                return 1;
                                            })
                                    )
                            )
                    )
            );
    }

    private static void execute(CommandSender sender, Settlement settlement, SettlementState state, int level, boolean triggerEvents) {
        if (SettlementManager.getInstance().getLevel(state, level).isEmpty()) {
            sender.sendPlainMessage("Level " + state + " " + level + " does not exist.");
            return;
        }
        settlement.setState(state);
        settlement.setLevel(level);
        sender.sendPlainMessage("Set " + settlement.getName() + " to " + state + " " + level);
        if (triggerEvents) {
            SettlementManager.getInstance().onLevelUp(settlement, settlement.getChief());
            sender.sendPlainMessage("Triggered level up events.");
        }
        settlement.save();
    }

}

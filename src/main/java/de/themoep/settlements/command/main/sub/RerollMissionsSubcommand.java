package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class RerollMissionsSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("rerollmissions")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .executes(ctx -> {
                        Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                        int newMissions = settlement.distributeNewMissions();
                        if (newMissions > 0) {
                            ctx.getSource().getSender().sendPlainMessage("Rerolled " + newMissions + " missions for all residents of " + settlement.getName());
                        } else {
                            ctx.getSource().getSender().sendPlainMessage("Failed to reroll missions for all residents of " + settlement.getName());
                        }
                        return 1;
                    })
            );
    }

}

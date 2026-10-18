package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.resident.Resident;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class RerollMissionSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("rerollmission")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .executes(ctx -> {
                        Resident resident = ctx.getArgument("resident", Resident.class);
                        int newMissions = resident.distributeNewMissions();
                        if (newMissions > 0) {
                            ctx.getSource().getSender().sendPlainMessage("Rerolled " + resident.getName() + "'s Missions. They have " + newMissions);
                        } else {
                            ctx.getSource().getSender().sendPlainMessage("Failed to reroll " + resident.getName() + "'s Missions.");
                        }
                        return 1;
                    })
            );
    }

}

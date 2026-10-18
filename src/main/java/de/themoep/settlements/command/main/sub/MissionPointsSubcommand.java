package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.resident.Resident;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class MissionPointsSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("missionpoints")
            .then(add())
            .then(remove())
            .then(set());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> add() {
        return Commands.literal("add")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .then(
                        Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                Resident resident = ctx.getArgument("resident", Resident.class);
                                int amount = ctx.getArgument("amount", int.class);
                                resident.addMissionPoints(amount);
                                ctx.getSource().getSender().sendPlainMessage("Added " + amount + " mission points to Resident " + resident.getName());
                                resident.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> remove() {
        return Commands.literal("remove")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .then(
                        Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                Resident resident = ctx.getArgument("resident", Resident.class);
                                int amount = ctx.getArgument("amount", int.class);
                                resident.removeMissionPoints(amount);
                                ctx.getSource().getSender().sendPlainMessage("Removed " + amount + " mission points from Resident " + resident.getName());
                                resident.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> set() {
        return Commands.literal("set")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .then(
                        Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(ctx -> {
                                Resident resident = ctx.getArgument("resident", Resident.class);
                                int amount = ctx.getArgument("amount", int.class);
                                resident.setMissionPoints(amount);
                                ctx.getSource().getSender().sendPlainMessage("Set Resident " + resident.getName() + "'s mission points to " + amount);
                                resident.save();
                                return 1;
                            })
                    )
            );
    }

}

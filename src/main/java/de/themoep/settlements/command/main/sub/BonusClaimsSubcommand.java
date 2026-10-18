package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class BonusClaimsSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("bonusclaims")
            .then(addSettlement())
            .then(removeSettlement())
            .then(setSettlement())
            .then(addResident())
            .then(removeResident())
            .then(setResident());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addSettlement() {
        return Commands.literal("add-settlement")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                long amount = ctx.getArgument("amount", long.class);
                                settlement.addBonusClaims(amount);
                                ctx.getSource().getSender().sendPlainMessage("Added " + amount + " bonus claims to Settlement " + settlement.getName());
                                settlement.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeSettlement() {
        return Commands.literal("remove-settlement")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                long amount = ctx.getArgument("amount", long.class);
                                settlement.removeBonusClaims(amount);
                                ctx.getSource().getSender().sendPlainMessage("Removed " + amount + " bonus claims from Settlement " + settlement.getName());
                                settlement.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setSettlement() {
        return Commands.literal("set-settlement")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                long amount = ctx.getArgument("amount", long.class);
                                settlement.setBonusClaims(amount);
                                ctx.getSource().getSender().sendPlainMessage("Set Settlement " + settlement.getName() + "'s bonus claims to " + amount);
                                settlement.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addResident() {
        return Commands.literal("add-resident")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Resident resident = ctx.getArgument("resident", Resident.class);
                                long amount = ctx.getArgument("amount", long.class);
                                resident.addBonusClaims(amount);
                                ctx.getSource().getSender().sendPlainMessage("Added " + amount + " bonus claims to Resident " + resident.getName());
                                resident.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> removeResident() {
        return Commands.literal("remove-resident")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Resident resident = ctx.getArgument("resident", Resident.class);
                                long amount = ctx.getArgument("amount", long.class);
                                resident.removeBonusClaims(amount);
                                ctx.getSource().getSender().sendPlainMessage("Removed " + amount + " bonus claims from Resident " + resident.getName());
                                resident.save();
                                return 1;
                            })
                    )
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setResident() {
        return Commands.literal("set-resident")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Resident resident = ctx.getArgument("resident", Resident.class);
                                long amount = ctx.getArgument("amount", long.class);
                                resident.setBonusClaims(amount);
                                ctx.getSource().getSender().sendPlainMessage("Set Resident " + resident.getName() + "'s bonus claims to " + amount);
                                resident.save();
                                return 1;
                            })
                    )
            );
    }

}

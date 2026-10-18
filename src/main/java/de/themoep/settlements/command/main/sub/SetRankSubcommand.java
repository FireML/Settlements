package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.command.argument.SettlementRankArgument;
import de.themoep.settlements.land.events.SettlementChiefChangeEvent;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class SetRankSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("setrank")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .then(
                        Commands.argument("rank", new SettlementRankArgument())
                            .executes(ctx -> {
                                Resident resident = ctx.getArgument("resident", Resident.class);
                                SettlementRank rank = ctx.getArgument("rank", SettlementRank.class);
                                Settlement settlement = resident.getSettlement();
                                if (settlement == null) {
                                    ctx.getSource().getSender().sendPlainMessage("Resident is not in a settlement.");
                                    return 1;
                                }
                                if (settlement.getRank(resident).equals(rank)) {
                                    ctx.getSource().getSender().sendPlainMessage("Resident already has that rank.");
                                    return 1;
                                }
                                if (rank.isChief()) {
                                    Resident chief = settlement.getChief();
                                    new SettlementChiefChangeEvent(settlement, chief, resident).callEvent();

                                    chief.setSettlementRank(resident.getSettlementRank());
                                    resident.setSettlementRank(rank);
                                    settlement.setChief(resident.getUUID());

                                    settlement.save();
                                    chief.save();
                                } else {
                                    resident.setSettlementRank(rank);
                                }
                                resident.save();
                                ctx.getSource().getSender().sendPlainMessage("Successfully set " + resident.getName() + "'s Settlement Rank to " + rank.getTag(settlement));
                                return 1;
                            })
                    )
            );
    }

}

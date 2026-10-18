package de.themoep.settlements.command.main.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class JoinSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("join")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .then(
                        Commands.argument("settlement", new SettlementArgument())
                            .executes(ctx -> {
                                Resident resident = ctx.getArgument("resident", Resident.class);
                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                Settlement resSettlement = resident.getSettlement();
                                if (settlement.equals(resSettlement)) { // Already in that settlement
                                    ctx.getSource().getSender().sendPlainMessage("Resident is already in that settlement.");
                                    return 1;
                                }
                                if (resSettlement != null) { // Already in another settlement
                                    ctx.getSource().getSender().sendPlainMessage("Resident is already in a settlement. They must be removed before using this command.");
                                    return 1;
                                }
                                settlement.addMember(resident);
                                ctx.getSource().getSender().sendPlainMessage(resident.getName() + " is now a member of " + settlement.getName());
                                MessageUtils.tellResidents(
                                    settlement.getOnlineResidents(),
                                    Message.get("command.settlement.join.new-resident").getReplacedComponent("player", resident.getName())
                                );
                                return 1;
                            })
                    )
            );
    }

}

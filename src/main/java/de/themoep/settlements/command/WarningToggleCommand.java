package de.themoep.settlements.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class WarningToggleCommand {

    private static final SimpleCommandExceptionType NOT_A_RESIDENT = new SimpleCommandExceptionType(
        new LiteralMessage("You are not a resident?")
    );

    public static @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("warningtoggle")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .executes(ctx -> {
                        Resident resident = ResidentManager.getInstance().getResident(ctx.getSource().getPlayerOrThrow());
                        if (resident == null) {
                            throw NOT_A_RESIDENT.create();
                        }
                        Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                        String option = resident.toggleCloseBuildingNotification(settlement) ? "enabled" : "disabled";
                        ConfigMessage.get("command.warningtoggle." + option)
                            .replace("state", settlement.getStateTag())
                            .replace("settlement", settlement.getName())
                            .replace("time", Config.Claiming.Close.Building.TOGGLE_TIME)
                            .send(ctx.getSource().getSender());
                        return 1;
                    })
            ).build();
    }

}

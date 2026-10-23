package de.themoep.settlements.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.command.exceptions.NotAResidentException;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.message.ComponentMessage;

public class WarningToggleCommand {

    public static @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("warningtoggle")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .executes(ctx -> {
                        Resident resident = ResidentManager.getInstance().getResident(ctx.getSource().getPlayerOrThrow());
                        if (resident == null) {
                            throw new NotAResidentException().create();
                        }
                        Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                        if (settlement.equals(resident.getSettlement())) {
                            sendMessage(
                                ConfigMessage.get("command.warningtoggle.in-settlement"),
                                ctx.getSource().getSender(),
                                settlement
                            );
                            return 1;
                        }
                        String option;
                        if (resident.disabledCloseBuildingNotification(settlement)) {
                            resident.enableCloseBuildingNotification(settlement);
                            option = "enabled";
                        } else {
                            resident.disableCloseBuildingNotification(settlement);
                            option = "disabled";
                        }
                        sendMessage(
                            ConfigMessage.get("command.warningtoggle." + option),
                            ctx.getSource().getSender(),
                            settlement
                        );
                        return 1;
                    })
            ).build();
    }

    private static void sendMessage(ComponentMessage<?, ?> message, Audience sender, Settlement settlement) {
        message
            .replace("{state}", settlement.getStateTag())
            .replace("{settlement}", settlement.getName())
            .send(sender);
    }

}

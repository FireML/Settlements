package de.themoep.settlements.command.resident.sub;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.SettlementArgument;
import de.themoep.settlements.command.exceptions.NotAResidentException;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.replacer.Replacer;

public class TransferClaimsSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("transferclaims")
            .then(
                Commands.argument("settlement", new SettlementArgument())
                    .then(
                        Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                Resident resident = ResidentManager.getInstance().getResident(ctx.getSource().getPlayerOrThrow());
                                if (resident == null) {
                                    throw new NotAResidentException().create();
                                }
                                Settlement settlement = ctx.getArgument("settlement", Settlement.class);
                                long amount = ctx.getArgument("amount", long.class);
                                Replacer replacer = Replacer.component()
                                    .addReplacement("{name}", resident.getName())
                                    .addReplacement("{amount}", amount)
                                    .addReplacement("{claims}", resident.getBonusClaims())
                                    .addReplacement("{targetstate}", settlement.getStateTag())
                                    .addReplacement("{target}", settlement.getName());
                                if (!resident.removeBonusClaims(amount)) {
                                    ConfigMessage.get("command.resident.claimtransfer.not-enough-bonus")
                                        .replace(replacer)
                                        .send(ctx.getSource().getSender());
                                    return 1;
                                }
                                settlement.addBonusClaims(amount);
                                resident.save();
                                settlement.save();
                                Utils.runAsync(() -> DatabaseManager.getInstance().log(settlement, resident, "deposit", "claim", amount));
                                ConfigMessage.get("command.resident.claimtransfer.success")
                                    .replace(replacer)
                                    .send(ctx.getSource().getSender());
                                if (!settlement.isMember(resident)) {
                                    ConfigMessage.get("command.resident.claimtransfer.notify-settlement")
                                        .replace(replacer)
                                        .send(settlement);
                                }
                                return 1;
                            })
                    )
            );
    }

}

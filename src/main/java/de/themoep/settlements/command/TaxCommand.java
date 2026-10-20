package de.themoep.settlements.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.replacer.Replacer;

public class TaxCommand {

    private static final SimpleCommandExceptionType NOT_A_RESIDENT = new SimpleCommandExceptionType(
        new LiteralMessage("You are not a resident? Please report this to admins.")
    );

    public static @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("tax")
            .executes(ctx -> {
                Player player = ctx.getSource().getPlayerOrThrow();
                Resident resident = ResidentManager.getInstance().getResident(player);
                if (resident == null) {
                    throw NOT_A_RESIDENT.create();
                }
                Settlement settlement = resident.getSettlement();
                if (settlement == null) {
                    sendInfo(player, resident);
                } else {
                    sendInfo(player, settlement, resident);
                }
                return 1;
            }).build();
    }

    private static void sendInfo(@NonNull Player player, @NonNull Settlement settlement, @NonNull Resident resident) {
        long settlementTax = resident.getSettlementTax();
        long plotTax = resident.getPlotTax();
        Replacer replacer = Replacer.component()
            .addReplacement("{state}", settlement.getStateTag())
            .addReplacement("{tax}", Utils.CURRENCY_FORMAT.format(settlementTax))
            .addReplacement("{plottax}", Utils.CURRENCY_FORMAT.format(plotTax))
            .addReplacement("{plotamount}", LandManager.getInstance().getOwnedPlot(resident.getUUID()).size())
            .addReplacement("{totaltax}", Utils.CURRENCY_FORMAT.format(settlementTax + plotTax));
        ConfigMessage.get("command.tax.info").replace(replacer).send(player);
    }

    private static void sendInfo(@NonNull Player player, @NonNull Resident resident) {
        long plotTax = resident.getPlotTax();
        Replacer replacer = Replacer.component()
            .addReplacement("{plotamount}", LandManager.getInstance().getOwnedPlot(resident.getUUID()).size())
            .addReplacement("{totaltax}", Utils.CURRENCY_FORMAT.format(plotTax));
        ConfigMessage.get("command.tax.info-no-settlement").replace(replacer).send(player);
    }

}

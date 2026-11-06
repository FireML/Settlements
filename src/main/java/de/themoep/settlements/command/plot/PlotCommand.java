package de.themoep.settlements.command.plot;

import com.mojang.brigadier.tree.LiteralCommandNode;
import de.themoep.settlements.command.plot.sub.BuildSubcommand;
import de.themoep.settlements.command.plot.sub.BuySubcommand;
import de.themoep.settlements.command.plot.sub.InfoSubcommand;
import de.themoep.settlements.command.plot.sub.MembersSubcommand;
import de.themoep.settlements.command.plot.sub.PermissionsSubcommand;
import de.themoep.settlements.command.plot.sub.ResetSubcommand;
import de.themoep.settlements.command.plot.sub.SaleSubcommand;
import de.themoep.settlements.command.plot.sub.SetDescriptionSubcommand;
import de.themoep.settlements.command.plot.sub.SetNameSubcommand;
import de.themoep.settlements.command.plot.sub.SetSubcommand;
import de.themoep.settlements.command.plot.sub.TaxSubcommand;
import de.themoep.settlements.command.plot.sub.UnownSubcommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class PlotCommand {

    public static @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("plot")
            .then(BuildSubcommand.get())
            .then(BuySubcommand.get())
            .then(InfoSubcommand.get())
            .then(MembersSubcommand.get())
            .then(PermissionsSubcommand.get())
            .then(ResetSubcommand.get())
            .then(SaleSubcommand.get())
            .then(SetDescriptionSubcommand.get())
            .then(SetNameSubcommand.get())
            .then(SetSubcommand.get())
            .then(TaxSubcommand.get())
            .then(UnownSubcommand.get())
            .build();
    }

}

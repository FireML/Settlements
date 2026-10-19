package de.themoep.settlements.command.resident;

import com.mojang.brigadier.tree.LiteralCommandNode;
import de.themoep.settlements.command.exceptions.NotAResidentException;
import de.themoep.settlements.command.resident.sub.ShowSubcommand;
import de.themoep.settlements.command.resident.sub.TransferClaimsSubcommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class ResidentCommand {

    public static @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("resident")
            .executes(ctx -> {
                Resident resident = ResidentManager.getInstance().getResident(ctx.getSource().getPlayerOrThrow());
                if (resident == null) {
                    throw new NotAResidentException().create();
                }
                ShowSubcommand.execute(ctx.getSource().getSender(), resident);
                return 1;
            })
            .then(ShowSubcommand.get())
            .then(TransferClaimsSubcommand.get())
            .build();
    }

}

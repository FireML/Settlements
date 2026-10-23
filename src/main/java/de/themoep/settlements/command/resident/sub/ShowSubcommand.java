package de.themoep.settlements.command.resident.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.command.exceptions.NotAResidentException;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import de.themoep.settlements.utils.dialog.InfoDialogBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import uk.firedev.daisylib.messages.message.ComponentMessage;
import uk.firedev.daisylib.messages.replacer.Replacer;

public class ShowSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("show")
            .executes(ctx -> {
                Resident resident = ResidentManager.getInstance().getResident(ctx.getSource().getPlayerOrThrow());
                if (resident == null) {
                    throw new NotAResidentException().create();
                }
                execute(ctx.getSource().getSender(), resident);
                return 1;
            })
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .executes(ctx -> {
                        execute(ctx.getSource().getSender(), ctx.getArgument("resident", Resident.class));
                        return 1;
                    })
            );
    }

    public static void execute(@NonNull CommandSender sender, @NonNull Resident resident) {
        String key = resident.hasSettlement() ? "command.resident.show.settlement" : "command.resident.show.no-settlement";
        Replacer replacer = Replacer.component()
            .addReplacements(Utils.toParsedMap(resident.getReplacements()));

        ComponentMessage<?, ?> title = ConfigMessage.get("command.resident.show.title")
            .replace(replacer);

        Component info = ConfigMessage.get(key)
            .replace(replacer)
            .toSingleMessage() // Turns it into a single component with newlines
            .get();

        new InfoDialogBuilder()
            .withTitle(title)
            .addContent(info)
            .open(sender);
    }

}

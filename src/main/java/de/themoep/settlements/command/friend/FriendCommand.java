package de.themoep.settlements.command.friend;

import com.mojang.brigadier.tree.LiteralCommandNode;
import de.themoep.settlements.command.friend.sub.AcceptSubcommand;
import de.themoep.settlements.command.friend.sub.AddSubcommand;
import de.themoep.settlements.command.friend.sub.DenySubcommand;
import de.themoep.settlements.command.friend.sub.RemoveSubcommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class FriendCommand {

    public static @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("friend")
            .then(AddSubcommand.get())
            .then(AcceptSubcommand.get())
            .then(DenySubcommand.get())
            .then(RemoveSubcommand.get())
            // TODO add list subcommand.
            .build();
    }

}

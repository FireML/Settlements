package de.themoep.settlements.command.friend.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.config.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class RemoveSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("remove")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .executes(ctx -> {
                        Resident target = ctx.getArgument("resident", Resident.class);
                        Resident resident = ResidentManager.getInstance().getResident(ctx.getSource().getPlayerOrThrow());

                        FriendRequest request = target.removeFriendRequest(resident);
                        if (request != null) {
                            DatabaseManager.getInstance().deleteAsync(request);
                            ConfigMessage.get("command.friend.remove.removed-request")
                                .replacePlainText("{name}", target.getName())
                                .send(resident);
                            return 1;
                        }

                        if (!resident.removeFriend(target)) {
                            ConfigMessage.get("command.friend.remove.not-friends")
                                .replacePlainText("{name}", target.getName())
                                .send(resident);
                            return 1;
                        }
                        target.removeFriend(resident);
                        resident.save();
                        target.save();
                        ConfigMessage.get("command.friend.remove.success")
                            .replacePlainText("{name}", target.getName())
                            .send(resident);
                        ConfigMessage.get("command.friend.remove.success")
                            .replacePlainText("{name}", resident.getName())
                            .send(target);
                        return 1;
                    })
            );
    }

}

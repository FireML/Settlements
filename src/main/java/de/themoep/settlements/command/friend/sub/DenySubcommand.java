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

public class DenySubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("deny")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .executes(ctx -> {
                        Resident target = ctx.getArgument("resident", Resident.class);
                        Resident resident = ResidentManager.getInstance().getResident(ctx.getSource().getPlayerOrThrow());

                        FriendRequest request = resident.removeFriendRequest(target);
                        if (request == null) {
                            ConfigMessage.get("command.friend.deny.no-request")
                                .replacePlainText("{name}", target.getName())
                                .send(resident);
                            return 1;
                        }
                        DatabaseManager.getInstance().delete(request);
                        FriendRequest reverseRequest = target.removeFriendRequest(resident);
                        if (reverseRequest != null) {
                            DatabaseManager.getInstance().delete(reverseRequest);
                        }
                        ConfigMessage.get("command.friend.deny.success.target")
                            .replacePlainText("{name}", target.getName())
                            .send(resident);
                        ConfigMessage.get("command.friend.deny.success.sender")
                            .replacePlainText("{name}", resident.getName())
                            .send(target);
                        target.save();
                        resident.save();
                        return 1;
                    })
            );
    }

}

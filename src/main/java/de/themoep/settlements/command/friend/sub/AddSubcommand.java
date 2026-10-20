package de.themoep.settlements.command.friend.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.config.ConfigMessage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class AddSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("add")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .executes(ctx -> {
                        Resident target = ctx.getArgument("resident", Resident.class);
                        Resident resident = ResidentManager.getInstance().getResident(ctx.getSource().getPlayerOrThrow());
                        if (resident.isFriend(target)) {
                            ConfigMessage.get("command.friend.add.already-friends")
                                .replacePlainText("{name}", target.getName())
                                .send(resident);
                            return 1;
                        }
                        FriendRequest request = new FriendRequest(target, resident);
                        if (!target.addFriendRequest(request)) {
                            ConfigMessage.get("command.friend.add.already-sent-request")
                                .replacePlainText("{name}", target.getName())
                                .send(resident);
                            return 1;
                        }
                        request.save();
                        ConfigMessage.get("command.friend.add.success")
                            .replacePlainText("{name}", target.getName())
                            .send(resident);
                        ConfigMessage.get("command.friend.add.received")
                            .replacePlainText("{name}", resident.getName())
                            .send(target);
                        return 1;
                    })
            );
    }

}

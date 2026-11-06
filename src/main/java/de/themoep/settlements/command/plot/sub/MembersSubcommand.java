package de.themoep.settlements.command.plot.sub;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.themoep.settlements.command.argument.ResidentArgument;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.checks.LandChecks;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import de.themoep.settlements.utils.dialog.InfoDialogBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public class MembersSubcommand {

    public static @NonNull LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("members")
            .executes(ctx -> {
                Player player = ctx.getSource().getPlayerOrThrow();
                OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                if (land == null) {
                    ConfigMessage.getError("not-a-claim").send(player);
                    return 1;
                }
                Utils.runAsync(() -> {
                    List<Resident> residents = land.getPlotMembers().stream()
                        .map(ResidentManager.getInstance()::getResident)
                        .filter(Objects::nonNull)
                        .toList();
                    InfoDialogBuilder.residents(residents).withTitle("Plot Members").open(player);
                });
                return 1;
            })
            .then(add())
            .then(remove());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> add() {
        return Commands.literal("add")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .executes(ctx -> {
                        Player player = ctx.getSource().getPlayerOrThrow();
                        OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                        if (land == null) {
                            ConfigMessage.getError("not-a-claim").send(player);
                            return 1;
                        }
                        if (!LandChecks.checkPlotAdmin(player, land)) {
                            return 1;
                        }
                        Resident target = ctx.getArgument("resident", Resident.class);
                        if (land.addPlotMember(target.getUUID())) {
                            LandManager.getInstance().syncAndSave(land);
                            ConfigMessage.get("command.plot.members.add.success")
                                .replace("{name}", target.getName())
                                .send(player);
                        } else {
                            ConfigMessage.get("command.plot.members.add.already-a-member")
                                .replace("{name}", target.getName())
                                .send(player);
                        }
                        return 1;
                    })
            );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> remove() {
        return Commands.literal("remove")
            .then(
                Commands.argument("resident", new ResidentArgument())
                    .executes(ctx -> {
                        Player player = ctx.getSource().getPlayerOrThrow();
                        OwnedLand land = LandManager.getInstance().getLand(player.getChunk());
                        if (land == null) {
                            ConfigMessage.getError("not-a-claim").send(player);
                            return 1;
                        }
                        if (!LandChecks.checkPlotAdmin(player, land)) {
                            return 1;
                        }
                        Resident target = ctx.getArgument("resident", Resident.class);
                        if (land.removePlotMember(target.getUUID())) {
                            LandManager.getInstance().syncAndSave(land);
                            ConfigMessage.get("command.plot.members.remove.success")
                                .replace("{name}", target.getName())
                                .send(player);
                        } else {
                            ConfigMessage.get("command.plot.members.remove.not-a-member")
                                .replace("{name}", target.getName())
                                .send(player);
                        }
                        return 1;
                    })
            );
    }

}

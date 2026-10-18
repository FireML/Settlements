package de.themoep.settlements.command.main;

import com.mojang.brigadier.tree.LiteralCommandNode;
import de.themoep.settlements.command.main.sub.BonusClaimsSubcommand;
import de.themoep.settlements.command.main.sub.FinishedMissionsSubcommand;
import de.themoep.settlements.command.main.sub.JoinSubcommand;
import de.themoep.settlements.command.main.sub.MissionPointsSubcommand;
import de.themoep.settlements.command.main.sub.ReloadSubcommand;
import de.themoep.settlements.command.main.sub.RerollMissionSubcommand;
import de.themoep.settlements.command.main.sub.RerollMissionsSubcommand;
import de.themoep.settlements.command.main.sub.SetLevelSubcommand;
import de.themoep.settlements.command.main.sub.SetRankSubcommand;
import de.themoep.settlements.command.main.sub.TriggerUpkeepSubcommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

public class MainCommand {

    public static @NonNull LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("settlements")
            .requires(stack -> stack.getSender().hasPermission("settlements.admin.command"))
            .then(BonusClaimsSubcommand.get())
            .then(MissionPointsSubcommand.get())
            .then(RerollMissionSubcommand.get())
            .then(RerollMissionsSubcommand.get())
            .then(JoinSubcommand.get())
            .then(SetLevelSubcommand.get())
            .then(FinishedMissionsSubcommand.get())
            .then(SetRankSubcommand.get())
            .then(ReloadSubcommand.get())
            .then(TriggerUpkeepSubcommand.get())
            .build();
    }

}

package de.themoep.settlements.commandold.admin;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.commandold.common.TargetParser;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Targetable;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class AdminMissionCommand extends SubCommand implements TargetParser {
    public AdminMissionCommand(SubCommand parent) {
        super(parent, "mission add|set|remove <s:settlement> <amount>", "finishedmissions");
        setPermission("settlements.admin.command.mission");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 3) {
            return false;
        }
        Operation operation;
        try {
            operation = Operation.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(sender, "input", args[2]);
            return false;
        }

        Targetable targetable = getTarget(sender, args[1]);
        if (targetable instanceof Settlement provider) {
            if (operation.apply(provider, amount)) {
                getMessage("success").tell(sender,
                        "operation", operation.name().toLowerCase(),
                        "amount", String.valueOf(amount),
                        "name", targetable.getName()
                );
                if (operation.equals(Operation.ADD)) {
                    Component message = Message.get("missions.awarded").getReplacedComponent(provider.getReplacements(this));
                    message = ComponentUtils.parsePlaceholders(message,
                        "finishedmissions", String.valueOf(provider.getFinishedMissions()),
                        "amount", String.valueOf(amount),
                        "state", provider.getStateTag()
                    );
                    MessageUtils.tellResidents(provider.getOnlineResidents(), message);
                }
                provider.save();
            }
        } else {
            return false;
        }

        return true;
    }

    private enum Operation {
        ADD() {
            @Override
            public boolean apply(Settlement missionProvider, int amount) {
                missionProvider.setFinishedMissions(missionProvider.getFinishedMissions() + amount);
                return true;
            }
        },
        SET() {
            @Override
            public boolean apply(Settlement missionProvider, int amount) {
                missionProvider.setFinishedMissions(amount);
                return true;
            }
        },
        REMOVE() {
            @Override
            public boolean apply(Settlement missionProvider, int amount) {
                if (missionProvider.getFinishedMissions() >= amount) {
                    missionProvider.setFinishedMissions(missionProvider.getFinishedMissions() - amount);
                    return true;
                }
                return false;
            }
        };

        public abstract boolean apply(Settlement missionProvider, int amount);
    }
}

package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.TargetParser;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.command.CommandSender;

public class AdminCooperationPointsCommand extends SubCommand implements TargetParser {
    public AdminCooperationPointsCommand(SubCommand parent) {
        super(parent, "cooperationpoints add|set|remove <s:settlement|r:resident> <amount>", "coop", "cp", "missionpoints", "mp");
        setPermission("settlements.admin.command.cooperationpoints");
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
        if (targetable instanceof Resident taker) {
            if (operation.apply(taker, amount)) {
                getMessage("success").tell(sender,
                        "operation", operation.name().toLowerCase(),
                        "amount", String.valueOf(amount),
                        "name", targetable.getName()
                );
                taker.save();
            }
        }

        return true;
    }

    private enum Operation {
        ADD() {
            @Override
            public boolean apply(Resident resident, int amount) {
                resident.setMissionPoints(resident.getMissionPoints() + amount);
                return true;
            }
        },
        SET() {
            @Override
            public boolean apply(Resident resident, int amount) {
                resident.setMissionPoints(amount);
                return true;
            }
        },
        REMOVE() {
            @Override
            public boolean apply(Resident resident, int amount) {
                if (resident.getMissionPoints() >= amount) {
                    resident.setMissionPoints(resident.getMissionPoints() - amount);
                    return true;
                }
                return false;
            }
        };

        public abstract boolean apply(Resident resident, int amount);
    }
}

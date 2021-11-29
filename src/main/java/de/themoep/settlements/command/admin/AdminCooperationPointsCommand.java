package de.themoep.settlements.command.admin;

import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import org.bukkit.command.CommandSender;

public class AdminCooperationPointsCommand extends SubCommand {
    public AdminCooperationPointsCommand(SubCommand parent) {
        super(parent, "cooperationpoints add|set|remove <empire> <amount>", "coop", "cp", "missionpoints", "mp");
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

        Settlement settlement = SettlementManager.getInstance().getFromName(args[1]);
        if (settlement != null) {
            if (operation.apply(settlement, amount)) {
                getMessage("success").tell(sender,
                        "operation", operation.name().toLowerCase(),
                        "amount", String.valueOf(amount),
                        "name", settlement.getName()
                );
                DatabaseManager.getInstance().saveSettlement(settlement);
            }
        } else {
            getError("empire-doesnt-exist").tell(sender, "name", args[1]);
        }

        return true;
    }

    private enum Operation {
        ADD() {
            @Override
            public boolean apply(Settlement settlement, int amount) {
                settlement.setMissionPoints(settlement.getMissionPoints() + amount);
                return true;
            }
        },
        SET() {
            @Override
            public boolean apply(Settlement empire, int amount) {
                empire.setMissionPoints(amount);
                return true;
            }
        },
        REMOVE() {
            @Override
            public boolean apply(Settlement empire, int amount) {
                if (empire.getMissionPoints() >= amount) {
                    empire.setMissionPoints(empire.getMissionPoints() - amount);
                    return true;
                }
                return false;
            }
        };

        public abstract boolean apply(Settlement empire, int amount);
    }
}

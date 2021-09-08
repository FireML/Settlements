package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import org.bukkit.command.CommandSender;

public class VillageAdminEmpireMissionCommand extends SubCommand {
    public VillageAdminEmpireMissionCommand(SubCommand parent) {
        super(parent, "empiremission add|set|remove <empire> <amount>", "finishedmissions");
        setPermission("kingdoms.admin.command.empiremission");
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

        Empire empire = EmpireManager.getInstance().getEmpireFromName(args[1]);
        if (empire != null) {
            if (operation.apply(empire, amount)) {
                getMessage("success").tell(sender,
                        "operation", operation.name().toLowerCase(),
                        "amount", String.valueOf(amount),
                        "name", empire.getName()
                );
                DatabaseManager.getInstance().saveEmpire(empire);
            }
        } else {
            getError("empire-doesnt-exist").tell(sender, "name", args[1]);
        }

        return true;
    }

    private enum Operation {
        ADD() {
            @Override
            public boolean apply(Empire empire, int amount) {
                empire.setFinishedMissions(empire.getFinishedMissions() + amount);
                return true;
            }
        },
        SET() {
            @Override
            public boolean apply(Empire empire, int amount) {
                empire.setFinishedMissions(amount);
                return true;
            }
        },
        REMOVE() {
            @Override
            public boolean apply(Empire empire, int amount) {
                if (empire.getFinishedMissions() >= amount) {
                    empire.setFinishedMissions(empire.getFinishedMissions() - amount);
                    return true;
                }
                return false;
            }
        };

        public abstract boolean apply(Empire empire, int amount);
    }
}

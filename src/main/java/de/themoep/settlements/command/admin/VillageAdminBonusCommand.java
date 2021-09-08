package de.themoep.settlements.command.admin;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.command.CommandSender;

public class VillageAdminBonusCommand extends SubCommand {
    public VillageAdminBonusCommand(SubCommand parent) {
        super(parent, "bonus add|set|remove <player>|s:<settlement>|e:<empire> <amount>", "bonusclaims");
        setPermission("kingdoms.admin.command.bonus");
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
        if (args[1].startsWith("village:") || args[1].startsWith("v:") || args[1].startsWith("settlement:") || args[1].startsWith("s:")) {
            String name = args[1].split(":")[1];
            Village village = VillageManager.getInstance().getFromName(name);
            if (village != null) {
                if (operation.apply(village, amount)) {
                    getMessage("success").tell(sender,
                            "operation", operation.name().toLowerCase(),
                            "amount", String.valueOf(amount),
                            "name", village.getName()
                    );
                    DatabaseManager.getInstance().saveSettlement(village);
                }
            } else {
                getError("village-doesnt-exist").tell(sender, "name", name);
            }
        } else if (args[1].startsWith("empire:") || args[1].startsWith("e:")) {
            String name = args[1].split(":")[1];
            Empire empire = EmpireManager.getInstance().getEmpireFromName(name);
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
                getError("empire-doesnt-exist").tell(sender, "name", name);
            }
        } else {
            String name = args[1];
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            String fName = name;
            Utils.runAsync(() -> {
                Resident resident = ResidentManager.getInstance().getResident(fName);
                if (resident != null) {
                    if (operation.apply(resident, amount)) {
                        getMessage("success").tell(sender,
                                "operation", operation.name().toLowerCase(),
                                "amount", String.valueOf(amount),
                                "name", resident.getName()
                        );
                        DatabaseManager.getInstance().saveResident(resident);
                    }
                } else {
                    getError("player-not-found").tell(sender, "name", fName);
                }
            });
        }

        return true;
    }

    private enum Operation {
        ADD() {
            @Override
            public boolean apply(Targetable holder, int amount) {
                holder.addBonusClaims(amount);
                return true;
            }
        },
        SET() {
            @Override
            public boolean apply(Targetable holder, int amount) {
                holder.setBonusClaims(amount);
                return true;
            }
        },
        REMOVE() {
            @Override
            public boolean apply(Targetable holder, int amount) {
                return holder.removeBonusClaims(amount);
            }
        };

        public abstract boolean apply(Targetable resident, int amount);
    }
}

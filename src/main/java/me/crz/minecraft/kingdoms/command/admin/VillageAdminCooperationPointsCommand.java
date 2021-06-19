package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.command.CommandSender;

public class VillageAdminCooperationPointsCommand extends SubCommand {
    public VillageAdminCooperationPointsCommand(SubCommand parent) {
        super(parent, "cooperationpoints add|set|remove <empire> <amount>", "coop", "cp", "missionpoints", "mp");
        setPermission("kingdoms.admin.command.cooperationpoints");
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

        Village village = VillageManager.getInstance().getVillageFromName(args[1]);
        if (village != null) {
            if (operation.apply(village, amount)) {
                getMessage("success").tell(sender,
                        "operation", operation.name().toLowerCase(),
                        "amount", String.valueOf(amount),
                        "name", village.getName()
                );
                DatabaseManager.getInstance().saveVillage(village);
            }
        } else {
            getError("empire-doesnt-exist").tell(sender, "name", args[1]);
        }

        return true;
    }

    private enum Operation {
        ADD() {
            @Override
            public boolean apply(Village village, int amount) {
                village.setMissionPoints(village.getMissionPoints() + amount);
                return true;
            }
        },
        SET() {
            @Override
            public boolean apply(Village empire, int amount) {
                empire.setMissionPoints(amount);
                return true;
            }
        },
        REMOVE() {
            @Override
            public boolean apply(Village empire, int amount) {
                if (empire.getMissionPoints() >= amount) {
                    empire.setMissionPoints(empire.getMissionPoints() - amount);
                    return true;
                }
                return false;
            }
        };

        public abstract boolean apply(Village empire, int amount);
    }
}

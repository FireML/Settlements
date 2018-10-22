package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.BonusClaimsHolder;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class VillageAdminBonusCommand extends SubCommand {
    public VillageAdminBonusCommand(SubCommand parent) {
        super(parent, "bonus add|set|remove player|village:<name> <amount>", "bonusclaims");
        setPermission("kingdoms.admin");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
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
            getError("not-a-number").tell(player, "input", args[2]);
            return false;
        }
        boolean isVillage = args[1].startsWith("village:") || args[1].startsWith("v:");
        if (isVillage) {
            String name = args[1].split(":")[1];
            Village village = VillageManager.getInstance().getVillageFromName(name);
            if (village != null) {
                if (operation.apply(village, amount)) {
                    getMessage("success").tell(player,
                            "operation", operation.name().toLowerCase(),
                            "amount", String.valueOf(amount),
                            "name", village.getName()
                    );
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
                }
            } else {
                getError("village-doesnt-exist").tell(player, "name", name);
            }
        } else {
            String name = args[1];
            if (name.contains(":")) {
                name = name.split(":")[2];
            }
            String fName = name;
            Utils.runAsync(() -> {
                Resident resident = ResidentManager.getInstance().getResident(fName);
                if (resident != null) {
                    if (operation.apply(resident, amount)) {
                        getMessage("success").tell(player,
                                "operation", operation.name().toLowerCase(),
                                "amount", String.valueOf(amount),
                                "name", resident.getName()
                        );
                        DatabaseManager.getInstance().saveResident(resident);
                    }
                } else {
                    getError("player-not-found").tell(player, "name", fName);
                }
            });
        }

        return true;
    }

    private enum Operation {
        ADD() {
            @Override
            public boolean apply(BonusClaimsHolder holder, int amount) {
                holder.addBonusClaims(amount);
                return true;
            }
        },
        SET() {
            @Override
            public boolean apply(BonusClaimsHolder holder, int amount) {
                holder.setBonusClaims(amount);
                return true;
            }
        },
        REMOVE() {
            @Override
            public boolean apply(BonusClaimsHolder holder, int amount) {
                return holder.removeBonusClaims(amount);
            }
        };

        public abstract boolean apply(BonusClaimsHolder resident, int amount);
    }
}

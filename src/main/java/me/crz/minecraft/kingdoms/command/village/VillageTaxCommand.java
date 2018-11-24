package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageTaxCommand extends VillageSubCommand {
    public VillageTaxCommand(SubCommand parent) {
        super(parent, "tax [set <amount>]", VillageRank.RESIDENT);
        registerCommand(new VillageSubCommand(this, "set <amount>", VillageRank.CHIEF) {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    return false;
                }
                try {
                    long amount = Long.parseLong(args[0]);
                    resident.getVillage().setTax(amount);
                    getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                            "state", resident.getVillage().getStateTag(),
                            "amount", String.valueOf(amount)
                    );
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(resident, "input", args[0]);
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            getMessage("current").tell(resident,
                    "state", resident.getVillage().getStateTag(),
                    "amount", String.valueOf(resident.getVillage().getTax())
            );
            return true;
        }
        return false;
    }
}

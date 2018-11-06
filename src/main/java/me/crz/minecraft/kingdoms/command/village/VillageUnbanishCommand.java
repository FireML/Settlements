package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

public class VillageUnbanishCommand extends VillageSubCommand {

    public VillageUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <name>", VillageRank.CHIEF);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident res = ResidentManager.getInstance().getResident(args[0]);
            if (res == null) {
                resident.tell(getError("player-not-found"), "name", args[0]);
                return;
            }
            if (!resident.getVillage().removeBanishedResident(res)) {
                resident.tell(getError("not-banished"), "name", res.getName());
                return;
            }
            DatabaseManager.getInstance().saveVillage(resident.getVillage());
            MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("unbanished"), "name", res.getName());
        });
        return true;
    }
}

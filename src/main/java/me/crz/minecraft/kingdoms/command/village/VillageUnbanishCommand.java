package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

public class VillageUnbanishCommand extends VillageSubCommand {

    public VillageUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <name>");
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
            resident.getVillage().removeBanishedResident(res);
            DatabaseManager.getInstance().saveVillage(resident.getVillage());
            MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("unbanished"), "player", res.getName());
        });
        return true;
    }
}

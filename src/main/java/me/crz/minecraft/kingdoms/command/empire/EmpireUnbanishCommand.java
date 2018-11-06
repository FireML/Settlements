package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class EmpireUnbanishCommand extends EmpireSubCommand {

    public EmpireUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <player>|[player|village|empire]:<name>", EmpireRank.LEADER);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Targetable target;
            if (args[0].startsWith("empire:") || args[0].startsWith("e:")) {
                target = EmpireManager.getInstance().getEmpireFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("empire-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return;
                }
            } else if (args[0].startsWith("village:") || args[0].startsWith("v:") || args[0].startsWith("settlement:") || args[0].startsWith("s:")) {
                target = VillageManager.getInstance().getVillageFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("village-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return;
                }
            } else {
                String name = args[0];
                if (name.contains(":")) {
                    name = name.split(":")[1];
                }
                target = ResidentManager.getInstance().getResident(name);
                if (target == null) {
                    getError("player-not-found").tell(resident, "name", name);
                    return;
                }
            }

            if (!resident.getVillage().getEmpire().removeBanished(target)) {
                resident.tell(getError("not-banished"), "name", target.getName());
                return;
            }

            MessageUtils.tellResidents(resident.getVillage().getEmpire().getOnlineResidents(), getMessage("unbanished"), "target", target.getName());
            DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
        });
        return true;
    }
}

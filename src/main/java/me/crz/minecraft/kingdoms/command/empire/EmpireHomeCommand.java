package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireHomeCommand extends ResidentSubCommand {
    public EmpireHomeCommand(SubCommand parent) {
        super(parent, "home [<name>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Empire empire;
        if (args.length < 1) {
            if (!resident.hasEmpire()) {
                getError("empire-required").tell(resident);
                return false;
            } else if (resident.getVillage().getEmpire().getHome() == null) {
                getMessage("own-no-home").tell(resident, "chief", EmpireManager.getInstance().getLeaderRank().getTag());
                return true;
            } else {
                empire = resident.getVillage().getEmpire();
            }
        } else {
            String name = String.join(" ", args);
            empire = EmpireManager.getInstance().getEmpireFromName(name);
            if (empire == null) {
                getError("empire-doesnt-exist").tell(resident, "name", name);
                return true;
            }
        }

        if (!empire.canAccess(resident)) {
            getMessage("no-access").tell(resident, "state", empire.getStateTag());
            return true;
        }

        if (empire.getHome() != null) {
            Utils.teleport(resident.getPlayer(), empire.getHome().toBukkitLocation(), empire.getStateKey(), getConfigPath(),
                    "state", empire.getStateKey(), "name", empire.getName());
        } else {
            getMessage("no-home").tell(resident, "state", empire.getStateTag());
        }
        return true;
    }
}

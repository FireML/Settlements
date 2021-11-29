package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

import java.util.regex.Pattern;

public class EmpireCreateCommand extends VillageSubCommand {
    private Pattern allowed = Pattern.compile("[\\w-]+");

    public EmpireCreateCommand(SubCommand parent) {
        super(parent, "create <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getSettlement().isMaxLevel()) {
            getError("max-level-kingdom-required").tell(resident,
                    "state", resident.getSettlement().getState().getTag(),
                    "level", String.valueOf(resident.getSettlement().getLevel())
            );
            return true;
        }
        if (args.length < 1) {
            return false;
        }
        String name = Utils.filterAllCurses(String.join(" ", args).trim());
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(resident, "input", name);
        } else if (empireExists(name)) {
            getError("empire-exists").tell(resident, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(resident, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(resident, "input", name);
        } else {
            if (Config.Empire.CREATION_COST != 0) {
                if (!resident.getSettlement().removeFromBank(Config.Empire.CREATION_COST)) {
                    getError("not-enough-money").tell(resident, "amount", String.valueOf(Config.Empire.CREATION_COST));
                    return true;
                }
            }
            Empire empire = new Empire(name, resident.getSettlement().getId(), 1);

            MessageUtils.broadcast(getMessage("broadcast"),
                    "name", empire.getName(),
                    "capital", resident.getSettlement().getName(),
                    "capitalstate", resident.getSettlement().getState().getTag(),
                    "player", resident.getName());
            EmpireManager.getInstance().registerNewEmpire(empire);
            getMessage("success").tell(resident, "name", empire.getName());
        }
        return true;
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (resident.hasEmpire()) {
            getError("no-empire-required").tell(resident);
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return !resident.hasEmpire() && super.hasAccess(resident);
    }

    private boolean empireExists(String name) {
        return EmpireManager.getInstance().getEmpireFromName(name) != null;
    }
}

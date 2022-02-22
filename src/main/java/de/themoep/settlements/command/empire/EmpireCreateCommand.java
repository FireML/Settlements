package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.settlement.SettlementSubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

import java.util.regex.Pattern;

public class EmpireCreateCommand extends SettlementSubCommand {
    private final Pattern allowed;

    public EmpireCreateCommand(SubCommand parent) {
        super(parent, "create <name>");
        allowed = Pattern.compile(Config.Empire.NAME_VALIDATION);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getSettlement().hasUnlocked("empire.create")) {
            getError("not-unlocked").tell(resident,
                    "state", resident.getSettlement().getStateTag(),
                    "name", resident.getSettlement().getName(),
                    "function", "Empire"
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
        } else if (name.length() > Config.Empire.NAME_LENGTH) {
            getError("too-long").tell(resident, "input", name, "amount", String.valueOf(Config.Empire.NAME_LENGTH));
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
            EmpireManager.getInstance().registerNewEmpire(resident, empire);
            getMessage("success").tell(resident,
                    "name", empire.getName(),
                    "cost", Utils.CURRENCY_FORMAT.format(Config.Empire.CREATION_COST),
                    "capital", resident.getSettlement().getName(),
                    "capitalstate", resident.getSettlement().getState().getTag(),
                    "player", resident.getName());
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

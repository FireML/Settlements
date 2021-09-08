package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

import java.util.regex.Pattern;

public class EmpireRenameCommand extends EmpireSubCommand {
    private Pattern allowed = Pattern.compile("[\\w-]+");

    public EmpireRenameCommand(SubCommand parent) {
        super(parent, "rename <name>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getVillage().getEmpire();
        if (args.length == 0) {
            return false;
        }
        String name = Utils.filterAllCurses(String.join(" ", args).trim());
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(res, "input", name);
        } else if (empireExists(name)) {
            getError("empire-exists").tell(res, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(res, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(res, "input", name);
        } else if (empire.isInRegen()) {
            getError("rename-in-progress").tell(res);
        } else if (empire.getLastRename() + Config.Empire.RENAME_COOLDOWN * 1000 > System.currentTimeMillis()) {
            getError("rename-on-cooldown").tell(res, "cooldown", String.valueOf(Config.Empire.RENAME_COOLDOWN));
        } else if (EmpireManager.getInstance().changeName(empire, name)){
            getMessage("success").tell(empire.getOnlineResidents(), "name", empire.getName());
        } else {
            getError("cant-rename").tell(res);
        }
        return true;
    }

    private boolean empireExists(String name) {
        return EmpireManager.getInstance().getEmpireFromName(name) != null;
    }
}

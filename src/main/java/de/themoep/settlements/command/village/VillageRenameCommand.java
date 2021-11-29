package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.Utils;

import java.util.regex.Pattern;

public class VillageRenameCommand extends VillageSubCommand {
    private Pattern allowed = Pattern.compile("[\\w-]+");

    public VillageRenameCommand(SubCommand parent) {
        super(parent, "rename <name>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Settlement settlement = res.getSettlement();
        if (args.length == 0) {
            return false;
        }
        String name = Utils.filterAllCurses(String.join(" ", args).trim());
        if (name.toLowerCase().contains("claims")) {
            getError("invalid-characters").tell(res, "input", name);
        } else if (villageExists(name)) {
            getError("settlement-exists").tell(res, "name", name);
        } else if (name.length() > 20) {
            getError("too-long").tell(res, "input", name, "amount", String.valueOf(20));
        } else if (!allowed.matcher(name).matches()) {
            getError("invalid-characters").tell(res, "input", name);
        } else {
            SettlementManager.getInstance().changeName(settlement, name);
            getMessage("success").tell(settlement.getOnlineResidents(), "name", settlement.getName(), "state", settlement.getState().getTag());
        }
        return true;
    }

    private boolean villageExists(String name) {
        return SettlementManager.getInstance().getFromName(name) != null;
    }
}

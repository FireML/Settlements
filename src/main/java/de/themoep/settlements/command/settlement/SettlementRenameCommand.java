package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

import java.util.regex.Pattern;

public class SettlementRenameCommand extends SettlementSubCommand {

    public SettlementRenameCommand(SubCommand parent) {
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
        } else if (SettlementManager.getInstance().getFromName(name) != null) {
            getError("settlement-exists").tell(res, "name", name);
        } else if (name.length() > Config.Settlement.NAME_LENGTH) {
            getError("too-long").tell(res, "input", name, "amount", String.valueOf(Config.Settlement.NAME_LENGTH));
        } else if (!settlement.isValidName(name)) {
            getError("invalid-characters").tell(res, "input", name);
        } else {
            SettlementManager.getInstance().changeName(settlement, name);
            getMessage("success").tell(settlement.getOnlineResidents(), "name", settlement.getName(), "state", settlement.getState().getTag());
        }
        return true;
    }
}

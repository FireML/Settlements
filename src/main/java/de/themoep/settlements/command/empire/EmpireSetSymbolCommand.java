package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;

public class EmpireSetSymbolCommand extends EmpireSubCommand {
    public EmpireSetSymbolCommand(SubCommand parent) {
        super(parent, "setsymbol <symbol>", "symbol");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        String symbol = "";
        if (args.length > 0) {
            symbol = args[0];
            if (symbol.length() > 1) {
                getError("too-long").tell(resident, "input", symbol, "amount", "1");
                return false;
            }
        }
        resident.getSettlement().getEmpire().setSymbol(symbol);
        getMessage("success").tell(resident.getSettlement().getEmpire().getOnlineResidents(), "symbol", symbol);
        DatabaseManager.getInstance().saveEmpire(resident.getSettlement().getEmpire());
        return true;
    }
}

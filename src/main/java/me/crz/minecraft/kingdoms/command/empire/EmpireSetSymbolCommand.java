package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;

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
        resident.getVillage().getEmpire().setSymbol(symbol);
        getMessage("success").tell(resident.getVillage().getEmpire().getOnlineResidents(), "symbol", symbol);
        DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
        return true;
    }
}

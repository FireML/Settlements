package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

public class EmpireUpkeepCommand extends EmpireSubCommand {
    public EmpireUpkeepCommand(SubCommand parent) {
        super(parent, "upkeep");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getSettlement().getEmpire();
        String afford;
        long upkeepAmount = empire.getUpkeep() - empire.getTaxIncome();
        if (upkeepAmount > 0) {
            long upkeepDays = empire.getBank() / upkeepAmount;
            afford = upkeepDays > 0 ? String.valueOf(upkeepDays) : ChatColor.RED + String.valueOf(upkeepDays);
        } else {
            afford = getText("afford-infinite");
        }
        res.tell(getMessage("info"),
                "cost", Utils.CURRENCY_FORMAT.format(empire.getUpkeep()),
                "time", UtilTime.format((double) TimeUnit.MILLISECONDS.toSeconds(Utils.timeTillNextUpkeep())),
                "afford", afford
        );
        return true;
    }
}

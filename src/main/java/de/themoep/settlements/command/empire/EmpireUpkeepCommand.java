package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.resident.Resident;
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
        int globalTotal = empire.getLand().size();
        long globalCost = empire.getUpkeep();
        int overworldTotal = empire.getLand().stream().filter(ownedLand -> ownedLand.getLocation().getWorld().equals("Kingdoms")).toList().size();
        long overworldCost = empire.getClaimUpkeep("Kingdoms") * overworldTotal;
        int netherTotal = empire.getLand().stream().filter(ownedLand -> ownedLand.getLocation().getWorld().equals("Kingdoms_nether")).toList().size();
        long netherCost = empire.getClaimUpkeep("Kingdoms_nether") * netherTotal;
        int endTotal = empire.getLand().stream().filter(ownedLand -> ownedLand.getLocation().getWorld().equals("Kingdoms_the_end")).toList().size();
        long endCost = empire.getClaimUpkeep("Kingdoms_the_end") * endTotal;
        String afford;
        long upkeepAmount = globalCost - empire.getTaxIncome();
        if (upkeepAmount > 0) {
            long upkeepDays = empire.getBank() / upkeepAmount;
            afford = upkeepDays > 0 ? String.valueOf(upkeepDays) : ChatColor.RED + String.valueOf(upkeepDays);
        } else {
            afford = getText("afford-infinite");
        }

        res.tell(getMessage("info"),
                "global_cost", Utils.CURRENCY_FORMAT.format(globalCost),
                "time", UtilTime.format((double) TimeUnit.MILLISECONDS.toSeconds(Utils.timeTillNextUpkeep())),
                "afford", afford,
                "global_total", String.valueOf(globalTotal),
                "overworld_cost", String.valueOf(overworldCost),
                "overworld_total", String.valueOf(overworldTotal),
                "nether_total", String.valueOf(netherTotal),
                "nether_cost", String.valueOf(netherCost),
                "end_total", String.valueOf(endTotal),
                "end_cost", String.valueOf(endCost)
        );
        return true;
    }
}

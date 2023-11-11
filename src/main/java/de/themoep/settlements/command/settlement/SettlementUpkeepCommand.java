package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;

import java.util.concurrent.TimeUnit;

public class SettlementUpkeepCommand extends SettlementSubCommand {
    public SettlementUpkeepCommand(SubCommand parent) {
        super(parent, "upkeep");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Settlement settlement = res.getSettlement();
        int globalTotal = settlement.getLand().size();
        long globalCost = settlement.getUpkeep();
        int overworldTotal = settlement.getLand().stream().filter(ownedLand -> ownedLand.getLocation().getWorld().equals("Kingdoms")).toList().size();
        long overworldCost = settlement.getClaimUpkeep("Kingdoms") * overworldTotal;
        int netherTotal = settlement.getLand().stream().filter(ownedLand -> ownedLand.getLocation().getWorld().equals("Kingdoms_nether")).toList().size();
        long netherCost = settlement.getClaimUpkeep("Kingdoms_nether") * netherTotal;
        int endTotal = settlement.getLand().stream().filter(ownedLand -> ownedLand.getLocation().getWorld().equals("Kingdoms_the_end")).toList().size();
        long endCost = settlement.getClaimUpkeep("Kingdoms_the_end") * endTotal;
        String afford;
        long upkeepAmount = globalCost - settlement.getTaxIncome();
        if (upkeepAmount > 0) {
            long upkeepDays = settlement.getBank() / upkeepAmount;
            afford = upkeepDays > 0 ? String.valueOf(upkeepDays) : "§c" + upkeepDays;
        } else {
            afford = getText("afford-infinite");
        }

        res.tell(getMessage("info").getText(),
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

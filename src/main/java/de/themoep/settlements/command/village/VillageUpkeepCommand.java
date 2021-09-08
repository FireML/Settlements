package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

public class VillageUpkeepCommand extends VillageSubCommand {
    public VillageUpkeepCommand(SubCommand parent) {
        super(parent, "upkeep");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village village = res.getVillage();
        long total = village.getUpkeep();
        String afford;
        long upkeepAmount = total - village.getTaxIncome();
        if (upkeepAmount > 0) {
            long upkeepDays = village.getBank() / upkeepAmount;
            afford = upkeepDays > 0 ? String.valueOf(upkeepDays) : ChatColor.RED + String.valueOf(upkeepDays);
        } else {
            afford = getText("afford-infinite");
        }

        res.tell(getMessage("info"),
                "cost", Utils.CURRENCY_FORMAT.format(total),
                "upkeep", Utils.CURRENCY_FORMAT.format(total <= 0 ? 0 : village.getClaimUpkeep(res.getPlayer().getWorld().getName())),
                "time", UtilTime.format((double) TimeUnit.MILLISECONDS.toSeconds(Utils.timeTillNextUpkeep())),
                "afford", afford
        );
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

public class EmpireUpkeepCommand extends EmpireSubCommand {
    public EmpireUpkeepCommand(SubCommand parent) {
        super(parent, "upkeep");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getVillage().getEmpire();
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

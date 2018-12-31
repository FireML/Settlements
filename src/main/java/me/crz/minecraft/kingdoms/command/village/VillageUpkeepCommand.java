package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

public class VillageUpkeepCommand extends VillageSubCommand {
    public VillageUpkeepCommand(SubCommand parent) {
        super(parent, "upkeep");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village village = res.getVillage();
        int total = village.getUpkeep();

        res.tell(getMessage("info"),
                "cost", Utils.CURRENCY_FORMAT.format(total),
                "upkeep", Utils.CURRENCY_FORMAT.format(Utils.getUpkeepCost()),
                "time", UtilTime.format((double) TimeUnit.MILLISECONDS.toSeconds(Utils.timeTillNextUpkeep())),
                "afford", village.has(total) ? ChatColor.GREEN + "yes" : ChatColor.RED + "no"
        );
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

public class EmpireUpkeepCommand extends EmpireSubCommand {
    public EmpireUpkeepCommand(SubCommand parent) {
        super(parent, "upkeep", EmpireRank.NONE);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getVillage().getEmpire();
        res.tell(getMessage("info"),
                "cost", String.valueOf(empire.getUpkeep()),
                "time", UtilTime.format((double) TimeUnit.MILLISECONDS.toSeconds(Utils.timeTillNextUpkeep())),
                "afford", empire.has(empire.getUpkeep()) ? ChatColor.GREEN + "yes" : ChatColor.RED + "no"
        );
        return true;
    }
}

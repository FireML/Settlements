package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.concurrent.TimeUnit;

import me.crz.minecraft.kingdoms.land.LandManager;

import me.crz.minecraft.kingdoms.command.SubCommand;
import org.bukkit.ChatColor;

public class VillageUpkeepCommand extends VillageSubCommand {
    public VillageUpkeepCommand(SubCommand parent) {
        super(parent, "upkeep", VillageRank.RESIDENT);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village village = res.getVillage();
        int total = LandManager.getInstance().getClaimedLandSize(village) * 500;

        res.tell(getMessage("info"),
                "cost", String.valueOf(total),
                "upkeep", String.valueOf(Utils.getUpkeepCost()),
                "time", UtilTime.format((double) TimeUnit.MILLISECONDS.toSeconds(Utils.timeTillNextUpkeep())),
                "afford", (village.getBank() >= total) ? ChatColor.GREEN + "yes" : ChatColor.RED + "no"
        );
        return true;
    }
}

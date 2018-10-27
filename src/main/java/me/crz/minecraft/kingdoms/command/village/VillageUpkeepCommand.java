package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.concurrent.TimeUnit;

import me.crz.minecraft.kingdoms.land.LandManager;

import java.util.Calendar;

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
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 12);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 1);
        midnight.set(Calendar.DAY_OF_YEAR, midnight.get(Calendar.DAY_OF_YEAR) + 1);
        long tillNoon = midnight.getTimeInMillis() - System.currentTimeMillis() - 1L;
        int total = LandManager.getInstance().getClaimedLandSize(village) * 500;

        res.tell(getMessage("info"),
                "cost", String.valueOf(total),
                "upkeep", String.valueOf(KingdomsPlugin.UPKEEP),
                "time", village.getLastUpkeep() < System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L)
                        ? UtilTime.format((double) TimeUnit.MILLISECONDS.toSeconds(tillNoon))
                        : UtilTime.format((double) TimeUnit.MILLISECONDS.toSeconds(village.getLastUpkeep() - System.currentTimeMillis())),
                "afford", (village.getBank() >= total) ? ChatColor.GREEN + "yes" : ChatColor.RED + "no"
        );
        return true;
    }
}

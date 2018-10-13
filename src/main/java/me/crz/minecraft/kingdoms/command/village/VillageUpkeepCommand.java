package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.UtilMath;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.concurrent.TimeUnit;

import me.crz.minecraft.kingdoms.land.LandManager;

import java.util.Calendar;

import me.crz.minecraft.kingdoms.command.SubCommand;

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
                        ? format((double) TimeUnit.MILLISECONDS.toSeconds(tillNoon))
                        : format((double) TimeUnit.MILLISECONDS.toSeconds(village.getLastUpkeep() - System.currentTimeMillis())),
                "afford", (village.getBank() >= total) ? "&ayes" : "&cno"
        );
        return true;
    }

    public static String format(double seconds) {
        if (seconds < 0.0) {
            seconds = 0.0;
        }
        double minutes = seconds / 60.0;
        double hours = minutes / 60.0;
        double days = hours / 24.0;
        double months = days / 30.0;
        double years = days / 365.0;
        if (minutes < 1.0) {
            return String.valueOf(UtilMath.trim(seconds)) + "&7 Seconds";
        }
        if (hours < 1.0) {
            return String.valueOf(UtilMath.trim(minutes)) + "&7 Minutes";
        }
        if (days < 1.0) {
            return String.valueOf(UtilMath.trim(hours)) + "&7 Hours";
        }
        if (months < 1.0) {
            return String.valueOf(UtilMath.trim(days)) + "&7 Days";
        }
        if (years < 1.0) {
            return String.valueOf(UtilMath.trim(months)) + "&7 Months";
        }
        return String.valueOf(UtilMath.trim(years)) + "&7 Years";
    }
}

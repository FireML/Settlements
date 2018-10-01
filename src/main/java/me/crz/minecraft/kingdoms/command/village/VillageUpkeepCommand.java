package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.UtilMath;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.concurrent.TimeUnit;

import me.crz.minecraft.kingdoms.land.LandManager;

import java.util.Calendar;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageUpkeepCommand extends SubCommand {
    public VillageUpkeepCommand() {
        super("upkeep", "upkeep");
        this.setPermission("kingdoms.user");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("test") && player.isOp()) {
            village.payUpkeep();
            return;
        }
        final Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 1);
        midnight.set(Calendar.DAY_OF_YEAR, midnight.get(Calendar.DAY_OF_YEAR) + 1);
        final long tillMidnight = midnight.getTimeInMillis() - System.currentTimeMillis() - 1L;
        final StringBuilder sb = new StringBuilder("&7------- &cUpkeep&7 -------\n");
        final int total = LandManager.getInstance().getClaimedLandSize(village) * 500;
        sb.append("&3&lUpkeep cost:&e ").append(total).append("$\n");
        sb.append("&3&lUpkeep: &e500$ &7/&e claim &7/&e day\n");
        if (village.getLastUpkeep() < System.nanoTime() + TimeUnit.DAYS.toNanos(1L)) {
            sb.append("&3&lTime Till Upkeep: &e").append(format((double) TimeUnit.MILLISECONDS.toSeconds(tillMidnight)));
        } else {
            sb.append("&3&lTime Till Upkeep: &e").append(format((double) TimeUnit.NANOSECONDS.toSeconds(village.getLastUpkeep() - System.nanoTime())));
        }
        sb.append("\n&3&lCan afford? ").append((village.getBank() >= total) ? "&ayes" : "&cno");
        MessageUtils.tell(player, sb.toString());
    }

    public static String format(double seconds) {
        if (seconds < 0.0) {
            seconds = 0.0;
        }
        final double minutes = seconds / 60.0;
        final double hours = minutes / 60.0;
        final double days = hours / 24.0;
        final double months = days / 30.0;
        final double years = days / 365.0;
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

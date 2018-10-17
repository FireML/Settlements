package me.crz.minecraft.kingdoms;


import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardPolicy;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.List;

public class ScoreBoard implements ScoreboardPolicy {
    private final ScoreboardManager manager;

    public ScoreBoard(ScoreboardManager manager) {
        super();
        this.manager = manager;
    }

    public static String withSuffix(long count) {
        if (count < 1000L) {
            return String.valueOf(count);
        }
        int exp = (int) (Math.log(count) / Math.log(1000.0));
        return String.format("%.1f%c", count / Math.pow(1000.0, exp), "kMBTPE".charAt(exp - 1));
    }

    @Override
    public List<String> getSidebar(Player member) {
        Resident resident = ResidentManager.getInstance().getResident(member);

        return manager.getMessage("sidebar.entries." + (resident.hasVillage() ? "village" : "no-village")).getReplacedTextList(
                "balance", withSuffix((long) KingdomsPlugin.getInstance().econ.getBalance(member)),
                "kills", String.valueOf(member.getStatistic(Statistic.PLAYER_KILLS)),
                "deaths", String.valueOf(member.getStatistic(Statistic.DEATHS)),
                "villagename", resident.getVillageName(),
                "villagestate", resident.hasVillage() ? resident.getVillage().getState().getTag() : "",
                "villagerank", resident.getVillageRank().getTag(resident.getVillage())
        );
    }

    public String c(String input) {
        return MessageUtils.replaceColorCodes(input);
    }

    @Override
    public String getPrefix(Player perspective, Player subject) {
        if (subject.isOp() && manager.hasRainbowTag(subject)) {
            return String.valueOf(c(KingdomsPlugin.chat.getPlayerPrefix(subject).toLowerCase()))
                    + ChatColor.RESET + manager.getNextRainbowColor(subject) + " ";
        }
        return this.c(KingdomsPlugin.chat.getPlayerPrefix(subject));
    }

    @Override
    public String getSuffix(Player perspective, Player subject) {
        return null;
    }

    @Override
    public String getUndername(Player member) {
        return null;
    }

    @Override
    public int getUndernameScore(Player perspective, Player subject) {
        return 0;
    }

    @Override
    public String getTablist(Player member) {
        return null;
    }

    @Override
    public int getTablistScore(Player perspective, Player subject) {
        return 0;
    }
}

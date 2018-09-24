package me.crz.minecraft.kingdoms;

import java.util.Random;
import org.bukkit.ChatColor;
import me.crz.minecraft.kingdoms.scoreboard.commands.CommandRainbowtag;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Statistic;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.VillageManager;
import java.util.List;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardPolicy;

public class ScoreBoard implements ScoreboardPolicy
{
    public ScoreBoard() {
        super();
    }
    
    public static String withSuffix(final long count) {
        if (count < 1000L) {
            return new StringBuilder().append(count).toString();
        }
        final int exp = (int)(Math.log(count) / Math.log(1000.0));
        return String.format("%.1f%c", count / Math.pow(1000.0, exp), "kMBTPE".charAt(exp - 1));
    }
    
    @Override
    public List<String> getSidebar(final Player member) {
        final Village k = VillageManager.getInstance().getVillageFromPlayer(member);
        if (k == null) {
            return new ArrayList<String>(Arrays.<String>asList(String.valueOf(this.c("&3&lBal&3: ")) + this.c("&b$&e" + withSuffix((long)KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(member.getUniqueId())))), "", String.valueOf(this.c("&3&lKills&3: &e")) + this.c(new StringBuilder(String.valueOf(member.getStatistic(Statistic.PLAYER_KILLS))).toString()), String.valueOf(this.c("&3&lDeaths&3: &e")) + this.c(new StringBuilder(String.valueOf(member.getStatistic(Statistic.DEATHS))).toString())));
        }
        return new ArrayList<String>(Arrays.<String>asList(String.valueOf(this.c("&3&lBal&3: ")) + this.c("&b$&e" + withSuffix((long)KingdomsPlugin.getInstance().econ.getBalance(Bukkit.getOfflinePlayer(member.getUniqueId())))), String.valueOf(this.c("&3&lBank: ")) + this.c("&b$&e" + withSuffix((long)k.getBank())), "", String.valueOf(this.c("&3&lKills&3: &e")) + this.c(new StringBuilder(String.valueOf(member.getStatistic(Statistic.PLAYER_KILLS))).toString()), String.valueOf(this.c("&3&lDeaths&3: &e")) + this.c(new StringBuilder(String.valueOf(member.getStatistic(Statistic.DEATHS))).toString())));
    }
    
    public String c(final String input) {
        return MessageUtils.replaceColorCodes(input);
    }
    
    @Override
    public String getPrefix(final Player perspective, final Player subject) {
        if (subject.isOp() && CommandRainbowtag.getInstance().isEnabled(subject.getUniqueId())) {
            return String.valueOf(this.c(KingdomsPlugin.chat.getPlayerPrefix(subject).toLowerCase())) + ChatColor.RESET.toString() + "�" + CommandRainbowtag.getInstance().getColours()[new Random().nextInt(CommandRainbowtag.getInstance().getColours().length)] + " ";
        }
        return this.c(KingdomsPlugin.chat.getPlayerPrefix(subject));
    }
    
    @Override
    public String getSuffix(final Player perspective, final Player subject) {
        return null;
    }
    
    @Override
    public String getUndername(final Player member) {
        return null;
    }
    
    @Override
    public int getUndernameScore(final Player perspective, final Player subject) {
        return 0;
    }
    
    @Override
    public String getTablist(final Player member) {
        return null;
    }
    
    @Override
    public int getTablistScore(final Player perspective, final Player subject) {
        return 0;
    }
}

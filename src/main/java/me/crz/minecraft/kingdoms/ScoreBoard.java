package me.crz.minecraft.kingdoms;


import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardPolicy;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ScoreBoard implements ScoreboardPolicy {
    private final ScoreboardManager manager;

    public ScoreBoard(ScoreboardManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public List<String> getSidebar(Player member) {
        if (member == null || !member.isOnline()) {
            return null;
        }
        Resident resident = ResidentManager.getInstance().getResidentIfPreset(member.getUniqueId());
        if (resident == null || !resident.showSidebar()) {
            return null;
        }

        Map<String, String> replacements = Utils.toMap(
                "balance", Utils.withSuffix((long) KingdomsPlugin.getInstance().econ.getBalance(member)),
                "bank", Utils.CURRENCY_FORMAT.format((long) KingdomsPlugin.getInstance().econ.getBalance(member)),
                "kills", String.valueOf(member.getStatistic(Statistic.PLAYER_KILLS)),
                "deaths", String.valueOf(member.getStatistic(Statistic.DEATHS)),
                "bonus", String.valueOf(resident.getBonusClaims()),
                "villagetitle", resident.getTitle()
        );
        if (resident.hasVillage()) {
            String[] villageRepl = resident.getVillage().getReplacements(null);
            for (int i = 0; i+1 < villageRepl.length; i+=2) {
                replacements.put("village" + villageRepl[i], villageRepl[i+1]);
            }
            if (resident.hasEmpire()) {
                String[] empireRepl = resident.getVillage().getEmpire().getReplacements(null);
                for (int i = 0; i+1 < empireRepl.length; i+=2) {
                    replacements.put("empire" + empireRepl[i], empireRepl[i+1]);
                }
            }
        }

        return manager.getMessage("sidebar.entries." + (resident.hasVillage() ? resident.hasEmpire() ? "empire" : "village" : "no-village")).getReplacedTextList(Utils.toArray(replacements));
    }

    public String c(String input) {
        return MessageUtils.replaceColorCodes(input);
    }

    @Override
    public String getPrefix(Player perspective, Player subject) {
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

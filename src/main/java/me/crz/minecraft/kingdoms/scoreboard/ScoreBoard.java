package me.crz.minecraft.kingdoms.scoreboard;


import co.aikar.timings.lib.MCTiming;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.integration.PlaceholderApiIntegration;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ScoreBoard {
    private final ScoreboardManager manager;

    public ScoreBoard(ScoreboardManager manager) {
        super();
        this.manager = manager;
    }

    public List<String> getSidebar(Player member) {
        if (member == null || !member.isOnline()) {
            return null;
        }
        Resident resident = ResidentManager.getInstance().getResidentIfPreset(member.getUniqueId());
        if (resident == null || !resident.showSidebar()) {
            return null;
        }

        Map<String, String> replacements;
        try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Scoreboard get player sidebar replacements")) {
            replacements = Utils.toMap(
                    "balance", Utils.withSuffix((long) KingdomsPlugin.getInstance().getEconomy().getBalance(member)),
                    "bank", Utils.CURRENCY_FORMAT.format((long) KingdomsPlugin.getInstance().getEconomy().getBalance(member)),
                    "kills", String.valueOf(member.getStatistic(Statistic.PLAYER_KILLS)),
                    "deaths", String.valueOf(member.getStatistic(Statistic.DEATHS)),
                    "bonus", String.valueOf(resident.getBonusClaims()),
                    "title", resident.getDisplayTitle(),
                    "villagetitle", resident.getVillageTitle(),
                    "villagerank", resident.hasVillage() ? resident.getVillageRank().getTag(resident.getVillage()) : "",
                    "empiretitle", resident.getEmpireTitle(),
                    "empirerank", resident.hasEmpire() ? resident.getEmpireRank().getTag() : ""
            );
            if (resident.hasVillage()) {
                String[] villageRepl = resident.getVillage().getReplacements(null);
                for (int i = 0; i + 1 < villageRepl.length; i += 2) {
                    replacements.put("village" + villageRepl[i], villageRepl[i + 1]);
                }
                if (resident.hasEmpire()) {
                    String[] empireRepl = resident.getVillage().getEmpire().getReplacements(null);
                    for (int i = 0; i + 1 < empireRepl.length; i += 2) {
                        replacements.put("empire" + empireRepl[i], empireRepl[i + 1]);
                    }
                }
            }
        }

        Message sidebar;
        try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Scoreboard get player sidebar message")) {
            sidebar = manager.getMessage("sidebar.entries." + (resident.hasVillage() ? resident.hasEmpire() ? "empire" : "village" : "no-village"));
        }
        List<String> text;
        try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Scoreboard get player sidebar replaced text")) {
            text = sidebar.getReplacedTextList(replacements);
        }
        if (KingdomsPlugin.getInstance().getPlaceholderApi() != null && Config.Sidebar.PLACEHOLDER_API) {
            try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Scoreboard get player sidebar placeholder api replacements")) {
                String[] replacedText = PlaceholderApiIntegration.setPlaceholders(member, String.join("\n", text)).split("\n");
                text = new ArrayList<>();
                Collections.addAll(text, replacedText);
            }
        }
        return text;
    }

    public String c(String input) {
        return MessageUtils.replaceColorCodes(input);
    }
}

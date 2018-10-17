package me.crz.minecraft.kingdoms.scoreboard;

import java.util.List;

import org.bukkit.entity.Player;

public interface ScoreboardPolicy {
    List<String> getSidebar(Player player);

    String getPrefix(Player player, Player relative);

    String getSuffix(Player player, Player relative);

    String getUndername(Player player);

    int getUndernameScore(Player player, Player relative);

    String getTablist(Player player);

    int getTablistScore(Player player, Player relative);
}

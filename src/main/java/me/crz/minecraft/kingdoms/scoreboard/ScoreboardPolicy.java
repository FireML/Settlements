package me.crz.minecraft.kingdoms.scoreboard;

import java.util.List;
import org.bukkit.entity.Player;

public interface ScoreboardPolicy
{
    List<String> getSidebar(final Player p0);
    
    String getPrefix(final Player p0, final Player p1);
    
    String getSuffix(final Player p0, final Player p1);
    
    String getUndername(final Player p0);
    
    int getUndernameScore(final Player p0, final Player p1);
    
    String getTablist(final Player p0);
    
    int getTablistScore(final Player p0, final Player p1);
}

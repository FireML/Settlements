package me.crz.minecraft.kingdoms.banish;

import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.land.OwnedLand;

public interface CustomRunnable
{
    void run(final OwnedLand p0, final Player p1, final Village p2);
}

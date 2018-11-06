package me.crz.minecraft.kingdoms.banish;

import me.crz.minecraft.kingdoms.land.LandOwner;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.land.OwnedLand;

public interface CustomRunnable {
    void run(final OwnedLand land, final Player player, final LandOwner landOwner);
}

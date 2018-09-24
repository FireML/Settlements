package me.crz.minecraft.kingdoms.land;

import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

class OwnedLand$1 extends BukkitRunnable {
    final /* synthetic */ OwnedLand this$0;
    private final /* synthetic */ Location val$edgeBlock;
    
    OwnedLand$1(final OwnedLand this$0, final Location val$edgeBlock) {
        this$0 = this$0;
        val$edgeBlock = val$edgeBlock;
        super();
    }
    
    public void run() {
        Bukkit.getWorld(val$edgeBlock.getWorld().getName()).getBlockAt(val$edgeBlock).setType(Material.REDSTONE_BLOCK);
    }
}
package me.crz.minecraft.kingdoms.land;

import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.ArrayList;
import org.bukkit.scheduler.BukkitRunnable;

class OwnedLand$2 extends BukkitRunnable {
    final /* synthetic */ OwnedLand this$0;
    private final /* synthetic */ ArrayList val$edgeMaterials;
    private final /* synthetic */ ArrayList val$edgeBlocks;
    
    OwnedLand$2(final OwnedLand this$0, final ArrayList val$edgeMaterials, final ArrayList val$edgeBlocks) {
        this$0 = this$0;
        val$edgeMaterials = val$edgeMaterials;
        val$edgeBlocks = val$edgeBlocks;
        super();
    }
    
    public void run() {
        for (int i = 0; i < val$edgeMaterials.size(); ++i) {
            Bukkit.getWorld(((Location)val$edgeBlocks.get(i)).getWorld().getName()).getBlockAt((Location)val$edgeBlocks.get(i)).setType((Material)val$edgeMaterials.get(i));
        }
    }
}
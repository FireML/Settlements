package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.village.Village;
import java.util.Iterator;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

class LandPerks$1 implements Runnable {
    final /* synthetic */ LandPerks this$0;
    
    LandPerks$1(final LandPerks this$0) {
        this$0 = this$0;
        super();
    }
    
    @Override
    public void run() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (ResidentManager.getInstance().doesResidentHaveVillage(player)) {
                final OwnedLand land = LandManager.getInstance().getLandFromLocation(player.getLocation());
                if (land == null) {
                    continue;
                }
                final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
                if (village == null) {
                    continue;
                }
                if (!land.getOwnerVillage().equals(village.getName())) {
                    continue;
                }
                if (!village.isBuff()) {
                    continue;
                }
                this$0.giveEffects(player, village);
            }
        }
    }
}
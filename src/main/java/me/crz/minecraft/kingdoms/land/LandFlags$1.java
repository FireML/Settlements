package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.village.Village;

class LandFlags$1 implements Runnable {
    final /* synthetic */ LandFlags this$0;
    private final /* synthetic */ Village val$originalVillage;
    private final /* synthetic */ Player val$player;
    
    LandFlags$1(final LandFlags this$0, final Village val$originalVillage, final Player val$player) {
        this$0 = this$0;
        val$originalVillage = val$originalVillage;
        val$player = val$player;
        super();
    }
    
    @Override
    public void run() {
        final TitleBuilder tb = new TitleBuilder();
        tb.withTimings(40, 60, 40);
        tb.withActionbar(val$originalVillage.getLeaveMessage());
        tb.send(val$player);
    }
}
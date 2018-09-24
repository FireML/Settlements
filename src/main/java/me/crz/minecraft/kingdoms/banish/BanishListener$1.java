package me.crz.minecraft.kingdoms.banish;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;

class BanishListener$1 implements CustomRunnable {
    final /* synthetic */ BanishListener this$0;
    private final /* synthetic */ PlayerMoveBlockEvent val$e;
    
    BanishListener$1(final BanishListener this$0, final PlayerMoveBlockEvent val$e) {
        this$0 = this$0;
        val$e = val$e;
        super();
    }
    
    @Override
    public void run(final OwnedLand land, final Player p, final Village village) {
        MessageUtils.tell(p, "&c&lYou are banished from &6&l" + village.getName());
        val$e.setCancelled(true);
    }
}
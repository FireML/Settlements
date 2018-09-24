package me.crz.minecraft.kingdoms.banish;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.event.player.PlayerTeleportEvent;

class BanishListener$2 implements CustomRunnable {
    final /* synthetic */ BanishListener this$0;
    private final /* synthetic */ PlayerTeleportEvent val$e;
    
    BanishListener$2(final BanishListener this$0, final PlayerTeleportEvent val$e) {
        this$0 = this$0;
        val$e = val$e;
        super();
    }
    
    @Override
    public void run(final OwnedLand land, final Player p, final Village village) {
        MessageUtils.tell(p, "&c&lYou cannot teleport to &6&l" + village.getName() + "&c&l because you are banished");
        val$e.setCancelled(true);
    }
}
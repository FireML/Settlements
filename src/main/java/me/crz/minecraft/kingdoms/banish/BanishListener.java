package me.crz.minecraft.kingdoms.banish;

import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;
import me.crz.minecraft.kingdoms.Base;

public class BanishListener extends Base
{
    public BanishListener() {
        super();
    }
    
    @EventHandler
    public void on(final PlayerMoveBlockEvent e) {
        VillageManager.getInstance().isResBanished(e.getTo(), e.getPlayer(), new CustomRunnable() {
            final /* synthetic */ BanishListener this$0;
            private final /* synthetic */ PlayerMoveBlockEvent val$e;
            
            BanishListener$1() {
                this$0 = this$0;
                super();
            }
            
            @Override
            public void run(final OwnedLand land, final Player p, final Village village) {
                MessageUtils.tell(p, "&c&lYou are banished from &6&l" + village.getName());
                e.setCancelled(true);
            }
        });
    }
    
    @EventHandler
    public void on(final PlayerTeleportEvent e) {
        VillageManager.getInstance().isResBanished(e.getTo(), e.getPlayer(), new CustomRunnable() {
            final /* synthetic */ BanishListener this$0;
            private final /* synthetic */ PlayerTeleportEvent val$e;
            
            BanishListener$2() {
                this$0 = this$0;
                super();
            }
            
            @Override
            public void run(final OwnedLand land, final Player p, final Village village) {
                MessageUtils.tell(p, "&c&lYou cannot teleport to &6&l" + village.getName() + "&c&l because you are banished");
                e.setCancelled(true);
            }
        });
    }
}

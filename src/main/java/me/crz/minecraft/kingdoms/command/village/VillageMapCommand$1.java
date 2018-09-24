package me.crz.minecraft.kingdoms.command.village;

import java.util.Iterator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.utils.KingdomRunnable;

class VillageMapCommand$1 implements KingdomRunnable {
    final /* synthetic */ VillageMapCommand this$0;
    private final /* synthetic */ Player val$player;
    
    VillageMapCommand$1(final VillageMapCommand this$0, final Player val$player) {
        this$0 = this$0;
        val$player = val$player;
        super();
    }
    
    @Override
    public void run() {
        val$player.sendMessage(MessageUtils.replaceColorCodes("&7--------------&eSettlement Map&r&7-------------"));
        for (final TextComponent component : VillageMapCommand.access$0(this$0, this$0.generateMap(val$player), val$player)) {
            val$player.spigot().sendMessage((BaseComponent)component);
        }
    }
}
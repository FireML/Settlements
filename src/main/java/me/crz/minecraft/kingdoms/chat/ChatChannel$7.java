package me.crz.minecraft.kingdoms.chat;

import java.util.Arrays;
import java.util.List;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;

enum ChatChannel$7
{
    ChatChannel$7(final String s, final int n) {
    }
    
    @Override
    public boolean canChat(final Player player) {
        return player.hasPermission("village.admin");
    }
    
    @Override
    public boolean canChatM(final Player player) {
        if (this.canChat(player)) {
            return true;
        }
        MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
        return false;
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.<String>asList("admin", "ac");
    }
}
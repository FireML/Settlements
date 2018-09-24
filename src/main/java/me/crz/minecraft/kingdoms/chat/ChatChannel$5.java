package me.crz.minecraft.kingdoms.chat;

import java.util.Arrays;
import java.util.List;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

enum ChatChannel$5
{
    ChatChannel$5(final String s, final int n) {
    }
    
    @Override
    public boolean canChat(final Player player) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            return false;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        return village.getChief().equals(player.getUniqueId()) || res.getVillageRank().equals(VillageRank.ELDER);
    }
    
    @Override
    public boolean canChatM(final Player player) {
        if (this.canChat(player)) {
            return true;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!");
        return false;
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.<String>asList("officer", "o");
    }
}
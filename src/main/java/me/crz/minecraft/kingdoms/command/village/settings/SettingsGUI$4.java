package me.crz.minecraft.kingdoms.command.village.settings;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.OfflinePlayer;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.List;
import java.util.Arrays;
import org.bukkit.ChatColor;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class SettingsGUI$4 extends ClickableItem {
    final /* synthetic */ SettingsGUI this$0;
    
    SettingsGUI$4(final SettingsGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        final StringBuilder sb = new StringBuilder("&8--------&c&l");
        sb.append(village.getName());
        sb.append("&r&8--------\n");
        sb.append("&a&lSTATE &r&7");
        sb.append(SettingsGUI.cfc(village.getState().name().toLowerCase()));
        sb.append("\n&6&lLEVEL &r&7");
        sb.append(village.getLevel());
        sb.append("\n&6&lLand &r&7");
        sb.append(LandManager.getInstance().getClaimedLandSize(village));
        sb.append("&r&e/&r&7");
        sb.append(Utils.getChunkMax(village.getLevel(), village.getState()));
        sb.append("\n&e&l");
        sb.append(VillageRank.CHIEF.getTag(village.getState()));
        sb.append(" &r&7");
        final OfflinePlayer player = Bukkit.getOfflinePlayer(village.getChief());
        sb.append(player.getName());
        return new CItem(Material.STAINED_GLASS_PANE, (byte)8).setName("&b&lVillage Data").setLore((List)Arrays.<String>asList(ChatColor.translateAlternateColorCodes('&', sb.toString()).split("\n"))).build();
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        return false;
    }
}
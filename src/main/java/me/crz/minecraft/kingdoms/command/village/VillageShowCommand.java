package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.OfflinePlayer;
import java.util.Iterator;
import org.bukkit.ChatColor;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.VillageState;
import java.text.DecimalFormat;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.land.LandManager;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageShowCommand extends SubCommand
{
    public VillageShowCommand() {
        super("Shows information on a village ", new String[] { "show" });
        this.setPermission("village.user");
        this.setUsage("show");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length == 0) {
            final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            if (village == null) {
                MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
                return;
            }
            MessageUtils.tellList(player, this.showSettlementMessage(village));
        }
        else {
            if (args.length != 1) {
                return;
            }
            final String name = args[0];
            final Village village2 = VillageManager.getInstance().getVillageFromName(name);
            final Player target = Bukkit.getPlayerExact(name);
            if (target != null) {
                final Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(Bukkit.getPlayerExact(name));
                MessageUtils.tellList(player, this.showSettlementMessage(playerVillage));
                return;
            }
            if (village2 != null) {
                MessageUtils.tellList(player, this.showSettlementMessage(village2));
                return;
            }
            MessageUtils.tell(player, MessageUtils.TOWN_NOT_FOUND_MESSAGE, null, null);
        }
    }
    
    List<String> showSettlementMessage(final Village village) {
        final List<String> messages = new ArrayList<String>();
        for (String line : MessageUtils.SHOW_SETTLEMENT_MESSAGE) {
            if (line.contains("{townName}")) {
                line = StringUtils.replace(line, "{townName}", village.getName());
            }
            if (line.contains("{state}")) {
                line = StringUtils.replace(line, "{state}", village.getState().name());
            }
            if (line.contains("{level}")) {
                line = StringUtils.replace(line, "{level}", Integer.toString(village.getLevel()));
            }
            if (line.contains("{land}")) {
                line = StringUtils.replace(line, "{land}", Integer.toString(LandManager.getInstance().getClaimedLandSize(village)));
            }
            if (line.contains("{maxLand}")) {
                line = StringUtils.replace(line, "{maxLand}", Integer.toString(Utils.getChunkMax(village.getLevel(), village.getState())));
            }
            if (line.contains("{outpost}")) {
                line = StringUtils.replace(line, "{outpost}", Integer.toString(LandManager.getInstance().getClaimedLandSizeOutpost(village)));
            }
            if (line.contains("{maxOutpost}")) {
                line = StringUtils.replace(line, "{maxOutpost}", Integer.toString(Utils.getChunkMaxOutpost(village.getLevel(), village.getState())));
            }
            if (line.contains("{pvp}")) {
                line = StringUtils.replace(line, "{pvp}", this.yesOrNo(village.isPvp()));
            }
            final DecimalFormat df = new DecimalFormat("#,###");
            if (line.contains("{balance}")) {
                line = StringUtils.replace(line, "{balance}", df.format((long)village.getBank()));
            }
            if (line.contains("{mobSpawning}")) {
                line = StringUtils.replace(line, "{mobSpawning}", this.yesOrNo(village.isMobSpawning()));
            }
            if (line.contains("{fireSpread}")) {
                line = StringUtils.replace(line, "{fireSpread}", this.yesOrNo(village.isFireSpread()));
            }
            if (line.contains("{explosions}")) {
                line = StringUtils.replace(line, "{explosions}", this.yesOrNo(village.isExplosions()));
            }
            if (line.contains("{buff}")) {
                line = StringUtils.replace(line, "{buff}", this.yesOrNo(village.isBuff()));
            }
            if (line.contains("{teleporter}")) {
                final String yon = (village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not available";
                line = StringUtils.replace(line, "{teleporter}", yon);
            }
            if (line.contains("{chiefRank}")) {
                line = StringUtils.replace(line, "{chiefRank}", VillageRank.CHIEF.getTag(village.getState()));
            }
            if (line.contains("{chief}")) {
                final OfflinePlayer player = Bukkit.getOfflinePlayer(village.getChief());
                line = StringUtils.replace(line, "{chief}", player.getName());
            }
            if (line.contains("{residents}")) {
                line = StringUtils.replace(line, "{residents}", this.getResidentsToString(village.getResidents(), village));
            }
            line = ChatColor.translateAlternateColorCodes('&', line);
            messages.add(line);
        }
        return messages;
    }
    
    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }
    
    public String getResidentsToString(final List<Resident> residents, final Village village) {
        final StringBuilder builder = new StringBuilder();
        int i = 0;
        for (final Resident res : residents) {
            final OfflinePlayer player = Bukkit.getOfflinePlayer(res.getUUID());
            builder.append("&e");
            builder.append(res.getVillageRank().getTag(village.getState()));
            builder.append(" ");
            builder.append(player.isOnline() ? "&a" : "&c");
            builder.append(player.getName());
            if (i + 1 < residents.size()) {
                builder.append("&7, ");
            }
            ++i;
        }
        return builder.toString();
    }
}

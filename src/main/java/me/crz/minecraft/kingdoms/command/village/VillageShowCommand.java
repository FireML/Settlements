package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.OfflinePlayer;

import org.bukkit.ChatColor;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.VillageState;

import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.land.LandManager;

import java.util.ArrayList;
import java.util.List;

import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageShowCommand extends SubCommand {
    public VillageShowCommand() {
        super("Shows information on a village ", "show");
        this.setPermission("kingdoms.user");
        this.setUsage("show");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length == 0) {
            Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            if (village == null) {
                MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
                return;
            }
            Utils.runAsync(() -> MessageUtils.tell(player, this.showSettlementMessage(village)));
        } else {
            if (args.length != 1) {
                return;
            }
            String name = args[0];
            Utils.runAsync(() -> {
                Village village2 = VillageManager.getInstance().getVillageFromName(name);
                Resident target = ResidentManager.getInstance().getResident(name);
                if (target != null) {
                    Village playerVillage = VillageManager.getInstance().getVillageFromResident(target);
                    MessageUtils.tell(player, this.showSettlementMessage(playerVillage));
                    return;
                }
                if (village2 != null) {
                    MessageUtils.tell(player, this.showSettlementMessage(village2));
                    return;
                }
                MessageUtils.tell(player, MessageUtils.Message.TOWN_NOT_FOUND, null, null);
            });
        }
    }

    private String showSettlementMessage(Village village) {
        String message = MessageUtils.Message.SHOW_SETTLEMENT.getText();
        if (message.contains("{townName}")) {
            message = message.replace("{townName}", village.getName());
        }
        if (message.contains("{state}")) {
            message = message.replace("{state}", village.getState().name());
        }
        if (message.contains("{level}")) {
            message = message.replace("{level}", Integer.toString(village.getLevel()));
        }
        if (message.contains("{land}")) {
            message = message.replace("{land}", Integer.toString(LandManager.getInstance().getClaimedLandSize(village)));
        }
        if (message.contains("{maxLand}")) {
            message = message.replace("{maxLand}", Integer.toString(Utils.getChunkMax(village.getLevel(), village.getState())));
        }
        if (message.contains("{outpost}")) {
            message = message.replace("{outpost}", Integer.toString(LandManager.getInstance().getClaimedLandSizeOutpost(village)));
        }
        if (message.contains("{maxOutpost}")) {
            message = message.replace("{maxOutpost}", Integer.toString(Utils.getChunkMaxOutpost(village.getLevel(), village.getState())));
        }
        if (message.contains("{pvp}")) {
            message = message.replace("{pvp}", this.yesOrNo(village.isPvp()));
        }
        if (message.contains("{balance}")) {
            DecimalFormat df = new DecimalFormat("#,###");
            message = message.replace("{balance}", df.format((long) village.getBank()));
        }
        if (message.contains("{mobSpawning}")) {
            message = message.replace("{mobSpawning}", this.yesOrNo(village.isMobSpawning()));
        }
        if (message.contains("{fireSpread}")) {
            message = message.replace("{fireSpread}", this.yesOrNo(village.isFireSpread()));
        }
        if (message.contains("{explosions}")) {
            message = message.replace("{explosions}", this.yesOrNo(village.isExplosions()));
        }
        if (message.contains("{buff}")) {
            message = message.replace("{buff}", this.yesOrNo(village.isBuff()));
        }
        if (message.contains("{teleporter}")) {
            String yon = (village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not available";
            message = message.replace("{teleporter}", yon);
        }
        if (message.contains("{chiefRank}")) {
            message = message.replace("{chiefRank}", VillageRank.CHIEF.getTag(village.getState()));
        }
        if (message.contains("{chief}")) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(village.getChief());
            message = message.replace("{chief}", player.getName());
        }
        if (message.contains("{residents}")) {
            message = message.replace("{residents}", this.getResidentsToString(village.getResidents(), village));
        }
        return MessageUtils.replaceColorCodes(message);
    }

    public String yesOrNo(boolean b) {
        return b ? "Yes" : "No";
    }

    private String getResidentsToString(List<Resident> residents, Village village) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Resident res : residents) {
            builder.append("&e");
            builder.append(res.getVillageRank().getTag(village.getState()));
            builder.append(" ");
            builder.append(res.getPlayer() != null ? "&a" : "&c");
            builder.append(res.getName());
            if (i + 1 < residents.size()) {
                builder.append("&7, ");
            }
            ++i;
        }
        return builder.toString();
    }
}

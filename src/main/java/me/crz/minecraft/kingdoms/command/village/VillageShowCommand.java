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
        this.setPermission("village.user");
        this.setUsage("show");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length == 0) {
            Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            if (village == null) {
                MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
                return;
            }
            Utils.runAsync(() -> MessageUtils.tellList(player, this.showSettlementMessage(village)));
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
                    MessageUtils.tellList(player, this.showSettlementMessage(playerVillage));
                    return;
                }
                if (village2 != null) {
                    MessageUtils.tellList(player, this.showSettlementMessage(village2));
                    return;
                }
                MessageUtils.tell(player, MessageUtils.TOWN_NOT_FOUND_MESSAGE, null, null);
            });
        }
    }

    private List<String> showSettlementMessage(Village village) {
        List<String> messages = new ArrayList<>();
        for (String line : MessageUtils.SHOW_SETTLEMENT_MESSAGE) {
            if (line.contains("{townName}")) {
                line = line.replace("{townName}", village.getName());
            }
            if (line.contains("{state}")) {
                line = line.replace("{state}", village.getState().name());
            }
            if (line.contains("{level}")) {
                line = line.replace("{level}", Integer.toString(village.getLevel()));
            }
            if (line.contains("{land}")) {
                line = line.replace("{land}", Integer.toString(LandManager.getInstance().getClaimedLandSize(village)));
            }
            if (line.contains("{maxLand}")) {
                line = line.replace("{maxLand}", Integer.toString(Utils.getChunkMax(village.getLevel(), village.getState())));
            }
            if (line.contains("{outpost}")) {
                line = line.replace("{outpost}", Integer.toString(LandManager.getInstance().getClaimedLandSizeOutpost(village)));
            }
            if (line.contains("{maxOutpost}")) {
                line = line.replace("{maxOutpost}", Integer.toString(Utils.getChunkMaxOutpost(village.getLevel(), village.getState())));
            }
            if (line.contains("{pvp}")) {
                line = line.replace("{pvp}", this.yesOrNo(village.isPvp()));
            }
            DecimalFormat df = new DecimalFormat("#,###");
            if (line.contains("{balance}")) {
                line = line.replace("{balance}", df.format((long) village.getBank()));
            }
            if (line.contains("{mobSpawning}")) {
                line = line.replace("{mobSpawning}", this.yesOrNo(village.isMobSpawning()));
            }
            if (line.contains("{fireSpread}")) {
                line = line.replace("{fireSpread}", this.yesOrNo(village.isFireSpread()));
            }
            if (line.contains("{explosions}")) {
                line = line.replace("{explosions}", this.yesOrNo(village.isExplosions()));
            }
            if (line.contains("{buff}")) {
                line = line.replace("{buff}", this.yesOrNo(village.isBuff()));
            }
            if (line.contains("{teleporter}")) {
                String yon = (village.getState() == VillageState.KINGDOM && village.getLevel() >= 4) ? (village.isTeleporter() ? "Private" : "Public") : "Not available";
                line = line.replace("{teleporter}", yon);
            }
            if (line.contains("{chiefRank}")) {
                line = line.replace("{chiefRank}", VillageRank.CHIEF.getTag(village.getState()));
            }
            if (line.contains("{chief}")) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(village.getChief());
                line = line.replace("{chief}", player.getName());
            }
            if (line.contains("{residents}")) {
                line = line.replace("{residents}", this.getResidentsToString(village.getResidents(), village));
            }
            line = ChatColor.translateAlternateColorCodes('&', line);
            messages.add(line);
        }
        return messages;
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

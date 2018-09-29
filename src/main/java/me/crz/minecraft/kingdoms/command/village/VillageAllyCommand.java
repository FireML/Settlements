package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAllyCommand extends SubCommand {
    public VillageAllyCommand() {
        super("Ally another village", "ally");
        this.setPermission("village.user");
        this.setUsage("ally <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        if (!this.canAlly(ResidentManager.getInstance().getResident(player))) {
            MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.ELDER.getTag(village.getState()));
            return;
        }
        Village invitedVillage = VillageManager.getInstance().getVillageFromName(args[0].toLowerCase());
        if (invitedVillage == null) {
            MessageUtils.tell(player, "&c&lVillage &r&6&l" + args[0] + "&r&c&l doesn't exist!");
            return;
        }
        if (invitedVillage.getName().equals(village.getName())) {
            MessageUtils.tell(player, "&c&lYou can't ally yourself!");
            return;
        }
        if (village.isAlly(invitedVillage)) {
            MessageUtils.tell(player, "&c&lYou are already allied to Village &r&6&l" + args[0]);
            return;
        }
        if (TownCache.getPendingTownAlly().containsKey(village.getName())) {
            List<String> invites = TownCache.getPendingTownAlly().get(village.getName());
            if (invites.contains(invitedVillage.getName())) {
                MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), MessageUtils.Message.NOW_ALLIED_TO, "name", village.getName());
                MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.NOW_ALLIED_TO, "name", invitedVillage.getName());
                invitedVillage.addAlly(village.getName());
                village.addAlly(invitedVillage.getName());

                Utils.runAsync(() -> {
                    DatabaseManager.getInstance().saveVillage(invitedVillage);
                    DatabaseManager.getInstance().saveVillage(village);
                });
            }
        } else {
            if (TownCache.getPendingTownAlly().containsKey(invitedVillage.getName())) {
                List<String> iInvites = TownCache.getPendingTownAlly().get(invitedVillage.getName());
                iInvites.add(village.getName());
                MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), MessageUtils.Message.ALLY_INVITE, "name", village.getName());
                MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), MessageUtils.Message.ALLY_ACCEPT_INFO, "name", village.getName());
            } else {
                TownCache.getPendingTownAlly().put(invitedVillage.getName(), new ArrayList<>(Collections.singletonList(village.getName())));
                MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), MessageUtils.Message.ALLY_INVITE, "name", village.getName());
            }
            MessageUtils.tell(player, "&7&lAlly request sent to &r&6&l" + invitedVillage.getName());
        }
    }

    private boolean canAlly(Resident res) {
        return VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER);
    }
}

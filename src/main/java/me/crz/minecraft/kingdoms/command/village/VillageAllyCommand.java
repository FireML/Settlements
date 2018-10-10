package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;

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
        this.setPermission("kingdoms.user");
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
        if (ResidentManager.getInstance().getResident(player).getVillageRank().below(VillageRank.ELDER)) {
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
        if (TownCache.getPendingTownAlly().containsEntry(village.getName(), invitedVillage.getName())) {
            MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), MessageUtils.Message.NOW_ALLIED_TO, "name", village.getName());
            MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.NOW_ALLIED_TO, "name", invitedVillage.getName());
            TownCache.getPendingTownAlly().remove(village.getName(), invitedVillage.getName());
            invitedVillage.addAlly(village);
            village.addAlly(invitedVillage);

            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(invitedVillage);
                DatabaseManager.getInstance().saveVillage(village);
            });
        } else {
            TownCache.getPendingTownAlly().put(invitedVillage.getName(), village.getName());
            MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), MessageUtils.Message.ALLY_INVITE, "name", village.getName());
            MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), MessageUtils.Message.ALLY_ACCEPT_INFO, "name", village.getName());
            MessageUtils.tell(player, "&7&lAlly request sent to &r&6&l" + invitedVillage.getName());
        }
    }
}

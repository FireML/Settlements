package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageUnAllyCommand extends SubCommand {
    public VillageUnAllyCommand() {
        super("remove an allied village", "unally");
        this.setPermission("village.user");
        this.setUsage("unally <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        if (!this.canAlly(ResidentManager.getInstance().getResident(player))) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!");
            return;
        }
        Village invitedVillage = VillageManager.getInstance().getVillageFromName(args[0].toLowerCase());
        if (invitedVillage == null) {
            MessageUtils.tell(player, "&c&lVillage &r&6&l" + args[0] + "&r&c&l doesn't exist!");
            return;
        }
        if (invitedVillage.getName().equals(village.getName())) {
            MessageUtils.tell(player, "&c&lYou can't un-ally yourself!");
            return;
        }
        if (!village.isAlly(invitedVillage)) {
            MessageUtils.tell(player, "&c&lYou are not allied to Village &r&6&l" + args[0]);
            return;
        }
        village.removeAlly(invitedVillage);
        invitedVillage.removeAlly(village);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(invitedVillage);
            DatabaseManager.getInstance().saveVillage(village);
        });
        MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), "&7&lAlly &6" + village.getName() + " &7removed!");
        MessageUtils.tellResidents(village.getOnlineResidents(), "&7&lAlly &6" + invitedVillage.getName() + " &7removed!");
    }

    private boolean canAlly(Resident res) {
        return VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER);
    }
}

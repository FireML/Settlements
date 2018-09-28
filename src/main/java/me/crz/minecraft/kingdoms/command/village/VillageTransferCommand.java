package me.crz.minecraft.kingdoms.command.village;

import java.util.UUID;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.OfflinePlayerManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageTransferCommand extends SubCommand {
    public VillageTransferCommand() {
        super("Transfers leadership of your village", "transfer");
        this.setPermission("village.user");
        this.setUsage("transfer <player>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + " or higher&r&c&l!", null, null);
            return;
        }
        Utils.runAsync(() -> {
            Resident tres = ResidentManager.getInstance().getResident(args[0]);
            if (tres == null) {
                MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE, null, null);
                return;
            }
            if (!this.isResident(tres, village)) {
                MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
                return;
            }
            tres.setVillageRank(VillageRank.CHIEF);
            res.setVillageRank(VillageRank.ELDER);
            village.setChief(tres.getUUID());
            DatabaseManager.getInstance().saveResident(res);
            DatabaseManager.getInstance().saveResident(tres);
            DatabaseManager.getInstance().saveVillage(village);
            MessageUtils.tellResidents(village.getOnlineResidents(), this.getSuccessSetRankMessage(VillageRank.CHIEF.getTag(village.getState()), player, args[0]), null, null);
        });
    }

    private String getSuccessSetRankMessage(String rank, Player player, String target) {
        String message = MessageUtils.SUCCESS_SET_RANK_MESSAGE;
        if (message.contains("{setter}")) {
            message = message.replace("{setter}", player.getName());
        }
        if (message.contains("{target}")) {
            message = message.replace("{target}", target);
        }
        if (message.contains("{rank}")) {
            message = message.replace("{rank}", rank);
        }
        return message;
    }

    private boolean isResident(Resident resident, Village village) {
        return resident.getVillageName().equals(village.getName());
    }
}

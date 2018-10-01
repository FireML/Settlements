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

public class VillageTransferCommand extends SubCommand {
    public VillageTransferCommand() {
        super("Transfers leadership of your village", "transfer");
        this.setPermission("kingdoms.user");
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
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
            MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.CHIEF.getTag(village.getState()));
            return;
        }
        Utils.runAsync(() -> {
            Resident tres = ResidentManager.getInstance().getResident(args[0]);
            if (tres == null) {
                MessageUtils.tell(player, MessageUtils.Message.PLAYER_NOT_FOUND, null, null);
                return;
            }
            if (!this.isResident(tres, village)) {
                MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT, null, null);
                return;
            }
            tres.setVillageRank(VillageRank.CHIEF);
            res.setVillageRank(VillageRank.ELDER);
            village.setChief(tres.getUUID());
            DatabaseManager.getInstance().saveResident(res);
            DatabaseManager.getInstance().saveResident(tres);
            DatabaseManager.getInstance().saveVillage(village);
            MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.SUCCESS_SET_RANK,
                    "rank", VillageRank.CHIEF.getTag(village.getState()),
                    "setter", player.getName(),
                    "target", args[0]
            );
        });
    }

    private boolean isResident(Resident resident, Village village) {
        return resident.getVillageName().equals(village.getName());
    }
}

package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminRankCommand extends SubCommand {
    public VillageAdminRankCommand(SubCommand parent) {
        super(parent, "join <player> <rank>");
        setPermission("kingdoms.admin");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        String rank = args[1].toUpperCase();
        if (!this.rankIsValid(rank)) {
            MessageUtils.tell(player, getError("invalid-rank"), "rank", rank);
            return true;
        }
        Utils.runAsync(() -> {
            Resident tRes = ResidentManager.getInstance().getResident(args[0]);
            if (tRes == null) {
                MessageUtils.tell(player, getError("player-not-found"), "player", args[0]);
                return;
            }
            Village village = VillageManager.getInstance().getVillageFromName(tRes.getVillageName());
            if (village == null) {
                MessageUtils.tell(player, getError("not-in-village"), "player", tRes.getName());
                return;
            }
            Resident res = ResidentManager.getInstance().getResident(village.getChief());
            VillageRank r = VillageRank.valueOf(rank);
            if (r.equals(VillageRank.CHIEF)) {
                res.setVillageRank(tRes.getVillageRank());
                tRes.setVillageRank(r);
                village.setChief(tRes.getUUID());

                DatabaseManager.getInstance().saveVillage(village);
                DatabaseManager.getInstance().saveResident(res);
                DatabaseManager.getInstance().saveResident(tRes);
            } else {
                tRes.setVillageRank(r);
                DatabaseManager.getInstance().saveResident(tRes);
            }
            MessageUtils.tell(player, Message.get("command.village.setrank.success"), "setter", player.getDisplayName(), "target", tRes.getName(), "rank", r.getTag(village));
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("command.village.setrank.success"), "setter", player.getDisplayName(), "target", tRes.getName(), "rank", r.getTag(village));
        });
        return true;
    }

    private boolean rankIsValid(String arg) {
        VillageRank[] values;
        for (int length = (values = VillageRank.values()).length, i = 0; i < length; ++i) {
            VillageRank rank = values[i];
            if (rank.name().equals(arg)) {
                return true;
            }
        }
        return false;
    }
}

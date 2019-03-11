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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAdminRankCommand extends SubCommand {
    public VillageAdminRankCommand(SubCommand parent) {
        super(parent, "rank <player> <rank>", "setrank");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        VillageRank rank;
        try {
            rank = VillageRank.valueOf(args[1]);
        } catch (IllegalArgumentException e) {
            MessageUtils.tell(sender, getError("invalid-rank"), "rank", args[1]);
            return true;
        }
        VillageRank r = rank;
        Utils.runAsync(() -> {
            Resident tRes = ResidentManager.getInstance().getResident(args[0]);
            if (tRes == null) {
                MessageUtils.tell(sender, getError("player-not-found"), "player", args[0]);
                return;
            }
            Village village = VillageManager.getInstance().getVillageFromName(tRes.getVillageName());
            if (village == null) {
                MessageUtils.tell(sender, getError("not-in-village"), "player", tRes.getName());
                return;
            }
            Resident res = village.getChief();
            if (r == VillageRank.getChief()) {
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
            if (sender instanceof Player && !village.getResidentIds().contains(((Player) sender).getUniqueId())) {
                MessageUtils.tell(sender, Message.get("command.village.setrank.success"), "title", "", "setter", sender.getName(), "target", tRes.getName(), "rank", r.getTag(village));
            }
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("command.village.setrank.success"), "title", "", "setter", sender.getName(), "target", tRes.getName(), "rank", r.getTag(village));
        });
        return true;
    }
}

package de.themoep.settlements.command.admin;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.events.LandownerChiefChangeEvent;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.rank.VillageRank;
import de.themoep.settlements.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import de.themoep.settlements.command.SubCommand;

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
            if (!tRes.hasVillage()) {
                MessageUtils.tell(sender, getError("not-in-village"), "player", tRes.getName());
                return;
            }
            Utils.runSync(() -> {
                Village village = tRes.getVillage();
                if (r.isChief()) {
                    Resident res = tRes.getVillage().getChief();
                    Utils.callEvent(new LandownerChiefChangeEvent(village, res, tRes));
                    res.setVillageRank(tRes.getVillageRank());
                    tRes.setVillageRank(r);
                    village.setChief(tRes.getUUID());

                    DatabaseManager.getInstance().saveSettlement(village);
                    DatabaseManager.getInstance().saveResident(res);
                    DatabaseManager.getInstance().saveResident(tRes);
                } else {
                    tRes.setVillageRank(r);
                    DatabaseManager.getInstance().saveResident(tRes);
                }
                if (sender instanceof Player && !village.getResidentIds().contains(((Player) sender).getUniqueId())) {
                    Message.get("command.village.setrank.success").tell(sender, "title", "", "setter", sender.getName(), "target", tRes.getName(), "rank", r.getTag(village));
                }
                Message.get("command.village.setrank.success").tell(village.getOnlineResidents(), "title", "", "setter", sender.getName(), "target", tRes.getName(), "rank", r.getTag(village));
            });
        });
        return true;
    }
}

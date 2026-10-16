package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.events.LandownerChiefChangeEvent;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminRankCommand extends SubCommand {
    public AdminRankCommand(SubCommand parent) {
        super(parent, "rank <player> <rank>", "setrank");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        SettlementRank rank;
        try {
            rank = SettlementRank.valueOf(args[1]);
        } catch (IllegalArgumentException e) {
            MessageUtils.tell(sender, getError("invalid-rank").getReplacedComponent("rank", args[1]));
            return true;
        }
        SettlementRank r = rank;
        Utils.runAsync(() -> {
            Resident tRes = ResidentManager.getInstance().getResident(args[0]);
            if (tRes == null) {
                MessageUtils.tell(sender, getError("player-not-found").getReplacedComponent("player", args[0]));
                return;
            }
            if (!tRes.hasSettlement()) {
                MessageUtils.tell(sender, getError("not-in-settlement").getReplacedComponent("player", tRes.getName()));
                return;
            }
            Utils.runSync(() -> {
                Settlement settlement = tRes.getSettlement();
                if (r.isChief()) {
                    Resident res = tRes.getSettlement().getChief();
                    new LandownerChiefChangeEvent(settlement, res, tRes).callEvent();
                    res.setSettlementRank(tRes.getSettlementRank());
                    tRes.setSettlementRank(r);
                    settlement.setChief(tRes.getUUID());

                    DatabaseManager.getInstance().saveSettlement(settlement);
                    DatabaseManager.getInstance().saveResident(res);
                    DatabaseManager.getInstance().saveResident(tRes);
                } else {
                    tRes.setSettlementRank(r);
                    DatabaseManager.getInstance().saveResident(tRes);
                }
                if (sender instanceof Player player && !settlement.getResidentIds().contains(player.getUniqueId())) {
                    Message.get("command.settlement.setrank.success").tell(sender, "title", "", "setter", sender.getName(), "target", tRes.getName(), "rank", r.getTag(settlement));
                }
                Message.get("command.settlement.setrank.success").tell(settlement.getOnlineResidents(), "title", "", "setter", sender.getName(), "target", tRes.getName(), "rank", r.getTag(settlement));
            });
        });
        return true;
    }
}

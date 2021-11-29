package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

public class SettlementKickCommand extends SettlementSubCommand {
    public SettlementKickCommand(SubCommand parent) {
        super(parent, "kick <name>");
    }

    @Override
    public boolean run(Resident player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase(player.getName())) {
            MessageUtils.tell(player, getMessage("cant-kick-yourself"));
            return true;
        }
        Utils.runAsync(() -> {
            Resident res = ResidentManager.getInstance().getResident(args[0]);
            if (res == null) {
                MessageUtils.tell(player, getError("player-not-found"));
                return;
            }
            if (!player.getSettlement().equals(res.getSettlement())) {
                MessageUtils.tell(player, getError("not-in-current"));
                return;
            }
            if (res.getSettlementRank().atLeast(player.getSettlementRank())) {
                MessageUtils.tell(player, getMessage("cant-kick-higher"), "rank", res.getSettlementRank().getTag(res.getSettlement()));
                return;
            }
            MessageUtils.tellResidents(res.getSettlement().getOnlineResidents(), getMessage("kicked-alert"), "player", res.getName());
            Utils.runSync(() -> {
                InviteCache.getPendingSettlementInvites().remove(res.getUUID(), res.getSettlement().getId());
                res.leaveSettlement();
            });
        });
        return true;
    }
}

package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

public class SettlementKickNoticeCommand extends SettlementSubCommand {
    public SettlementKickNoticeCommand(SubCommand parent) {
        super(parent, "kicknotice <name>");
    }

    @Override
    public boolean run(Resident player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Utils.runAsync(() -> {
            Resident res = ResidentManager.getInstance().getResident(args[0]);
            if (res == null) {
                MessageUtils.tell(player, getError("player-not-found").getComponent());
                return;
            }
            if (!player.getSettlement().equals(res.getSettlement())) {
                MessageUtils.tell(player, getError("not-in-current").getComponent());
                return;
            }
            if (res.getSettlementRank().atLeast(player.getSettlementRank())) {
                MessageUtils.tell(player, getMessage("cant-kick-higher").getReplacedComponent("rank", res.getSettlementRank().getTag(res.getSettlement())));
            }
        });
        return true;
    }
}

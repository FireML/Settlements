package de.themoep.settlements.command.admin;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.village.events.SettlementJoinEvent;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import de.themoep.settlements.command.SubCommand;

public class VillageAdminJoinCommand extends SubCommand {
    public VillageAdminJoinCommand(SubCommand parent) {
        super(parent, "join <player> <settlement>");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        Settlement settlement = SettlementManager.getInstance().getFromName(args[1]);
        if (settlement == null) {
            getError("settlement-doesnt-exist").tell(sender, "name", args[1]);
            return true;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                getError("player-not-found").tell(sender, "name", args[0]);
                return;
            }
            if (target.getSettlementName().equals(settlement.getName())) {
                MessageUtils.tell(sender, "&6&l" + target.getName() + "&r&c&l is already in the &r&6&l" + settlement.getName());
                return;
            }
            Utils.runSync(() -> {
                Utils.callEvent(new SettlementJoinEvent(settlement, target));
                target.setSettlement(settlement);
                target.setSettlementRank(SettlementRank.getDefault());
                target.addChannel(ChatChannel.SETTLEMENT);
                Message.get("command.chat.joined").tell(target, "name", ChatChannel.SETTLEMENT.getTag());
                DatabaseManager.getInstance().saveResident(target);
                MessageUtils.tell(sender, "&6&l" + target.getName() + "&r&c&l is now in " + settlement.getName());
                Player targetP = target.getPlayer();
                if (targetP != null) {
                    MessageUtils.tell(targetP, Message.get("command.settlement.join.success"), "name", settlement.getName(), "state", settlement.getState().getTag());
                }
                MessageUtils.tellResidents(settlement.getOnlineResidents(), Message.get("command.settlement.join.new-resident"), "player", target.getName());
            });
        });
        return true;
    }
}

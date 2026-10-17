package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.firedev.chatchannels.api.ChatChannel;

public class AdminJoinCommand extends SubCommand {
    public AdminJoinCommand(SubCommand parent) {
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
                MessageUtils.tell(sender, ComponentUtils.parseComponent("&6&l" + target.getName() + "&r&c&l is already in the &r&6&l" + settlement.getName()));
                return;
            }
            Utils.runSync(() -> {
                settlement.addMember(target);
                Message.get("command.chat.joined").tell(target);
                MessageUtils.tell(sender, ComponentUtils.parseComponent("&6&l" + target.getName() + "&r&c&l is now in " + settlement.getName()));
                Player targetP = target.getPlayer();
                if (targetP != null) {
                    MessageUtils.tell(targetP, Message.get("command.settlement.join.success").getReplacedComponent("name", settlement.getName(), "state", settlement.getState().getTag()));
                }
                MessageUtils.tellResidents(settlement.getOnlineResidents(), Message.get("command.settlement.join.new-resident").getReplacedComponent("player", target.getName()));
            });
        });
        return true;
    }
}

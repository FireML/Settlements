package de.themoep.settlements.command.friend;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

public class FriendAcceptCommand extends PlayerSubCommand {
    public FriendAcceptCommand(PlayerSubCommand parent) {
        super(parent, "accept <name>", "approve");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        FriendRequest request = res.removeFriendRequest(args[0]);
        if (request == null) {
            getError("no-request").tell(player, "name", args[0]);
            return true;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                getError("player-not-found").tell(player, "name", args[0]);
                return;
            }
            DatabaseManager.getInstance().delete(request);
            FriendRequest reverseRequest = target.removeFriendRequest(res.getName());
            if (reverseRequest != null) {
                DatabaseManager.getInstance().delete(reverseRequest);
            }
            Utils.runSync(() -> {
                target.addFriend(res.getUUID());
                res.addFriend(target.getUUID());
                getMessage("success").tell(player, "name", target.getName());
                if (target.getPlayer() != null) {
                    getMessage("received").tell(target, "name", res.getName());
                } else {
                    for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                        provider.sendMail(res.getSettlementTitle() + " " + res.getName(), target, getText("mail",
                                "name", res.getName()
                        ));
                    }
                }
                target.save();
                res.save();
            });
        });
        return true;
    }
}

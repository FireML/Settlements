package de.themoep.settlements.command.friend;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

public class FriendAddCommand extends PlayerSubCommand {
    public FriendAddCommand(PlayerSubCommand parent) {
        super(parent, "add <name>", "request");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                getError("player-not-found").tell(player, "name", args[0]);
                return;
            }
            Resident res = ResidentManager.getInstance().getResident(player);
            if (res.isFriend(target)) {
                getError("already-friends").tell(player, "name", target.getName());
                return;
            }
            Utils.runSync(() -> {
                FriendRequest request = new FriendRequest(target, res);
                if (!target.addFriendRequest(request)) {
                    getError("already-sent-request").tell(player, "name", target.getName());
                    return;
                }
                request.save();
                getMessage("success").tell(player, "name", target.getName());
                if (target.getPlayer() != null) {
                    getMessage("received").tell(target, "name", res.getName());
                }
            });
        });
        return true;
    }
}

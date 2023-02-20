package de.themoep.settlements.command.friend;

import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

public class FriendRemoveCommand extends PlayerSubCommand {
    public FriendRemoveCommand(PlayerSubCommand parent) {
        super(parent, "remove <name>", "reject");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident res = ResidentManager.getInstance().getResident(player);
            FriendRequest request = res.removeFriendRequest(args[0]);
            if (request != null) {
                DatabaseManager.getInstance().delete(request);
                getMessage("removed-request").tell(res, "name", request.getSenderName());
                return;
            }
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null ) {
                getError("player-not-found").tell(player, "name", args[0]);
                return;
            }
            if (!res.removeFriend(target.getUUID())) {
                getError("not-friends").tell(res, "name", target.getName());
                return;
            }
            target.removeFriend(res.getUUID());
            res.save();
            target.save();
            getMessage("success").tell(res, "name", target.getName());
        });
        return true;
    }
}

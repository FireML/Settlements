package de.themoep.settlements.command.friend;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

public class FriendAddCommand extends PlayerSubCommand {
    public FriendAddCommand(PlayerSubCommand parent) {
        super(parent, "add <name>");
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
            if (!res.addFriend(target.getUUID())) {
                getError("already-friends").tell(player, "name", target.getName());
                return;
            }
            DatabaseManager.getInstance().saveResident(res);
            getMessage("success").tell(player, "name", target.getName());
        });
        return true;
    }
}

package de.themoep.settlements.command.friend;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;
import de.themoep.settlements.command.PlayerSubCommand;

public class FriendRemoveCommand extends PlayerSubCommand {
    public FriendRemoveCommand(PlayerSubCommand parent) {
        super(parent, "remove <name>");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null ) {
                getError("player-not-found").tell(player, "name", args[0]);
                return;
            }
            Resident res = ResidentManager.getInstance().getResident(player);
            if (!res.removeFriend(target.getUUID())) {
                getError("not-friends").tell(player, "name", target.getName());
                return;
            }
            DatabaseManager.getInstance().saveResident(res);
            getMessage("success").tell(player, "name", target.getName());
        });
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.friend;

import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
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
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
            getMessage("success").tell(player, "name", target.getName());
        });
        return true;
    }
}

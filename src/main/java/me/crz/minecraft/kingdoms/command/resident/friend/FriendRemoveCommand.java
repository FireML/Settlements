package me.crz.minecraft.kingdoms.command.resident.friend;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class FriendRemoveCommand extends SubCommand {
    public FriendRemoveCommand(SubCommand parent) {
        super(parent, "remove <name>");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null ) {
                MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l found!", "{player}", args[0]);
                return;
            }
            Resident res = ResidentManager.getInstance().getResident(player);
            if (!res.removeFriend(target.getUUID())) {
                MessageUtils.tell(player, "&c you are not friends with &6" + target.getName());
                return;
            }
            DatabaseManager.getInstance().saveResident(res);
            MessageUtils.tell(player, "&cSuccessfully removed &6" + target.getName() + "&c from your friends list");
        });
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.resident.friend;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class FriendAddCommand extends SubCommand {
    public FriendAddCommand() {
        super("Adds a player to your friend list", "add");
        this.setPermission("village.user");
        this.setUsage("add <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l found!", "{player}", args[0]);
                return;
            }
            Resident res = ResidentManager.getInstance().getResident(player);
            if (!res.addFriend(target.getUUID())) {
                MessageUtils.tell(player, "&c you are already friends with &6" + target.getName());
                return;
            }
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
            MessageUtils.tell(player, "&aSuccessfully added &6" + target.getName() + "&a as a friend");
        });
    }
}

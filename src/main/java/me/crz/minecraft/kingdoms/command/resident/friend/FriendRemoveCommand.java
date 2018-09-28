package me.crz.minecraft.kingdoms.command.resident.friend;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class FriendRemoveCommand extends SubCommand {
    public FriendRemoveCommand() {
        super("Removes a player to your friend list", "remove");
        this.setPermission("village.user");
        this.setUsage("remove <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(player);
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
    }
}

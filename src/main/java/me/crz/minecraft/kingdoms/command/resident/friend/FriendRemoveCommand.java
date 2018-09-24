package me.crz.minecraft.kingdoms.command.resident.friend;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class FriendRemoveCommand extends SubCommand {
    public FriendRemoveCommand() {
        super("Removes a player to your friend list", "remove");
        this.setPermission("village.user");
        this.setUsage("remove <name>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l can be found!", "{player}", args[0]);
            return;
        }
        final Player target = Bukkit.getPlayer(args[0]);
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!res.getFriends().contains(target.getUniqueId().toString())) {
            MessageUtils.tell(player, "&c you are not friends with &6" + target.getDisplayName());
            return;
        }
        res.removeFriend(target.getUniqueId().toString());
        ResidentManager.getInstance().saveResident(res);
        MessageUtils.tell(player, "&cSuccessfully removed &6" + target.getDisplayName() + "&c from your friends list");
    }
}

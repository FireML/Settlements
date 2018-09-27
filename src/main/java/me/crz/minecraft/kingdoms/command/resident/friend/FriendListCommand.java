package me.crz.minecraft.kingdoms.command.resident.friend;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class FriendListCommand extends SubCommand {
    public FriendListCommand() {
        super("Lists all the people on your friends list", "list");
        this.setPermission("village.user");
        this.setUsage("list");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        Utils.runAsync(() -> MessageUtils.tell(player, this.getFriendsToString(ResidentManager.getInstance().getResident(player).getFriends())));
    }

    public String getFriendsToString(final Collection<UUID> ids) {
        return "&a&lFriends:&r\n" +
                ids.stream().map(id -> {
                    Player player = Bukkit.getPlayer(id);
                    if (player != null && player.isOnline()) {
                        return "&a" + player.getName();
                    }
                    Resident resident = ResidentManager.getInstance().getResident(id);
                    if (resident != null) {
                        return "&c" + resident.getName();
                    }
                    return "&c" + id;
                }).collect(Collectors.joining("&7, "));
    }
}

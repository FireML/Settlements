package me.crz.minecraft.kingdoms.command.resident.friend;

import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.UUID;
import java.util.List;
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
        MessageUtils.tell(player, this.getFriendsToString(ResidentManager.getInstance().getResident(player).getFriends()));
    }

    public String getFriendsToString(final Collection<String> ids) {
        return "&a&lFriends:&r\n" +
                ids.stream().map(id -> {
                    final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(id));
                    return (player.isOnline() ? "&a" : "&c") + player.getName();
                }).collect(Collectors.joining("&7, "));
    }
}

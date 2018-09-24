package me.crz.minecraft.kingdoms.command.resident.friend;

import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import java.util.UUID;
import java.util.List;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class FriendListCommand extends SubCommand
{
    public FriendListCommand() {
        super("Lists all the people on your friends list", new String[] { "list" });
        this.setPermission("village.user");
        this.setUsage("list");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        MessageUtils.tell(player, this.getFriendsToString(ResidentManager.getInstance().getResident(player).getFriends()));
    }
    
    public String getFriendsToString(final List<String> ids) {
        final StringBuilder builder = new StringBuilder();
        builder.append("&a&lFriends:&r\n");
        for (int i = 0; i < ids.size(); ++i) {
            try {
                final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString((String)ids.get(i)));
                builder.append(player.isOnline() ? "&a" : "&c");
                builder.append(player.getName());
                if (i + 1 < ids.size()) {
                    builder.append("&7, ");
                }
            }
            catch (Exception ex) {}
        }
        return builder.toString();
    }
}

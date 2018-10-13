package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

public class ChatCommand extends SubCommand {
    private final String help;

    public ChatCommand() {
        super(null, "chat");
        help = "&8&l--------&r&c&l/Chat&r&8&l--------" +
                "\n&8[&9*&8] &b/{alias} <channel> &7- sets the active chat" +
                "\n&8[&9*&8] &b/{alias} leave <channel> &7- disable the chat of the channel" +
                "\n&8[&9*&8] &b/{alias} swear &7- toggles swearing";
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        if (args.length < 1) {
            StringBuilder temp = new StringBuilder(help.replace("{alias}", alias));
            if (p.hasPermission("kingdoms.staff")) {
                temp.append("\n&8[&9*&8] &b/").append(alias).append(" lock &7- locks and unlocks general chat");
            }
            Resident res = ResidentManager.getInstance().getResident(p);
            for (ChatChannel channel : ChatChannel.values()) {
                if (channel.canChat(p)) {
                    String channelName = channel.humanizedName();
                    if (channel.equals(res.getActiveChannel())) {
                        temp.append("\n&8[&9*&8] &b").append(channelName).append(" &7&l[&a&lActive&7&l] &3&l<");
                    } else if (res.getChannels().contains(channel)) {
                        temp.append("\n&8[&9*&8] &b").append(channelName).append(" &7&l[&a&lEnabled&7&l]");
                    } else {
                        temp.append("\n&8[&9*&8] &b").append(channelName).append(" &7&l[&c&lDisabled&7&l]");
                    }
                }
            }
            MessageUtils.tell(p, temp.toString());
            return true;
        } else if (ChatChannel.isChannel(args[0])) {
            ChatChannel channel2 = ChatChannel.get(args[0].toUpperCase());
            if (!channel2.canChatM(p)) {
                return true;
            }
            channel2.setActiveChatChannel(p);
            MessageUtils.tell(p, "&7You have joined Chat Channel &6" + args[0]);
            return true;
        } else if (args[0].equalsIgnoreCase("swear")) {
            Resident res = ResidentManager.getInstance().getResident(p);
            res.setSwear(!res.getSwear());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
            MessageUtils.tell(p, "&7&lSwearing has been " + (res.getSwear() ? "&AEnabled" : "&cDisabled") + "&7!");
            return true;
        } else if (args[0].equalsIgnoreCase("lock")) {
            if (p.hasPermission("kingdoms.staff")) {
                if (Data.muteChat) {
                    Data.muteChat = false;
                    Bukkit.broadcastMessage(MessageUtils.replaceColorCodes("&c&lGeneral Chat has been un-muted by &6&l" + p.getDisplayName()));
                } else {
                    Data.muteChat = true;
                    Bukkit.broadcastMessage(MessageUtils.replaceColorCodes("&c&lGeneral Chat has been muted by &6&l" + p.getDisplayName()));
                }
            } else {
                MessageUtils.tell(p, getError("no-permission"));
            }
        } else if (args[0].equalsIgnoreCase("leave")) {
            Resident res = ResidentManager.getInstance().getResident(p);
            if (args.length < 2) {
                MessageUtils.tell(p, "&cPlease specify a channel you would like to leave!\n&c/" + alias + " leave &6<channel>");
                return true;
            }
            if (!ChatChannel.isChannel(args[1])) {
                MessageUtils.tell(p, "&cChat Channel &6" + args[1] + " is not valid channel!");
                return true;
            }
            ChatChannel channel3 = ChatChannel.valueOf(args[1].toUpperCase());
            if (res.getChannels().contains(channel3)) {
                MessageUtils.tell(p, "&7You have left Chat Channel &6" + args[1]);
                res.removeChannel(channel3);
            } else {
                MessageUtils.tell(p, "&cChat Channel &6" + args[1] + " is not valid channel!");
            }
            return true;
        }
        return false;
    }
}

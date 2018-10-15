package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatCommand extends SubCommand {
    private final String help;

    public ChatCommand() {
        super(null, "chat");
        registerCommand(new SubCommand(this, "leave <channel>") {
            @Override
            public boolean run(Player p, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }
                Resident res = ResidentManager.getInstance().getResident(p);
                ChatChannel channel = ChatChannel.get(args[0]);
                if (channel != null && res.getChannels().contains(channel)) {
                    MessageUtils.tell(p, getMessage("success"), "name", channel.name());
                    res.removeChannel(channel);
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                } else {
                    MessageUtils.tell(p, getError("not-a-channel"), "name", args[0]);
                }
                return true;
            }
        });
        registerCommand(new PermissionSubCommand(this, "lock", "kingdoms.staff") {
            @Override
            public boolean run(Player p, String base, String alias, String... args) {
                if (Data.muteChat) {
                    Data.muteChat = false;
                    Bukkit.broadcastMessage(getMessage("unmuted").getReplacedText("name", p.getDisplayName()));
                } else {
                    Data.muteChat = true;
                    Bukkit.broadcastMessage(getMessage("muted").getReplacedText("name", p.getDisplayName()));
                }
                return true;
            }
        });
        registerCommand(new SubCommand(this, "swear") {
            @Override
            public boolean run(Player p, String base, String alias, String... args) {
                Resident res = ResidentManager.getInstance().getResident(p);
                res.setSwear(!res.getSwear());
                Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                MessageUtils.tell(p, getMessage(res.getSwear() ? "enabled" : "disabled"));
                return false;
            }
        });
        help = "&8&l--------&r&c&l/Chat&r&8&l--------" +
                "\n&8[&9*&8] &b/{alias} <channel> &7- sets the active chat";
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        if (args.length < 1) {
            sendUsageMessage(p, this, base, alias);

            Resident res = ResidentManager.getInstance().getResident(p);
            for (ChatChannel channel : ChatChannel.values()) {
                if (channel.canChat(p)) {
                    String channelName = channel.getTag();
                    if (channel.equals(res.getActiveChannel())) {
                        MessageUtils.tell(p, getMessage("channel-entry.active"), "name", channelName);
                    } else if (res.getChannels().contains(channel)) {
                        MessageUtils.tell(p, getMessage("channel-entry.enabled"), "name", channelName);
                    } else {
                        MessageUtils.tell(p, getMessage("channel-entry.disabled"), "name", channelName);
                    }
                }
            }
            return true;
        } else if (ChatChannel.isChannel(args[0])) {
            ChatChannel channel = ChatChannel.get(args[0].toUpperCase());
            if (channel.canChatM(p)) {
                channel.setActiveChatChannel(p);
                MessageUtils.tell(p, getMessage("joined"), "name", channel.getTag());
            }
            return true;
        }
        return false;
    }

    private abstract class PermissionSubCommand extends SubCommand {
        public PermissionSubCommand(ChatCommand parent, String name, String permission) {
            super(parent, name);
            setPermission(permission);
        }
    }
}

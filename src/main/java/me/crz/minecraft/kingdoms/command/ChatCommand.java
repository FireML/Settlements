package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.chat.gui.ColorChannelSelectGui;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Data;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatCommand extends PlayerSubCommand {

    public ChatCommand() {
        super(null, "chat <channel>", "c");
        setHelpCommand(new HelpCommand(this));
        for (ChatChannel channel : ChatChannel.values()) {
            if (channel.isPrivate()) {
                continue;
            }
            registerCommand(new PlayerSubCommand(this, channel.name().toLowerCase(), channel.getAliases().toArray(new String[0])) {
                @Override
                public boolean run(Player p, String base, String alias, String... args) {
                    if (args.length == 0) {
                        if (channel.canChatM(p)) {
                            Resident res = ResidentManager.getInstance().getResident(p);
                            if ((res.setActiveChannel(channel) && !Utils.shouldResetChannelOnLogin()) | res.addChannel(channel)) {
                                Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                            }
                        }
                    } else {
                        ChatManager.getInstance().sendChannelMessage(p, channel, String.join(" ", args));
                    }
                    return true;
                }

                @Override
                public boolean isVisible() {
                    return false;
                }
            });
        }
        registerCommand(new PlayerSubCommand(this, "<channel>") {
            @Override
            public boolean run(Player p0, String p1, String p2, String... p3) {
                return false;
            }
        });
        registerCommand(new PlayerSubCommand(this, "leave <channel>") {
            @Override
            public boolean run(Player p, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }
                Resident res = ResidentManager.getInstance().getResident(p);
                ChatChannel channel = ChatChannel.get(args[0]);
                if (channel != null && res.getChannels().contains(channel)) {
                    res.removeChannel(channel);
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                } else {
                    MessageUtils.tell(p, getError("not-a-channel"), "name", args[0]);
                }
                return true;
            }
        });
        registerCommand(new PermissionPlayerSubCommand(this, "lock", "kingdoms.chat.lock") {
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
        registerCommand(new PlayerSubCommand(this, "swear") {
            @Override
            public boolean run(Player p, String base, String alias, String... args) {
                Resident res = ResidentManager.getInstance().getResident(p);
                res.setSwear(!res.getSwear());
                Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
                MessageUtils.tell(p, getMessage(res.getSwear() ? "enabled" : "disabled"));
                return false;
            }
        });
        registerCommand(new PlayerSubCommand(this, "colour", "color", "colours", "colors") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                ColorChannelSelectGui.getInstance().open(player);
                return true;
            }
        });
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        if (args.length < 1) {
            getHelpCommand().run(p, alias, "help");

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
            getMessage("footer").tell(p);
            return true;
        }
        return false;
    }

    private abstract class PermissionPlayerSubCommand extends PlayerSubCommand {
        public PermissionPlayerSubCommand(ChatCommand parent, String name, String permission) {
            super(parent, name);
            setPermission(permission);
        }
    }
}

package de.themoep.settlements.command;

import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatManager;
import de.themoep.settlements.chat.gui.ColorChannelSelectGui;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
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
                            boolean activeChanged = res.setActiveChannel(channel);
                            boolean save = activeChanged;
                            if (res.addChannel(channel)) {
                                save = true;
                            } else if (activeChanged) {
                                Message.get("command.chat.selected").tell(p, "name", channel.getTag());
                            } else {
                                Message.get("command.chat.already-joined").tell(p, "name", channel.getTag());
                            }

                            if (save && !Config.Chat.RESET_CHANNELS_ON_LOGIN) {
                                DatabaseManager.getInstance().saveResident(res);
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
                    DatabaseManager.getInstance().saveResident(res);
                } else {
                    MessageUtils.tell(p, getError("not-a-channel"), "name", args[0]);
                }
                return true;
            }
        });
        registerCommand(new PermissionPlayerSubCommand(this, "lock", "settlements.chat.lock") {
            @Override
            public boolean run(Player p, String base, String alias, String... args) {
                if (ConfigManager.getInstance().getDataConfig().getConfig().getBoolean("chat-lock")) {
                    ConfigManager.getInstance().getDataConfig().getConfig().set("chat-lock", false);
                    Bukkit.broadcastMessage(getMessage("unmuted").getReplacedText("name", p.getDisplayName()));
                } else {
                    ConfigManager.getInstance().getDataConfig().getConfig().set("chat-lock", true);
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
                DatabaseManager.getInstance().saveResident(res);
                MessageUtils.tell(p, getMessage(res.getSwear() ? "enabled" : "disabled"));
                return false;
            }
        });
        registerCommand(new ResidentSubCommand(this, "colour", "color", "colours", "colors") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                new ColorChannelSelectGui(resident).open();
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

    private abstract static class PermissionPlayerSubCommand extends PlayerSubCommand {
        public PermissionPlayerSubCommand(ChatCommand parent, String name, String permission) {
            super(parent, name);
            setPermission(permission);
        }
    }
}

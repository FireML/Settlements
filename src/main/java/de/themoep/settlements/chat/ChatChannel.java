package de.themoep.settlements.chat;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.NameTag;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ChatChannel implements NameTag {
    LOCAL("local", "l"),
    TRADE("trade", "t"),
    EVENT("event", "ev"),
    GENERAL("general", "g"),
    HELP("help", "h") {
        @Override
        public boolean autoJoin(Player player) {
            return player.hasPermission("kingdoms.autojoinchannel.help");
        }
    },
    SETTLEMENT("settlement", "s", "village") {
        @Override
        public boolean canChat(Player player) {
            return ResidentManager.getInstance().getResident(player).hasVillage();
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, Message.get("error.village-required"));
            return false;
        }
    },
    ALLY("ally", "a") {
        @Override
        public boolean canChat(Player player) {
            Resident resident = ResidentManager.getInstance().getResident(player);
            return resident.hasVillage() && !resident.getVillage().getAlliedVillageIds().isEmpty();
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, Message.get("error.no-allies"));
            return false;
        }
    },
    EMPIREALLY("eally", "ea", "alliance") {
        @Override
        public boolean canChat(Player player) {
            Resident resident = ResidentManager.getInstance().getResident(player);
            return resident.hasVillage() && (!resident.getVillage().getAlliedEmpireIds().isEmpty() ||
                    resident.hasEmpire() && resident.getVillage().getEmpire().hasAllies());
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, Message.get("error.no-allies"));
            return false;
        }
    },
    EMPIRE("empire", "e") {
        @Override
        public boolean canChat(Player player) {
            return ResidentManager.getInstance().getResident(player).hasEmpire();
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, Message.get("error.empire-required"));
            return false;
        }
    },
    ADMIN("admin", "ac") {
        @Override
        public boolean canChat(Player player) {
            return player.hasPermission("kingdoms.channel.admin");
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, Message.get("error.no-channel-permission"), "channel", name());
            return false;
        }
    },
    STAFF("staff", "sc") {
        @Override
        public boolean canChat(Player player) {
            return player.hasPermission("kingdoms.channel.staff");
        }

        @Override
        public boolean canChatM(Player player) {
            if (this.canChat(player)) {
                return true;
            }
            MessageUtils.tell(player, Message.get("error.no-channel-permission"), "channel", name());
            return false;
        }
    },
    PRIVATE_SENT(true),
    PRIVATE_RECEIVED(true);

    private static Map<String, ChatChannel> BY_ALIAS = new HashMap<>();

    private List<String> aliases = new ArrayList<>();

    private boolean priv = false;

    static {
        for (ChatChannel channel : values()) {
            for (String alias : channel.getAliases()) {
                BY_ALIAS.put(alias.toLowerCase(), channel);
            }
        }
    }

    ChatChannel(String... aliases) {
        this.aliases = Arrays.asList(aliases);
    }

    ChatChannel(boolean priv) {
        this.priv = priv;
    }

    public boolean canChat(Player player) {
        return !isPrivate();
    }

    public boolean autoJoin(Player player) {
        return canChat(player);
    }

    public boolean canChatM(Player player) {
        return !isPrivate();
    }

    public List<String> getAliases() {
        return aliases;
    }

    public static ChatChannel get(String name) {
        ChatChannel channel = BY_ALIAS.get(name.toLowerCase());
        return channel != null ? channel : GENERAL;
    }

    public static boolean isChannel(String name) {
        return BY_ALIAS.containsKey(name.toLowerCase());
    }

    public boolean isPrivate() {
        return priv;
    }
}

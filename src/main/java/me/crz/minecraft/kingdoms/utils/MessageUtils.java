package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;

import java.util.List;

import me.crz.minecraft.kingdoms.Base;

public class MessageUtils extends Base {
    public enum Message {
        NO_PERMISSION,
        NO_PERMISSION_,
        ONLY_CONSOLE,
        ONLY_PLAYER,
        PLAYER_NOT_FOUND,
        HELP,
        MUST_LEAVE_CURRENT,
        NOT_IN_CURRENT,
        SUCCESS_CREATE,
        SUCCESS_DELETE,
        RECEIVE_INVITE,
        TOWN_NOT_FOUND,
        NO_INVITE,
        IS_FULL,
        SUCCESS_JOIN,
        SUCCESS_LEAVE,
        NAME_TAKEN,
        CANT_LEAVE_OWN,
        SUCCESS_INVITE,
        KICKED_PLAYER,
        NEW_RESIDENT,
        ALERT_LEAVE,
        SHOW_SETTLEMENT,
        SUCCESS_DEPOSIT,
        FAIL_DEPOSIT,
        LIST_DEPOSIT,
        UPGRADE,
        CANT_SET_THAT_PERSONS_RANK,
        INVALID_RANK,
        SUCCESS_SET_RANK,
        SET_RANK,
        INVALID_LEVEL,
        UPGRADE_SETTLEMENT,
        CANT_CLAIM_MORE,
        ALREADY_OWN_LAND,
        CANT_CLAIM_HERE,
        SUCCESS_CLAIM_LAND,
        CANT_BUILD_HERE,
        DEFAULT_ENTER,
        DEFAULT_LEAVE,
        SUCCESS_CHANGE_ENTER,
        SUCCESS_CHANGE_LEAVE,
        SUCCESS_CREATE_SALE_PLOT,
        SUCCESS_BUILD_STATE,
        NO_LAND_AROUND,
        NOT_IN_VILLAGE,
        UPGRADE_UNLOCK,
        ALERT_UPKEEP_UNCLAIMED,
        ALERT_UPKEEP_DELETED,
        ALERT_UPKEEP_CHARGED,
        ALERT_UPKEEP_CANT_PAY,
        BANK_WITHDRAW,
        RANK_REQUIRED,
        BANK_NOT_ENOUGH_MONEY,
        BANK_DEPOSIT,
        PLAYER_NOT_ENOUGH_MONEY,
        SUCCESS_CLAIM_OUTPOST,
        ERROR_MAX_OUTPOST_CHUNKS,
        SUCCESS_UNCLAIM_OUTPOST,
        OUTPOST_NOT_FOUND,
        VILLAGE_RENAMED,
        ERROR_VILLAGE_EXISTS,
        TELEPORT_DEPOSIT,
        TELEPORT_CLAIM_REMOVED,
        TELEPORT_CLAIM_SET,
        NOW_ALLIED_TO,
        ALLY_INVITE,
        ALLY_ACCEPT_INFO,
        BANISHED,
        UNBANISHED,
        SUCCESS_UNCLAIM_LAND,
        VILLAGE_HOME_SET, ALLY_REMOVED;

        private final String text;

        Message() {
            text = replaceColorCodes(ConfigManager.getInstance().getMessagesConfig().getString(getKey()));
        }

        private String getKey() {
            return name().toLowerCase().replace('_', '-');
        }

        public String getText() {
            return text;
        }

        public String getReplaced(String... replaced) {
            return replacePlaceholders(text, replaced);
        }
    }

    public MessageUtils() {
        super();
    }

    public static void tell(Player player, Message message, String... replacements) {
        tell(player, message.text, replacements);
    }

    public static void tell(Player player, String message, String... replacements) {
        if (replacements != null && replacements.length > 0) {
            message = replacePlaceholders(message, replacements);
        }
        message = replaceColorCodes(message);
        player.sendMessage(message);
    }

    public static void tellResidents(List<Resident> residents, Message message, String... replacements) {
        for (Resident res : residents) {
            Player player = Bukkit.getPlayer(res.getUUID());
            if (player != null && player.isOnline()) {
                tell(player, message, replacements);
            }
        }
    }

    public static void tellListResidents(List<Resident> residents, List<String> message) {
        for (Resident res : residents) {
            Player player = Bukkit.getPlayer(res.getUUID());
            if (player != null && player.isOnline()) {
                tellList(player, message);
            }
        }
    }

    public static void tellList(Player player, List<String> messages) {
        if (messages == null) {
            return;
        }
        for (String message : messages) {
            player.sendMessage(replaceColorCodes(message));
        }
    }

    public static String replacePlaceholders(Message message, String... replacements) {
        return replacePlaceholders(message.text, replacements);
    }

    public static String replacePlaceholders(String message, String... replacements) {
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            String placeholder = replacements[i];
            if (placeholder != null && replacements[i + 1] != null) {
                if (!placeholder.startsWith("{") && !placeholder.endsWith("}")) {
                    placeholder = "{" + placeholder + "}";
                }
                message = message.replace(placeholder, replacements[i+1]);
            }
        }
        return message;
    }

    public static String replaceColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static BaseComponent[] toColoredComponents(String message) {
        return TextComponent.fromLegacyText(replaceColorCodes(message));
    }

    public static void tellResidentsC(List<Resident> residents, String message, ChatChannel channel) {
        for (Resident res : residents) {
            Player op = Bukkit.getPlayer(res.getUUID());
            if (op != null && op.isOnline() && res.getChannels().contains(channel)) {
                tell(op, message);
            }
        }
    }
}

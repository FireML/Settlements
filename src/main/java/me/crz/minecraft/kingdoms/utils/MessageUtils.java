package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.List;

import me.crz.minecraft.kingdoms.Base;

public class MessageUtils extends Base {
    public static final String NO_PERMISSION_MESSAGE;
    public static final String NO_PERMISSION_INTERACT;
    public static final String ONLY_CONSOLE_MESSAGE;
    public static final String ONLY_PLAYER_MESSAGE;
    public static final String PLAYER_NOT_FOUND_MESSAGE;
    public static final List<String> HELP_MESSAGE;
    public static final String MUST_LEAVE_CURRENT_MESSAGE;
    public static final String NOT_IN_CURRENT_MESSAGE;
    public static final String SUCCESS_CREATE_MESSAGE;
    public static final String SUCCESS_DELETE_MESSAGE;
    public static final String RECEIVE_INVITE_MESSAGE;
    public static final String TOWN_NOT_FOUND_MESSAGE;
    public static final String NO_INVITE_MESSAGE;
    public static final String IS_FULL_MESSAGE;
    public static final String SUCCESS_JOIN_MESSAGE;
    public static final String SUCCESS_LEAVE_MESSAGE;
    public static final String NAME_TAKEN_MESSAGE;
    public static final String CANT_LEAVE_OWN_MESSAGE;
    public static final String SUCCESS_INVITE_MESSAGE;
    public static final String KICKED_PLAYER_MESSAGE;
    public static final String NEW_RESIDENT_MESSAGE;
    public static final String ALERT_LEAVE_MESSAGE;
    public static final List<String> SHOW_SETTLEMENT_MESSAGE;
    public static final String SUCCESS_DEPOSIT_MESSAGE;
    public static final String FAIL_DEPOSIT_MESSAGE;
    public static final String LIST_DEPOSIT_MESSAGE;
    public static final String UPGRADE_MESSAGE;
    public static final String CANT_SET_THAT_PERSONS_RANK_MESSAGE;
    public static final String INVALID_RANK_MESSAGE;
    public static final String SUCCESS_SET_RANK_MESSAGE;
    public static final String INVALID_LEVEL_MESSAGE;
    public static final String UPGRADE_SETTLEMENT_MESSAGE;
    public static final String CANT_CLAIM_MORE_MESSAGE;
    public static final String ALREADY_OWN_LAND_MESSAGE;
    public static final String CANT_CLAIM_HERE_MESSAGE;
    public static final String SUCCESS_CLAIM_LAND_MESSAGE;
    public static final String CANT_BUILD_HERE_MESSAGE;
    public static final String DEFAULT_ENTER_MESSAGE;
    public static final String DEFAULT_LEAVE_MESSAGE;
    public static final String SUCCESS_CHANGE_ENTER_MESSAGE;
    public static final String SUCCESS_CHANGE_LEAVE_MESSAGE;
    public static final String SUCCESS_CREATE_SALE_PLOT_MESSAGE;
    public static final String SUCCESS_BUILD_STATE_MESSAGE;
    public static final String NO_LAND_AROUND_MESSAGE;

    static {
        NO_PERMISSION_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("NO-PERMISSION-MESSAGE");
        NO_PERMISSION_INTERACT = ConfigManager.getInstance().getMessagesConfig().getString("NO-PERMISSION-INTERACT");
        ONLY_CONSOLE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("ONLY-CONSOLE-MESSAGE");
        ONLY_PLAYER_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("ONLY-PLAYER-MESSAGE");
        PLAYER_NOT_FOUND_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("PLAYER-NOT-FOUND-MESSAGE");
        HELP_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getStringList("HELP-MESSAGE");
        MUST_LEAVE_CURRENT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("MUST-LEAVE-CURRENT-MESSAGE");
        NOT_IN_CURRENT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("NOT-IN-CURRENT-MESSAGE");
        SUCCESS_CREATE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-CREATE-MESSAGE");
        SUCCESS_DELETE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-DELETE-MESSAGE");
        RECEIVE_INVITE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("RECEIVE-INVITE-MESSAGE");
        TOWN_NOT_FOUND_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("TOWN-NOT-FOUND-MESSAGE");
        NO_INVITE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("NO-INVITE-MESSAGE");
        IS_FULL_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("IS-FULL-MESSAGE");
        SUCCESS_JOIN_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-JOIN-MESSAGE");
        SUCCESS_LEAVE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-LEAVE-MESSAGE");
        NAME_TAKEN_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("NAME-TAKEN-MESSAGE");
        CANT_LEAVE_OWN_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("CANT-LEAVE-OWN-MESSAGE");
        SUCCESS_INVITE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-INVITE-MESSAGE");
        KICKED_PLAYER_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("KICKED-PLAYER-MESSAGE");
        NEW_RESIDENT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("NEW-RESIDENT-MESSAGE");
        ALERT_LEAVE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("ALERT-LEAVE-MESSAGE");
        SHOW_SETTLEMENT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getStringList("SHOW-SETTLEMENT-MESSAGE");
        SUCCESS_DEPOSIT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-DEPOSIT-MESSAGE");
        FAIL_DEPOSIT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("FAIL-DEPOSIT-MESSAGE");
        LIST_DEPOSIT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("LIST-DEPOSIT-MESSAGE");
        UPGRADE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("UPGRADE-MESSAGE");
        CANT_SET_THAT_PERSONS_RANK_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("CANT-SET-THAT-PERSONS-RANK-MESSAGE");
        INVALID_RANK_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("INVALID-RANK-MESSAGE");
        SUCCESS_SET_RANK_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-SET-RANK-MESSAGE");
        INVALID_LEVEL_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("INVALID-LEVEL-MESSAGE");
        UPGRADE_SETTLEMENT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("UPGRADE-SETTLEMENT-MESSAGE");
        CANT_CLAIM_MORE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("CANT-CLAIM-MORE-MESSAGE");
        ALREADY_OWN_LAND_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("ALREADY-OWN-LAND-MESSAGE");
        CANT_CLAIM_HERE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("CANT-CLAIM-HERE-MESSAGE");
        SUCCESS_CLAIM_LAND_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-CLAIM-LAND-MESSAGE");
        CANT_BUILD_HERE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("CANT-BUILD-HERE-MESSAGE");
        DEFAULT_ENTER_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("DEFAULT-ENTER-MESSAGE");
        DEFAULT_LEAVE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("DEFAULT-LEAVE-MESSAGE");
        SUCCESS_CHANGE_ENTER_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-CHANGE-ENTER-MESSAGE");
        SUCCESS_CHANGE_LEAVE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-CHANGE-LEAVE-MESSAGE");
        SUCCESS_CREATE_SALE_PLOT_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-CREATE-SALE-PLOT-MESSAGE");
        SUCCESS_BUILD_STATE_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("SUCCESS-BUILD-STATE-MESSAGE");
        NO_LAND_AROUND_MESSAGE = ConfigManager.getInstance().getMessagesConfig().getString("NO-LAND-AROUND-MESSAGE");
    }

    public MessageUtils() {
        super();
    }

    public static void tell(final Player player, final String message) {
        tell(player, message, null, null);
    }

    public static void tell(final Player player, String message, final String placeholder, final String value) {
        if (placeholder != null && value != null) {
            message = replacePlaceholders(message, placeholder, value);
        }
        message = replaceColorCodes(message);
        player.sendMessage(message);
    }

    public static void tellResidents(final List<Resident> residents, final String message) {
        tellResidents(residents, message, null, null);
    }

    public static void tellResidents(final List<Resident> residents, final String message, final String placeholder, final String value) {
        for (final Resident res : residents) {
            final Player player = Bukkit.getPlayer(res.getUUID());
            if (player != null) {
                if (!player.isOnline()) {
                    continue;
                }
                tell(player, message, placeholder, value);
            }
        }
    }

    public static void tellListResidents(final List<Resident> residents, final List<String> message) {
        for (final Resident res : residents) {
            final Player player = Bukkit.getPlayer(res.getUUID());
            if (player != null) {
                if (!player.isOnline()) {
                    continue;
                }
                tellList(player, message);
            }
        }
    }

    public static void tellList(final Player player, final List<String> messages) {
        if (messages == null) {
            return;
        }
        for (String message : messages) {
            message = replaceColorCodes(message);
            player.sendMessage(message);
        }
    }

    public static String replacePlaceholders(String message, final String placeholder, final String value) {
        return message.replace(placeholder, value);
    }

    public static String replaceColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void tellResidentsC(final List<Resident> residents, final String message, final ChatChannel channel) {
        for (final Resident res : residents) {
            final Player op = Bukkit.getPlayer(res.getUUID());
            if (op != null) {
                if (!op.isOnline()) {
                    continue;
                }
                if (!Base.getResidentManager().isListening(op, channel)) {
                    continue;
                }
                tell(op, message);
            }
        }
    }
}

package me.crz.minecraft.kingdoms.utils;

import de.themoep.minedown.Replacer;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;

import java.util.List;

import me.crz.minecraft.kingdoms.Base;

public class MessageUtils extends Base {

    public MessageUtils() {
        super();
    }

    public static void tell(Player player, Message message, String... replacements) {
        tell(player, message.getComponents(), replacements);
    }

    public static void tell(Player player, BaseComponent[] message, String... replacements) {
        if (replacements != null && replacements.length > 0) {
            message = replacePlaceholders(message, replacements);
        }
        player.spigot().sendMessage(message);
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

    public static BaseComponent[] replacePlaceholders(BaseComponent[] message, String... replacements) {
        return new Replacer()
                .placeholderPrefix('{')
                .placeholderSuffix('}')
                .replace(replacements)
                .replaceIn(message);
    }

    public static String replacePlaceholders(String message, String... replacements) {
        return new Replacer()
                .placeholderPrefix('{')
                .placeholderSuffix('}')
                .replace(replacements)
                .replaceIn(message);
    }

    public static String replaceColorCodes(String message) {
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
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

    public static String invertColors(String text) {
        String colorTwo = ChatColor.getLastColors(text);
        String replacedText = text.replace(colorTwo, "{colorTwo}");
        String colorOne = ChatColor.getLastColors(replacedText);
        return replacedText.replace(colorOne, colorTwo).replace("{colorTwo}", colorOne);
    }
}

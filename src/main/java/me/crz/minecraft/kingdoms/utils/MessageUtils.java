package me.crz.minecraft.kingdoms.utils;

import com.google.common.collect.ImmutableMap;
import de.themoep.minedown.Replacer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import me.crz.minecraft.kingdoms.Base;

public class MessageUtils extends Base {

    public MessageUtils() {
        super();
    }

    public static void tell(CommandSender sender, Message message, String... replacements) {
        tell(sender, message.getComponents(), replacements);
    }

    public static void tell(Resident resident, Message message, String... replacements) {
        tell(resident.getPlayer(), message.getComponents(), replacements);
    }

    public static void tell(CommandSender sender, BaseComponent[] message, String... replacements) {
        if (sender == null || message.length == 0 || TextComponent.toPlainText(message).isEmpty()) {
            return;
        }
        if (replacements != null && replacements.length > 0) {
            message = replacePlaceholders(message, replacements);
        }
        sender.spigot().sendMessage(message);
    }

    public static void tell(CommandSender sender, Message message, Map<String,BaseComponent[]> replacements) {
        tell(sender, message.getComponents(), replacements);
    }

    public static void tell(CommandSender sender, BaseComponent[] message, String placeholder, BaseComponent[] value) {
        tell(sender, message, ImmutableMap.of(placeholder, value));
    }

    public static void tell(CommandSender sender, BaseComponent[] message, Map<String,BaseComponent[]> replacements) {
        if (sender == null || message.length == 0 || TextComponent.toPlainText(message).isEmpty()) {
            return;
        }
        if (replacements != null && !replacements.isEmpty()) {
            message = replacePlaceholders(message, replacements);
        }
        sender.spigot().sendMessage(message);
    }

    public static void tell(Resident player, String message, String... replacements) {
        tell(player.getPlayer(), message, replacements);
    }

    public static void tell(CommandSender sender, String message, String... replacements) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        if (replacements != null && replacements.length > 0) {
            message = replacePlaceholders(message, replacements);
        }
        message = replaceColorCodes(message);
        sender.sendMessage(message);
    }

    public static void tellResidents(Collection<Resident> residents, Message message, String... replacements) {
        BaseComponent[] components = message.getReplacedComponents(replacements);
        for (Resident res : residents) {
            tell(res.getPlayer(), components);
        }
    }

    public static void tellListResidents(Collection<Resident> residents, List<String> message) {
        for (Resident res : residents) {
            tellList(res.getPlayer(), message);
        }
    }

    public static void tellList(CommandSender sender, List<String> messages) {
        if (sender == null || messages == null) {
            return;
        }
        for (String message : messages) {
            sender.sendMessage(replaceColorCodes(message));
        }
    }

    public static BaseComponent[] replacePlaceholders(BaseComponent[] message, String... replacements) {
        if (replacements.length == 0) {
            return message;
        }
        return new Replacer()
                .placeholderPrefix("{")
                .placeholderSuffix("}")
                .replace(replacements)
                .replaceIn(message);
    }

    public static BaseComponent[] replacePlaceholders(BaseComponent[] message, Map<String, BaseComponent[]> replacements) {
        if (replacements.isEmpty()) {
            return message;
        }
        Replacer replacer = new Replacer()
                .placeholderPrefix("{")
                .placeholderSuffix("}");
        replacer.componentReplacements().putAll(replacements);
        return replacer.replaceIn(message);
    }

    public static BaseComponent[] replacePlaceholders(BaseComponent[] message, String placeholder, BaseComponent[] value) {
        return new Replacer()
                .placeholderPrefix("{")
                .placeholderSuffix("}")
                .replace(placeholder, value)
                .replaceIn(message);
    }

    public static String replacePlaceholders(String message, String... replacements) {
        if (message == null) {
            return null;
        } else if (replacements.length == 0) {
            return message;
        }
        return new Replacer()
                .placeholderPrefix("{")
                .placeholderSuffix("}")
                .replace(replacements)
                .replaceIn(message);
    }

    public static String replacePlaceholders(String message, Map<String, String> replacements) {
        if (message == null) {
            return null;
        } else if (replacements.isEmpty()) {
            return message;
        }
        return new Replacer()
                .placeholderPrefix("{")
                .placeholderSuffix("}")
                .replace(replacements)
                .replaceIn(message);
    }

    public static String replaceColorCodes(String message) {
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : null;
    }

    public static BaseComponent[] toColoredComponents(String message) {
        return TextComponent.fromLegacyText(replaceColorCodes(message));
    }

    public static String invertColors(String text) {
        String colorTwo = ChatColor.getLastColors(text);
        String replacedText = text.replace(colorTwo, "{colorTwo}");
        String colorOne = ChatColor.getLastColors(replacedText);
        return replacedText.replace(colorOne, colorTwo).replace("{colorTwo}", colorOne);
    }

    public static String yesOrNo(boolean b) {
        return b ? "Yes" : "No";
    }

    public static void broadcast(Message message, String... replacements) {
        BaseComponent[] components = message.getReplacedComponents(replacements);
        for (Player player : Bukkit.getOnlinePlayers()) {
            tell(player, components);
        }
    }

    public static void tellAdvanced(Player player, String key, ImmutableMap<String, BaseComponent[]> replacements) {
        tell(player, Message.get(key + ".message"), replacements);
        new TitleBuilder()
                .withTitle(TextComponent.toLegacyText(Message.get(key + ".title").getReplacedComponents(replacements)))
                .withSubtitle(TextComponent.toLegacyText(Message.get(key + ".subtitle").getReplacedComponents(replacements)))
                .withActionbar(TextComponent.toLegacyText(Message.get(key + ".actionbar").getReplacedComponents(replacements)))
                .send(player);
    }

    public static void tellAdvanced(Player player, String key, String... replacements) {
        tell(player, Message.get(key + ".message"), replacements);
        new TitleBuilder()
                .withTitle(Message.get(key + ".title").getText())
                .withSubtitle(Message.get(key + ".subtitle").getText())
                .withActionbar(Message.get(key + ".actionbar").getText())
                .send(player, replacements);
    }

    public static boolean isContainedIn(String string, BaseComponent... components) {
        for (BaseComponent component : components) {
            if (component instanceof TextComponent && ((TextComponent) component).getText().toLowerCase().contains(string.toLowerCase())) {
                return true;
            }
            if (component.getExtra() != null && isContainedIn(string, component.getExtra().toArray(new BaseComponent[0]))) {
                return true;
            }
        }
        return false;
    }
}

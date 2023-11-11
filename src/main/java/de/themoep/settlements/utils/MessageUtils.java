package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.libs.themoep.minedown.adventure.Replacer;
import uk.firedev.poleislib.libs.zrips.cmilib.Colors.CMIChatColor;
import uk.firedev.poleislib.utils.ComponentUtils;
import uk.firedev.poleislib.utils.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MessageUtils {

    public static void tell(CommandSender sender, Message message, String... replacements) {
        tell(sender, message.getText(), replacements);
    }

    public static void tell(Resident resident, Message message, String... replacements) {
        tell(resident.getPlayer(), message.getText(), replacements);
    }

    public static void tell(Resident player, String message, String... replacements) {
        tell(player.getPlayer(), message, replacements);
    }

    public static void tell(CommandSender sender, Component message, String... replacements) {
        sender.sendMessage(ComponentUtils.parsePlaceholders(message, replacements));
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
        String send = StringUtils.parsePlaceholders(message.getText(), replacements);
        for (Resident res : residents) {
            tell(res.getPlayer(), send);
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

    public static void sendActionBar(List<Resident> residents, Message message, String... replacements) {
        for (Resident resident : residents) {
            sendActionBar(resident, message, replacements);
        }
    }

    public static void sendActionBar(Resident resident, Message message, String... replacements) {
        sendActionBar(resident.getPlayer(), message, replacements);
    }

    public static void sendActionBar(Player player, Message message, String... replacements) {
        if (player == null || message == null || !message.exists()) {
            return;
        }

        player.sendActionBar(message.getReplacedComponent(replacements));
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
        return CMIChatColor.colorize(message);
    }

    public static String yesOrNo(boolean b) {
        return b ? "Yes" : "No";
    }

    public static void notify(LandOwner sender, List<Resident> residents, Message message, String... replacements) {
        for (Resident resident : residents) {
            notify(sender, resident, message, replacements);
        }
    }

    public static void notify(LandOwner sender, Resident resident, Message message, String... replacements) {
        if (resident.getPlayer() != null) {
            message.tell(resident, replacements);
        } else {
            for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                provider.sendMail(sender.getStateTag() + " " + sender.getName(), resident, message.getReplacedText(replacements));
            }
        }
    }

    public static void tellAdvanced(Player player, String key, String... replacements) {
        tell(player, Message.get(key + ".message"), replacements);
        new TitleBuilder()
                .withTitle(Message.get(key + ".title"))
                .withSubtitle(Message.get(key + ".subtitle"))
                .withActionbar(Message.get(key + ".actionbar"))
                .send(player, replacements);
    }

    public static boolean isContainedIn(String string, String check) {
        return check.toLowerCase().contains(string.toLowerCase());
    }
}

package de.themoep.settlements.utils;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.firedev.daisylib.builders.TitleBuilder;

import java.util.Collection;
import java.util.List;

public class MessageUtils {

    public static void tell(Resident resident, Component message) {
        tell(resident.getPlayer(), message);
    }

    public static void tell(CommandSender sender, Component message) {
        sender.sendMessage(message);
    }

    public static void tellResidents(Collection<Resident> residents, Component message) {
        residents.forEach(resident -> resident.tell(message));
    }

    public static void tellListResidents(Collection<Resident> residents, List<String> message) {
        residents.forEach(resident -> tellList(resident.getPlayer(), message));
        for (Resident res : residents) {
            tellList(res.getPlayer(), message);
        }
    }

    public static void tellList(CommandSender sender, List<String> messages) {
        if (sender == null || messages == null) {
            return;
        }
        for (String message : messages) {
            tell(sender, ComponentUtils.parseComponent(message));
        }
    }

    public static void sendActionBar(List<Resident> residents, Component message) {
        for (Resident resident : residents) {
            sendActionBar(resident, message);
        }
    }

    public static void sendActionBar(Resident resident, Component message) {
        sendActionBar(resident.getPlayer(), message);
    }

    public static void sendActionBar(Player player, Component message) {
        if (player == null || message == null) {
            return;
        }

        player.sendActionBar(message);
    }

    public static String yesOrNo(boolean b) {
        return b ? "Yes" : "No";
    }

    public static void notify(LandOwner<?, ?> sender, List<Resident> residents, Message message, String... replacements) {
        for (Resident resident : residents) {
            notify(sender, resident, message, replacements);
        }
    }

    public static void notify(LandOwner<?, ?> sender, Resident resident, Message message, String... replacements) {
        if (resident.getPlayer() != null) {
            message.tell(resident, replacements);
        }
    }

    public static void tellAdvanced(Player player, String key, String... replacements) {
        String message = Message.get(key + ".message").getReplacedText(replacements);
        if (!message.isEmpty()) {
            tell(player, ComponentUtils.parseComponent(message));
        }
        TitleBuilder.create()
                .withTitle(Message.get(key + ".title").getReplacedComponent(replacements))
                .withSubtitle(Message.get(key + ".subtitle").getReplacedComponent(replacements))
                .send(player);
        ComponentUtils.sendActionBar(Message.get(key + ".actionbar").getReplacedComponent(replacements), player);
    }

    public static boolean isContainedIn(String string, String check) {
        return check.toLowerCase().contains(string.toLowerCase());
    }
}

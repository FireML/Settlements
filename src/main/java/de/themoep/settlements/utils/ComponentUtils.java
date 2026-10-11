package de.themoep.settlements.utils;

import de.themoep.minedown.adventure.MineDown;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatColorType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import uk.firedev.daisylib.messages.replacer.Replacer;

import java.util.HashMap;
import java.util.Map;

public class ComponentUtils {

    public static String toString(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static Component parseComponent(String text) {
        return MineDown.parse(text);
    }

    public static Component parseComponent(String text, String... replacements) {
        return MineDown.parse(text, replacements);
    }

    public static Component parseComponent(String text, Map<String, ?> replacements) {
        Replacer replacer = Replacer.replacer().addReplacements(replacements);
        return replacer.apply(MineDown.parse(text));
    }

    public static Component parsePlaceholders(Component component, Map<String, Component> replacements) {
        Replacer replacer = Replacer.replacer();
        replacements.forEach((string, replacement) -> {
            replacer.addReplacement("{" + string + "}", replacement);
        });
        return replacer.apply(component);
    }

    public static Component parsePlaceholders(Component component, String... replacements) {
        if (replacements.length % 2 != 0) {
            SettlementsPlugin.getInstance().getLogging().debug("Invalid replacement array size. Replacements will not be added.");
            return component;
        }
        Replacer replacer = Replacer.replacer();
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            replacer.addReplacement(replacements[i], replacements[i + 1]);
        }
        return replacer.apply(component);
    }

    public static Component parseColors(Component component) {
        String str = LegacyComponentSerializer.legacySection().serialize(component);
        str = ChatColor.translateAlternateColorCodes('&', str);
        return LegacyComponentSerializer.legacySection().deserialize(str);
    }

    public static void broadcastMessage(Component component) {
        Bukkit.broadcast(component);
    }

    public static void sendActionBar(Component component, Audience audience) {
        audience.sendActionBar(component);
    }

}

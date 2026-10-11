package de.themoep.settlements.utils;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import de.themoep.minedown.adventure.Replacer;

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
        return new Replacer()
            .placeholderPrefix("{")
            .placeholderSuffix("}")
            .replace(replacements)
            .replaceIn(MineDown.parse(text));
    }

    public static Component parsePlaceholders(Component component, Map<String, ?> replacements) {
        return new Replacer()
            .placeholderPrefix("{")
            .placeholderSuffix("}")
            .replace(replacements)
            .replaceIn(component);
    }

    public static Component parsePlaceholders(Component component, String... replacements) {
        return new Replacer()
            .placeholderPrefix("{")
            .placeholderSuffix("}")
            .replace(replacements)
            .replaceIn(component);
    }

    public static Component parseColors(Component component) {
        String str = LegacyComponentSerializer.legacySection().serialize(component);
        return MineDown.parse(str);
    }

    public static void broadcastMessage(Component component) {
        Bukkit.broadcast(component);
    }

    public static void sendActionBar(Component component, Audience audience) {
        audience.sendActionBar(component);
    }

}

package de.themoep.settlements.utils;

import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.Replacer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StringUtils {

    public static String stripColors(String string) {
        return PlainTextComponentSerializer.plainText().serialize(MineDown.parse(string));
    }

    public static String parseColors(String string) {
        return LegacyComponentSerializer.legacySection().serialize(MineDown.parse(string));
    }

    public static String parseColors(String string, String... replacements) {
        return LegacyComponentSerializer.legacySection().serialize(ComponentUtils.parseComponent(string, replacements));
    }

    public static String parsePlaceholders(String text, String... replacements) {
        return new Replacer()
            .placeholderPrefix("{")
            .placeholderSuffix("}")
            .replace(replacements)
            .replaceIn(text);
    }

    public static String parsePlaceholders(String text, Map<String, String> replacements) {
        return new Replacer()
            .placeholderPrefix("{")
            .placeholderSuffix("}")
            .replace(replacements)
            .replaceIn(text);
    }

}

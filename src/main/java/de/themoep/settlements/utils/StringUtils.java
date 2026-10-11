package de.themoep.settlements.utils;

import de.themoep.minedown.adventure.MineDown;
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
        return LegacyComponentSerializer.legacySection().serialize(MineDown.parse(string, replacements));
    }

    public static String parsePlaceholders(String text, String... replacements) {
        return LegacyComponentSerializer.legacySection().serialize(MineDown.parse(text, replacements));
    }

    public static String parsePlaceholders(String text, Map<String, String> replacements) {
        List<String> replacementList = new ArrayList<>();
        replacements.forEach((key, value) -> {
            replacementList.add(key);
            replacementList.add(value);
        });
        return LegacyComponentSerializer.legacySection().serialize(MineDown.parse(text, replacementList.toArray(String[]::new)));
    }

}

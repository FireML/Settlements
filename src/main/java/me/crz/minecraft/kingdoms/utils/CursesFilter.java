package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.config.ConfigAccessor;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class CursesFilter {
    private static CursesFilter instance;
    private final Set<String> literalCurses = new HashSet<>();
    private final Set<Pattern> regexCurses = new HashSet<>();
    private String replaceWith = "cookie";

    public CursesFilter(FileConfiguration config) {

        replaceWith = config.getString("replace-with");

        literalCurses.addAll(config.getStringList("literal"));
        for (String regex : config.getStringList("regexes")) {
            try {
                regexCurses.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } catch (PatternSyntaxException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, regex + " is not a valid curse regex pattern!");
            }
        }
    }

    public String filterCurses(final String message) {
        return Arrays.stream(message.split(" "))
                .map(s -> {
                    if (literalCurses.contains(s.toLowerCase())) {
                        return replaceWith;
                    }
                    for (Pattern pattern : regexCurses) {
                        if (pattern.matcher(s).matches()) {
                            return replaceWith;
                        }
                    }
                    return s;
                })
                .collect(Collectors.joining(" "));
    }

    public boolean containsCurses(String message) {
        for (String literalCurse : literalCurses) {
            if (message.toLowerCase().contains(literalCurse)) {
                return true;
            }
        }
        for (Pattern pattern : regexCurses) {
            if (pattern.matcher(message).matches()) {
                return true;
            }
        }
        return false;
    }
}

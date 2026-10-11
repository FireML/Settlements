package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsPlugin;
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
    private String replaceWith;

    public CursesFilter(FileConfiguration config) {

        replaceWith = config.getString("replace-with");
        if (replaceWith == null) {
            replaceWith = "cookie";
        }

        literalCurses.addAll(config.getStringList("literal"));
        for (String regex : config.getStringList("regexes")) {
            try {
                regexCurses.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } catch (PatternSyntaxException e) {
                SettlementsPlugin.getInstance().getLogging().warn(regex + " is not a valid curse regex pattern!");
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

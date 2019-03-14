package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class UtilChat {
    private static UtilChat instance;
    private final Set<String> literalCurses = new HashSet<>();
    private final Set<Pattern> regexCurses = new HashSet<>();
    private String replaceWith = "cookie";

    public UtilChat() {
        load();
    }

    public static UtilChat getInstance() {
        if (instance == null) {
            instance = new UtilChat();
        }
        return instance;
    }

    public void load() {
        replaceWith = ConfigManager.getInstance().getCursesConfig().getString("replace-with");

        literalCurses.addAll(ConfigManager.getInstance().getCursesConfig().getStringList("literal"));
        for (String regex : ConfigManager.getInstance().getCursesConfig().getStringList("regexes")) {
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

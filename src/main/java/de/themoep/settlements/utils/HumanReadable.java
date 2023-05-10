package de.themoep.settlements.utils;

import org.apache.commons.text.WordUtils;

public interface HumanReadable {
    default String humanizedName() {
        return humanizedName(name());
    }

    static String humanizedName(Enum e) {
        return humanizedName(e.name());
    }

    static String humanizedName(String string) {
        return WordUtils.capitalizeFully(string
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
        );
    }

    public String name();
}

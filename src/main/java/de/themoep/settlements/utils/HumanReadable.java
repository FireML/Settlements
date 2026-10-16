package de.themoep.settlements.utils;

import org.apache.commons.lang3.text.WordUtils;

public interface HumanReadable {
    default String humanizedName() {
        return humanizedName(name());
    }

    static String humanizedName(Enum<?> e) {
        return humanizedName(e.name());
    }

    static String humanizedName(String string) {
        return WordUtils.capitalizeFully(string
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
        );
    }

    String name();
}

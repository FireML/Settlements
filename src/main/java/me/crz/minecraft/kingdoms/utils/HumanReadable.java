package me.crz.minecraft.kingdoms.utils;

import org.apache.commons.lang.WordUtils;

public interface HumanReadable {
    default String humanizedName() {
        return humanizedName(name());
    }

    static String humanizedName(Enum e) {
        return humanizedName(e.name());
    }

    static String humanizedName(String string) {
        return WordUtils.capitalizeFully(string.replace('_', ' '));
    }

    public String name();
}

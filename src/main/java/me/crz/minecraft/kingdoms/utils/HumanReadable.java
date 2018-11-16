package me.crz.minecraft.kingdoms.utils;

import org.apache.commons.lang.WordUtils;

public interface HumanReadable {
    default String humanizedName() {
        return WordUtils.capitalizeFully(name().replace('_', ' '));
    }

    static String humanizedName(Enum e) {
        return WordUtils.capitalizeFully(e.name().replace('_', ' '));
    }

    public String name();
}

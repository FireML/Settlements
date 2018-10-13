package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.utils.NameTag;

public enum BuildState implements NameTag {
    RESIDENTS,
    OFFICERS,
    ALLY,
    PUBLIC;

    public static BuildState next(final BuildState current) {
        if (current == RESIDENTS) {
            return OFFICERS;
        }
        if (current == OFFICERS) {
            return ALLY;
        }
        if (current == ALLY) {
            return PUBLIC;
        }
        return RESIDENTS;
    }
}

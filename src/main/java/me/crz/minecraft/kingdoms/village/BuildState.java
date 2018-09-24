package me.crz.minecraft.kingdoms.village;

public enum BuildState {
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

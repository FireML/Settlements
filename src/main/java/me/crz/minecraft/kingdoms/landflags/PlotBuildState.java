package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.utils.NameTag;

public enum PlotBuildState implements NameTag {
    RESIDENTS,
    OFFICERS,
    ALLY,
    PUBLIC,
    PRIVATE,
    FRIENDS;

    public static PlotBuildState next(final PlotBuildState current) {
        switch (current) {
            case ALLY:
                return PUBLIC;
            case PUBLIC:
                return PRIVATE;
            case PRIVATE:
                return FRIENDS;
            case FRIENDS:
                return RESIDENTS;
            case RESIDENTS:
                return OFFICERS;
            case OFFICERS:
                return ALLY;
        }
        return RESIDENTS;
    }
}

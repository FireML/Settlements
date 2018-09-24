package me.crz.minecraft.kingdoms.landflags;

public enum PlotBuildState
{
    RESIDENTS("RESIDENTS", 0), 
    OFFICERS("OFFICERS", 1), 
    ALLY("ALLY", 2), 
    PUBLIC("PUBLIC", 3), 
    PRIVATE("PRIVATE", 4), 
    FRIENDS("FRIENDS", 5);
    
    private static final /* synthetic */ PlotBuildState[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new PlotBuildState[] { RESIDENTS, OFFICERS, ALLY, PUBLIC, PRIVATE, FRIENDS };
    }
    
    private PlotBuildState(final String s, final int n) {
    }
    
    public static PlotBuildState next(final PlotBuildState current) {
        if (current == RESIDENTS) {
            return OFFICERS;
        }
        if (current == OFFICERS) {
            return ALLY;
        }
        if (current == ALLY) {
            return PUBLIC;
        }
        if (current == PUBLIC) {
            return PRIVATE;
        }
        if (current == PRIVATE) {
            return FRIENDS;
        }
        return RESIDENTS;
    }
    
    public static PlotBuildState[] values() {
        final PlotBuildState[] enum$VALUES = ENUM$VALUES;
        final int length;
        final PlotBuildState[] array;
        System.arraycopy(enum$VALUES, 0, array = new PlotBuildState[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static PlotBuildState valueOf(final String s) {
        return Enum.<PlotBuildState>valueOf(PlotBuildState.class, s);
    }
}

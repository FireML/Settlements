package me.crz.minecraft.kingdoms.village;

public enum BuildState
{
    RESIDENTS("RESIDENTS", 0), 
    OFFICERS("OFFICERS", 1), 
    ALLY("ALLY", 2), 
    PUBLIC("PUBLIC", 3);
    
    private static final /* synthetic */ BuildState[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new BuildState[] { RESIDENTS, OFFICERS, ALLY, PUBLIC };
    }
    
    private BuildState(final String s, final int n) {
    }
    
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
    
    public static BuildState[] values() {
        final BuildState[] enum$VALUES = ENUM$VALUES;
        final int length;
        final BuildState[] array;
        System.arraycopy(enum$VALUES, 0, array = new BuildState[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static BuildState valueOf(final String s) {
        return Enum.<BuildState>valueOf(BuildState.class, s);
    }
}

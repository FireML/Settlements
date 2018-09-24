package me.crz.minecraft.kingdoms.village;

public enum VillageState
{
    VILLAGE("VILLAGE", 0), 
    KINGDOM("KINGDOM", 1);
    
    private static final /* synthetic */ VillageState[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new VillageState[] { VILLAGE, KINGDOM };
    }
    
    private VillageState(final String s, final int n) {
    }
    
    public static VillageState[] values() {
        final VillageState[] enum$VALUES = ENUM$VALUES;
        final int length;
        final VillageState[] array;
        System.arraycopy(enum$VALUES, 0, array = new VillageState[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static VillageState valueOf(final String s) {
        return Enum.<VillageState>valueOf(VillageState.class, s);
    }
}

package me.crz.minecraft.kingdoms.village;

public enum UpkeepStage
{
    PAID("PAID", 0), 
    LAND("LAND", 1);
    
    private static final /* synthetic */ UpkeepStage[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new UpkeepStage[] { PAID, LAND };
    }
    
    private UpkeepStage(final String s, final int n) {
    }
    
    public static UpkeepStage[] values() {
        final UpkeepStage[] enum$VALUES = ENUM$VALUES;
        final int length;
        final UpkeepStage[] array;
        System.arraycopy(enum$VALUES, 0, array = new UpkeepStage[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static UpkeepStage valueOf(final String s) {
        return Enum.<UpkeepStage>valueOf(UpkeepStage.class, s);
    }
}

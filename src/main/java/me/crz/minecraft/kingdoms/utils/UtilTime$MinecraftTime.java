package me.crz.minecraft.kingdoms.utils;

public enum MinecraftTime
{
    SUNRISE("SUNRISE", 0, 23000), 
    DAY("DAY", 1, 6000), 
    SUNSET("SUNSET", 2, 13100), 
    NIGHT("NIGHT", 3, 18000);
    
    private int ticks;
    private static final /* synthetic */ MinecraftTime[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new MinecraftTime[] { SUNRISE, DAY, SUNSET, NIGHT };
    }
    
    private MinecraftTime(final String s, final int n, final int ticks) {
        this.ticks = ticks;
    }
    
    public int getTicks() {
        return ticks;
    }
    
    public static MinecraftTime fromString(final String name) {
        MinecraftTime[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final MinecraftTime minecraftTime = values[i];
            if (minecraftTime.name().equalsIgnoreCase(name)) {
                return minecraftTime;
            }
        }
        return null;
    }
    
    public static boolean exists(final String name) {
        return fromString(name) != null;
    }
    
    public static MinecraftTime[] values() {
        final MinecraftTime[] enum$VALUES = ENUM$VALUES;
        final int length;
        final MinecraftTime[] array;
        System.arraycopy(enum$VALUES, 0, array = new MinecraftTime[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static MinecraftTime valueOf(final String s) {
        return Enum.<MinecraftTime>valueOf(MinecraftTime.class, s);
    }
}

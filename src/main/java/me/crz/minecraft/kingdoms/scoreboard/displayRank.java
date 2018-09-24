package me.crz.minecraft.kingdoms.scoreboard;

enum displayRank
{
    citizen("citizen", 0), 
    merchant("merchant", 1), 
    merchantP("merchantP", 2), 
    baron("baron", 3), 
    baronP("baronP", 4), 
    elder("elder", 5), 
    elderP("elderP", 6), 
    pup("pup", 7), 
    pupP("pupP", 8), 
    scout("scout", 9), 
    scoutP("scoutP", 10), 
    hunter("hunter", 11), 
    hunterP("hunterP", 12), 
    alpha("alpha", 13), 
    alphaP("alphaP", 14), 
    fenrir("fenrir", 15), 
    fenrirP("fenrirP", 16), 
    helper("helper", 17), 
    mod("mod", 18), 
    srmod("srmod", 19), 
    builder("builder", 20), 
    dev("dev", 21), 
    admin("admin", 22), 
    owner("owner", 23);
    
    private static final /* synthetic */ displayRank[] ENUM$VALUES;
    
    static {
        ENUM$VALUES = new displayRank[] { citizen, merchant, merchantP, baron, baronP, elder, elderP, pup, pupP, scout, scoutP, hunter, hunterP, alpha, alphaP, fenrir, fenrirP, helper, mod, srmod, builder, dev, admin, owner };
    }
    
    private displayRank(final String s, final int n) {
    }
    
    public static displayRank get(final String check) {
        displayRank[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final displayRank disp = values[i];
            if (disp.name().equalsIgnoreCase(check.trim())) {
                return disp;
            }
        }
        return citizen;
    }
    
    public static displayRank[] values() {
        final displayRank[] enum$VALUES = ENUM$VALUES;
        final int length;
        final displayRank[] array;
        System.arraycopy(enum$VALUES, 0, array = new displayRank[length = enum$VALUES.length], 0, length);
        return array;
    }
    
    public static displayRank valueOf(final String s) {
        return Enum.<displayRank>valueOf(displayRank.class, s);
    }
}

package me.crz.minecraft.kingdoms.scoreboard;

enum DisplayRank {
    citizen,
    merchant,
    merchantP,
    baron,
    baronP,
    elder,
    elderP,
    pup,
    pupP,
    scout,
    scoutP,
    hunter,
    hunterP,
    alpha,
    alphaP,
    fenrir,
    fenrirP,
    helper,
    mod,
    srmod,
    builder,
    dev,
    admin,
    owner;

    public static DisplayRank get(final String check) {
        DisplayRank[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final DisplayRank disp = values[i];
            if (disp.name().equalsIgnoreCase(check.trim())) {
                return disp;
            }
        }
        return citizen;
    }
}

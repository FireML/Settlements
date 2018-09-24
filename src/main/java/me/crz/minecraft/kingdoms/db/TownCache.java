package me.crz.minecraft.kingdoms.db;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;

public class TownCache {
    private static HashMap<UUID, String> pendingTownInvites;
    private static HashMap<String, List<String>> pendingTownAlly;

    static {
        pendingTownInvites = new HashMap<>();
        pendingTownAlly = new HashMap<>();
    }

    public TownCache() {
        super();
    }

    public static HashMap<UUID, String> getPendingTownInvites() {
        return pendingTownInvites;
    }

    public static HashMap<String, List<String>> getPendingTownAlly() {
        return pendingTownAlly;
    }
}

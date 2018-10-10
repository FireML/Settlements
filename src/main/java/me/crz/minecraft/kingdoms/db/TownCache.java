package me.crz.minecraft.kingdoms.db;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;

public class TownCache {
    private static Multimap<UUID, String> pendingTownInvites = MultimapBuilder.hashKeys().hashSetValues().build();
    private static Multimap<String, String> pendingTownAlly = MultimapBuilder.hashKeys().hashSetValues().build();

    public TownCache() {
        super();
    }

    public static Multimap<UUID, String> getPendingTownInvites() {
        return pendingTownInvites;
    }

    public static Multimap<String, String> getPendingTownAlly() {
        return pendingTownAlly;
    }
}

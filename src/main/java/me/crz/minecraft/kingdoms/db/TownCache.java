package me.crz.minecraft.kingdoms.db;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.utils.IdObject;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;

public class TownCache {
    private static Multimap<UUID, IdObject> pendingTownInvites = MultimapBuilder.hashKeys().hashSetValues().build();
    private static Multimap<IdObject, IdObject> pendingTownAlly = MultimapBuilder.hashKeys().hashSetValues().build();

    public TownCache() {
        super();
    }

    public static Multimap<UUID, IdObject> getPendingTownInvites() {
        return pendingTownInvites;
    }

    public static Multimap<IdObject, IdObject> getPendingTownAlly() {
        return pendingTownAlly;
    }
}

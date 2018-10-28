package me.crz.minecraft.kingdoms.db;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.utils.IdObject;

import java.util.UUID;

public class InviteCache {
    private static Multimap<UUID, IdObject> pendingVillageInvites = MultimapBuilder.hashKeys().hashSetValues().build();
    private static Multimap<IdObject, IdObject> pendingTownAlly = MultimapBuilder.hashKeys().hashSetValues().build();
    private static Multimap<IdObject, IdObject> pendingEmpireInvites = MultimapBuilder.hashKeys().hashSetValues().build();

    public InviteCache() {
        super();
    }

    public static Multimap<UUID, IdObject> getPendingVillageInvites() {
        return pendingVillageInvites;
    }

    public static Multimap<IdObject, IdObject> getPendingEmpireInvites() {
        return pendingEmpireInvites;
    }

    public static Multimap<IdObject, IdObject> getPendingTownAlly() {
        return pendingTownAlly;
    }
}

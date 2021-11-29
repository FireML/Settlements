package de.themoep.settlements.db;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.utils.IdObject;

import java.util.UUID;

public class InviteCache {
    private static Multimap<UUID, IdObject> pendingSettlementInvites = MultimapBuilder.hashKeys().hashSetValues().build();
    private static Multimap<IdObject, IdObject> pendingEmpireInvites = MultimapBuilder.hashKeys().hashSetValues().build();
    private static Multimap<String, String> pendingAlly = MultimapBuilder.hashKeys().hashSetValues().build();

    public InviteCache() {
        super();
    }

    public static Multimap<UUID, IdObject> getPendingSettlementInvites() {
        return pendingSettlementInvites;
    }

    public static Multimap<IdObject, IdObject> getPendingEmpireInvites() {
        return pendingEmpireInvites;
    }

    public static boolean hasPendingAlly(LandOwner landOwner, LandOwner target) {
        return pendingAlly.containsEntry(landOwner.getIdKey(), target.getIdKey());
    }

    public static boolean removePendingAlly(LandOwner landOwner, LandOwner target) {
        return pendingAlly.remove(landOwner.getIdKey(), target.getIdKey());
    }

    public static boolean addPendingAlly(LandOwner target, LandOwner landOwner) {
        return pendingAlly.put(target.getIdKey(), landOwner.getIdKey());
    }
}

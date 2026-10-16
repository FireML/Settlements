package de.themoep.settlements.db;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.utils.IdObject;

public class InviteCache {
    private static final Multimap<IdObject, IdObject> pendingEmpireInvites = MultimapBuilder.hashKeys().hashSetValues().build();

    public static Multimap<IdObject, IdObject> getPendingEmpireInvites() {
        return pendingEmpireInvites;
    }
}

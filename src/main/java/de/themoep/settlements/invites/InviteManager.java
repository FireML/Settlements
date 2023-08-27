package de.themoep.settlements.invites;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Utils;

import java.util.Collection;
import java.util.UUID;

public class InviteManager {

    private static InviteManager instance;

    private final Multimap<UUID, SettlementInvite> playerInvites = MultimapBuilder.hashKeys().arrayListValues().build();

    private void setup() {
        for (SettlementInvite invite : DatabaseManager.getInstance().getSettlementInvites()) {
            addInvite(invite);
        }
    }

    public static InviteManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new InviteManager();
            instance.setup();
        }
    }

    public Collection<SettlementInvite> getInvites(Resident resident) {
        return playerInvites.get(resident.getUUID());
    }

    public SettlementInvite getInvite(Resident resident, Settlement settlement) {
        return getInvite(resident.getUUID(), settlement);
    }

    public SettlementInvite getInvite(UUID playerId, Settlement settlement) {
        for (SettlementInvite invite : playerInvites.get(playerId)) {
            if (invite.isSource(settlement)) {
                return invite;
            }
        }
        return null;
    }

    public boolean addInvite(Resident resident, Settlement settlement) {
        if (resident == null || settlement == null) {
            return false;
        }
        SettlementInvite invite = new SettlementInvite(resident, settlement);
        if (invite == null) {
            return false;
        }
        playerInvites.put(resident.getUUID(), invite);
        invite.save();
        return true;
    }

    private boolean addInvite(SettlementInvite invite) {
        if (invite == null) {
            return false;
        }
        if (invite.getTarget() == null) {
            return false;
        }
        playerInvites.put(invite.getTarget().getUUID(), invite);
        return true;
    }

    public void removeInvite(Resident resident, Settlement settlement) {
        playerInvites.get(resident.getUUID()).removeIf(invite -> {
            if (invite.isSource(settlement)) {
                Utils.runAsync(() -> DatabaseManager.getInstance().delete(invite));
                return true;
            }
            return false;
        });
    }
}

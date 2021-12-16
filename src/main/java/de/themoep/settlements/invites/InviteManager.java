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

    private Multimap<UUID, SettlementInvite> playerInvites = MultimapBuilder.hashKeys().arrayListValues().build();

    public InviteManager() {
        for (SettlementInvite invite : DatabaseManager.getInstance().getSettlementInvites()) {
            addInvite(invite);
        }
    }

    public static InviteManager getInstance() {
        if (instance == null) {
            instance = new InviteManager();
        }
        return instance;
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

    public void addInvite(Resident resident, Settlement settlement) {
        SettlementInvite invite = new SettlementInvite(resident, settlement);
        addInvite(invite);
        invite.save();
    }

    private void addInvite(SettlementInvite invite) {
        playerInvites.put(invite.getTarget().getUUID(), invite);
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

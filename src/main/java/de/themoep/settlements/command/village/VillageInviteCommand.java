package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.utils.Utils;

public class VillageInviteCommand extends VillageSubCommand {
    public VillageInviteCommand(SubCommand parent) {
        super(parent, "invite <player>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident invitedPlayer = ResidentManager.getInstance().getResident(args[0]);
            if (invitedPlayer == null) {
                res.tell(getError("player-not-found"), "name", args[0]);
                return;
            }
            if (invitedPlayer.hasVillage()) {
                res.tell(getMessage("already-in-village"), "name", invitedPlayer.getName(), "state", invitedPlayer.getVillage().getState().getTag());
                return;
            }
            Utils.runSync(() -> {
                InviteCache.getPendingVillageInvites().put(invitedPlayer.getUUID(), res.getVillage().getId());
                getMessage("received").tell(invitedPlayer, "name", res.getVillage().getName(), "state", res.getVillage().getState().getTag());
                getMessage("joinfee").tell(invitedPlayer,
                        "name", res.getVillage().getName(),
                        "state", res.getVillage().getState().getTag(),
                        "amount", String.valueOf(res.getVillage().getJoinFee()),
                        "joinfee", String.valueOf(res.getVillage().getJoinFee()),
                        "tax", String.valueOf(res.getVillage().getTax())
                );
            });
            getMessage("alert").tell(res.getVillage().getOnlineResidents(), "player", invitedPlayer.getName());
        });
        return true;
    }
}

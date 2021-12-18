package de.themoep.settlements.command.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.Utils;

public class SettlementInviteCommand extends SettlementSubCommand {
    public SettlementInviteCommand(SubCommand parent) {
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
                getError("player-not-found").tell(res, "name", args[0]);
                return;
            }
            if (invitedPlayer.hasSettlement()) {
                getMessage("already-in-settlement").tell(res, "name", invitedPlayer.getName(), "state", invitedPlayer.getSettlement().getState().getTag());
                return;
            }
            if (InviteManager.getInstance().getInvite(invitedPlayer, res.getSettlement()) != null) {
                getMessage("already-has-invite").tell(res, "name", invitedPlayer.getName(), "state", res.getSettlement().getState().getTag());
                return;
            }
            Utils.runSync(() -> {
                InviteManager.getInstance().addInvite(invitedPlayer, res.getSettlement());
                if (invitedPlayer.getPlayer() != null) {
                    getMessage("received").tell(invitedPlayer, "name", res.getSettlement().getName(), "state", res.getSettlement().getState().getTag());
                    getMessage("joinfee").tell(invitedPlayer,
                            "name", res.getSettlement().getName(),
                            "state", res.getSettlement().getState().getTag(),
                            "amount", String.valueOf(res.getSettlement().getJoinFee()),
                            "joinfee", String.valueOf(res.getSettlement().getJoinFee()),
                            "tax", String.valueOf(res.getSettlement().getTax())
                    );
                } else {
                    for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                        provider.sendMail(res.getSettlementTitle() + " " + res.getName(), invitedPlayer, getText("mail",
                                "name", res.getSettlement().getName(),
                                "state", res.getSettlement().getState().getTag(),
                                "amount", String.valueOf(res.getSettlement().getJoinFee()),
                                "joinfee", String.valueOf(res.getSettlement().getJoinFee()),
                                "tax", String.valueOf(res.getSettlement().getTax())
                        ));
                    }
                }
            });
            getMessage("alert").tell(res.getSettlement().getOnlineResidents(), "player", invitedPlayer.getName());
        });
        return true;
    }
}

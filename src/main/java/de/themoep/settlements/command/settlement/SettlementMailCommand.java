package de.themoep.settlements.command.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.resident.Resident;
import uk.firedev.poleislib.utils.ComponentUtils;

public class SettlementMailCommand extends SettlementSubCommand {
    public SettlementMailCommand(SubCommand parent) {
        super(parent, "mail <message>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (SettlementsPlugin.getInstance().getMailProviders().isEmpty()) {
            res.getPlayer().sendMessage(ComponentUtils.parseComponent("&cNo mail plugin installed?"));
            return true;
        } else if (args.length == 0) {
            return false;
        }
        MailProvider mailProvider = SettlementsPlugin.getInstance().getMailProviders().iterator().next();
        String sender = getMessage("sender").getReplacedText(
                "title", res.getSettlementTitle(),
                "displaytitle", res.getDisplayTitle(),
                "name", res.getName(),
                "settlementstate", res.getSettlement().getStateTag(),
                "settlementname", res.getSettlement().getName());
        String message = String.join(" ", args);
        for (Resident resident : res.getSettlement().getResidents()) {
            if (resident != res) {
                mailProvider.sendMail(sender, resident, message);
            }
        }
        getMessage("sent").tell(res, "amount", String.valueOf(res.getSettlement().getResidentIds().size() - 1));
        return true;
    }
}

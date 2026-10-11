package de.themoep.settlements.command.empire;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ComponentUtils;

public class EmpireMailCommand extends EmpireSubCommand {
    public EmpireMailCommand(SubCommand parent) {
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
                "empirestate", res.getSettlement().getEmpire().getStateTag(),
                "empirename", res.getSettlement().getEmpire().getName());
        String message = String.join(" ", args);
        for (Settlement member : res.getSettlement().getEmpire().getMembers()) {
            if (!member.isMember(res)) {
                mailProvider.sendMail(sender, member.getChief(), message);
            }
        }
        getMessage("sent").tell(res, "amount", String.valueOf(res.getSettlement().getEmpire().getMemberIds().size() - 1));
        return true;
    }
}

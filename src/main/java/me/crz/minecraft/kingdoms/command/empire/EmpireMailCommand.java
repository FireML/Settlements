package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.integration.MailProvider;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.ChatColor;

public class EmpireMailCommand extends EmpireSubCommand {
    public EmpireMailCommand(SubCommand parent) {
        super(parent, "mail <message>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (KingdomsPlugin.getInstance().getMailProviders().isEmpty()) {
            res.getPlayer().sendMessage(ChatColor.RED + "No mail plugin installed?");
            return true;
        } else if (args.length == 0) {
            return false;
        }
        MailProvider mailProvider = KingdomsPlugin.getInstance().getMailProviders().iterator().next();
        String sender = getMessage("sender").getReplacedText(
                "title", res.getDisplayTitle(),
                "name", res.getName(),
                "empirestate", res.getVillage().getEmpire().getStateTag(),
                "empirename", res.getVillage().getEmpire().getName());
        String message = String.join(" ", args);
        for (Village member : res.getVillage().getEmpire().getMembers()) {
            if (!member.isMember(res)) {
                mailProvider.sendMail(sender, member.getChief(), message);
            }
        }
        getMessage("sent").tell(res, "amount", String.valueOf(res.getVillage().getEmpire().getMemberIds().size() - 1));
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.integration.MailProvider;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.ChatColor;

public class VillageMailCommand extends VillageSubCommand {
    public VillageMailCommand(SubCommand parent) {
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
                "title", res.getVillageTitle(),
                "displaytitle", res.getDisplayTitle(),
                "name", res.getName(),
                "settlementstate", res.getVillage().getStateTag(),
                "settlementname", res.getVillage().getName());
        String message = String.join(" ", args);
        for (Resident resident : res.getVillage().getResidents()) {
            if (resident != res) {
                mailProvider.sendMail(sender, resident, message);
            }
        }
        getMessage("sent").tell(res, "amount", String.valueOf(res.getVillage().getResidentIds().size() - 1));
        return true;
    }
}

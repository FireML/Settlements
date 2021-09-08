package de.themoep.settlements.command.resident;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.OfflinePlayer;

import java.sql.Date;

import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.Bukkit;
import de.themoep.settlements.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import de.themoep.settlements.command.SubCommand;
import org.bukkit.entity.Player;

public class ResidentShowCommand extends SubCommand {

    public ResidentShowCommand(SubCommand parent) {
        super(parent, "show <name>", "data", "info");
        setVisible(false);
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1 && !(sender instanceof Player)) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident target;
            if (args.length > 0) {
                target = ResidentManager.getInstance().getResident(args[0]);
                if (target == null) {
                    MessageUtils.tell(sender, getError("player-not-found"), "name", args[0]);
                    return;
                }
            } else {
                target = ResidentManager.getInstance().getResident((Player) sender);
            }

            OfflinePlayer op = Bukkit.getOfflinePlayer(target.getUUID());
            double balance = SettlementsPlugin.getInstance().getEconomy().getBalance(op);

            getMessage("info." + (target.hasVillage() ? target.hasEmpire() ? "in-empire" : "in-village" : "free")).tell(sender,
                    "name", target.getName(),
                    "empire", target.hasEmpire() ? target.getVillage().getEmpire().getName() : "none",
                    "empirerank", target.hasEmpire() ? target.getEmpireRank().getTag() : "none",
                    "settlement", target.hasVillage() ? target.getVillage().getName() : "none",
                    "rank", target.hasVillage() ? target.getVillageRank().getTag(target.getVillage()) : "none",
                    "title", target.hasVillage() ? target.getVillageTitle() : "none",
                    "balance", Utils.CURRENCY_FORMAT.format(balance),
                    "tax", Utils.CURRENCY_FORMAT.format(target.getTax()),
                    "bonus", Utils.CURRENCY_FORMAT.format(target.getBonusClaims()),
                    "joindate", Utils.DATE_FORMAT.format(new Date(op.getFirstPlayed())),
                    "lastlogin", Utils.DATE_FORMAT.format(new Date(target.getLastLogin()))
            );
        });
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.resident;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.OfflinePlayer;

import java.sql.Date;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class ResidentShowCommand extends SubCommand {

    public ResidentShowCommand(SubCommand parent) {
        super(parent, "show <name>", "data", "info");
        setVisible(false);
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                MessageUtils.tell(sender, getError("player-not-found"), "name", args[0]);
                return;
            }

            OfflinePlayer op = Bukkit.getOfflinePlayer(target.getUUID());
            double balance = KingdomsPlugin.getInstance().econ.getBalance(op);

            MessageUtils.tell(sender, getMessage("info"),
                    "name", target.getName(),
                    "settlement", (target.getVillage() == null) ? "none" : target.getVillage().getName(),
                    "rank", (target.getVillage() == null) ? "none" : target.getVillageRank().getTag(target.getVillage()),
                    "title", (target.getVillage() == null) ? "none" : target.getTitle(),
                    "balance", Utils.CURRENCY_FORMAT.format(balance),
                    "bonus", Utils.CURRENCY_FORMAT.format(target.getBonusClaims()),
                    "joindate", Utils.DATE_FORMAT.format(new Date(op.getFirstPlayed())),
                    "lastlogin",  Utils.DATE_FORMAT.format(new Date(target.getLastLogin()))
            );
        });
        return true;
    }
}

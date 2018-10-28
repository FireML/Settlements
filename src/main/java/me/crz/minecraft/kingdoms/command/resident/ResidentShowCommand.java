package me.crz.minecraft.kingdoms.command.resident;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.OfflinePlayer;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class ResidentShowCommand extends SubCommand {
    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public ResidentShowCommand(SubCommand parent) {
        super(parent, "show <name>");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                MessageUtils.tell(player, getError("player-not-found"), "name", args[0]);
                return;
            }

            OfflinePlayer op = Bukkit.getOfflinePlayer(target.getUUID());
            double balance = KingdomsPlugin.getInstance().econ.getBalance(op);

            MessageUtils.tell(player, getMessage("info"),
                    "name", target.getName(),
                    "settlement", (target.getVillage() == null) ? "none" : target.getVillage().getName(),
                    "rank", (target.getVillage() == null) ? "none" : target.getVillageRank().getTag(target.getVillage()),
                    "title", (target.getVillage() == null) ? "none" : target.getTitle(),
                    "balance", DECIMAL_FORMAT.format(balance),
                    "taxowed", DECIMAL_FORMAT.format(target.getTaxOwed()),
                    "joindate", DATE_FORMAT.format(new Date(op.getFirstPlayed())),
                    "lastlogin",  DATE_FORMAT.format(new Date(target.getLastLogin()))
            );
        });
        return true;
    }
}

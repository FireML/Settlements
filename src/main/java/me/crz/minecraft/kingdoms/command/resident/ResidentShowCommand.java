package me.crz.minecraft.kingdoms.command.resident;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.OfflinePlayer;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class ResidentShowCommand extends SubCommand {
    private final DecimalFormat df = new DecimalFormat("#.##");

    public ResidentShowCommand() {
        super("Shows you information about a resident", "show");
        this.setPermission("kingdoms.user");
        this.setUsage("show <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        Utils.runAsync(() -> {
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target == null) {
                MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l can be found!", "{player}", args[0]);
                return;
            }
            Village village = VillageManager.getInstance().getVillageFromResident(target);
            OfflinePlayer op = Bukkit.getOfflinePlayer(target.getUUID());
            double balance = KingdomsPlugin.getInstance().econ.getBalance(op);
            StringBuilder sbp = new StringBuilder("&8------&c&l");
            sbp.append(target.getName());
            sbp.append("&8------\n");
            sbp.append("&a&lSettlement: &6");
            sbp.append((village == null) ? "none" : village.getName());
            sbp.append("\n&a&lBalance: &6$");
            sbp.append(df.format(balance));
            sbp.append("\n&a&lJoin Date: &6");
            sbp.append(new SimpleDateFormat("dd/MM/yyyy").format(new Date(op.getFirstPlayed())));
            sbp.append("\n&a&lLast Login: &6");
            sbp.append(new SimpleDateFormat("dd/MM/yyyy").format(new Date(target.getLastLogin())));
            MessageUtils.tell(player, sbp.toString());
        });
    }
}

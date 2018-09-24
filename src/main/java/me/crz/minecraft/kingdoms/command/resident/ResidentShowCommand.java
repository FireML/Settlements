package me.crz.minecraft.kingdoms.command.resident;

import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.OfflinePlayerManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class ResidentShowCommand extends SubCommand {
    public ResidentShowCommand() {
        super("Shows you information about a resident", "show");
        this.setPermission("village.user");
        this.setUsage("show <name>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        final UUID uuid = OfflinePlayerManager.getInstance().uuidFromName(args[0].toLowerCase());
        if (uuid == null) {
            MessageUtils.tell(player, "&c&lNo &r&6&lplayer&r&c&l with the name &r&6&l{player}&r&c&l can be found!", "{player}", args[0]);
            return;
        }
        final OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
        final Village village = VillageManager.getInstance().getVillageFromResident(ResidentManager.getInstance().getResident(target.getUniqueId()));
        final double balance = KingdomsPlugin.getInstance().econ.getBalance(target);
        final DecimalFormat df = new DecimalFormat("#,###.00");
        final StringBuilder sbp = new StringBuilder("&8------&c&l");
        sbp.append(target.getName());
        sbp.append("&8------\n");
        sbp.append("&a&lSettlement: &6");
        sbp.append((village == null) ? "none" : village.getName());
        sbp.append("\n&a&lBalance: &6$");
        sbp.append(df.format(balance));
        sbp.append("\n&a&lJoin Date: &6");
        sbp.append(new SimpleDateFormat("dd/MM/yyyy").format(new Date(target.getFirstPlayed())));
        MessageUtils.tell(player, sbp.toString());
    }
}

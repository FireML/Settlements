package me.crz.minecraft.kingdoms.command.village.money;

import net.milkbowl.vault.economy.EconomyResponse;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.rank.VillageRank;

import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageMoneyCommand extends SubCommand {
    public VillageMoneyCommand() {
        super("Commands to interact with the village bank", "bank", "money");
        this.setPermission("village.user");
        this.setUsage("bank <deposit|withdraw|balance> <amount>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        final String option = args[0];
        final DecimalFormat df = new DecimalFormat("#,###");
        if (option.equalsIgnoreCase("balance") || option.equalsIgnoreCase("bal")) {
            MessageUtils.tell(player, "&2&lVillage Balance: &r&7" + df.format((long) village.getBank()));
            return;
        }
        if (option.equalsIgnoreCase("withdraw") || option.equalsIgnoreCase("with")) {
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
                MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + " or higher&r&c&l!");
                return;
            }
            if (args.length < 2 && !this.isInt(args[1])) {
                MessageUtils.tell(player, "&cPlease specify an amount to withdraw!");
                return;
            }
            final int withdrewInit = Integer.parseInt(args[1]);
            if (withdrewInit <= village.getBank()) {
                final EconomyResponse r = Base.getEconomy().depositPlayer(player, (double) withdrewInit);
                if (r.transactionSuccess()) {
                    village.setBank(village.getBank() - withdrewInit);
                    VillageManager.getInstance().saveVillage(village);
                    MessageUtils.tellResidents(village.getResidents(), "&e&l" + res.getVillageRank() + " &r&e" + player.getName() + " &r&7withdrew &6" + withdrewInit + "&r&7 from the village bank!");
                } else {
                    MessageUtils.tell(player, "&cAn error occurred, please tell an admin!");
                }
            } else {
                MessageUtils.tell(player, "&c&lNot enough money in the village bank to withdraw &r&6&l" + withdrewInit + "&r&c&l!", null, null);
            }
        }
        if (option.equalsIgnoreCase("deposit") || option.equalsIgnoreCase("deposit")) {
            if (args.length < 2 && !this.isInt(args[1])) {
                MessageUtils.tell(player, "&cPlease specify an amount to withdraw!");
                return;
            }
            final int deposit = Integer.parseInt(args[1]);
            final OfflinePlayer op = Bukkit.getOfflinePlayer(player.getUniqueId());
            if (Base.getEconomy().getBalance(op) >= deposit) {
                final EconomyResponse r2 = Base.getEconomy().withdrawPlayer(op, (double) deposit);
                if (r2.transactionSuccess()) {
                    village.setBank(village.getBank() + deposit);
                    VillageManager.getInstance().saveVillage(village);
                    MessageUtils.tellResidents(village.getResidents(), "&e&l" + res.getVillageRank() + "&r&e" + player.getName() + " &r&7deposited &6" + deposit + "&r&7 into the village bank!");
                } else {
                    MessageUtils.tell(player, "&cAn error occurred, please tell an admin!");
                }
            } else {
                MessageUtils.tell(player, "&c&lNot enough money in your bank to withdraw &r&6&l" + deposit + "&r&c&l!");
            }
        }
    }

    public boolean isInt(final String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

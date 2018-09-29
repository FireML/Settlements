package me.crz.minecraft.kingdoms.command.village.money;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
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
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, this.getUsage(base));
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        String option = args[0];
        DecimalFormat df = new DecimalFormat("#,###");
        if (option.equalsIgnoreCase("balance") || option.equalsIgnoreCase("bal")) {
            MessageUtils.tell(player, "&2&lVillage Balance: &r&7" + df.format((long) village.getBank()));
        } else if (option.equalsIgnoreCase("withdraw") || option.equalsIgnoreCase("with")) {
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
                MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.CHIEF.getTag(village.getState()));
                return;
            }
            if (args.length < 2 && !this.isInt(args[1])) {
                MessageUtils.tell(player, "&cPlease specify an amount to withdraw!");
                return;
            }
            int withdrewInit = Integer.parseInt(args[1]);
            if (withdrewInit <= village.getBank()) {
                EconomyResponse r = Base.getEconomy().depositPlayer(player, (double) withdrewInit);
                if (r.transactionSuccess()) {
                    village.setBank(village.getBank() - withdrewInit);
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
                    MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.BANK_WITHDRAW,
                            "rank", res.getVillageRank().getTag(village.getState()),
                            "player", player.getName(),
                            "amount", String.valueOf(withdrewInit));
                } else {
                    MessageUtils.tell(player, "&cAn error occurred, please tell an admin!");
                }
            } else {
                MessageUtils.tell(player, MessageUtils.Message.BANK_NOT_ENOUGH_MONEY, "amount", String.valueOf(withdrewInit));
            }
        } else if (option.equalsIgnoreCase("deposit") || option.equalsIgnoreCase("deposit")) {
            if (args.length < 2 && !this.isInt(args[1])) {
                MessageUtils.tell(player, "&cPlease specify an amount to withdraw!");
                return;
            }
            int deposit = Integer.parseInt(args[1]);
            OfflinePlayer op = Bukkit.getOfflinePlayer(player.getUniqueId());
            if (Base.getEconomy().getBalance(op) >= deposit) {
                EconomyResponse r2 = Base.getEconomy().withdrawPlayer(op, (double) deposit);
                if (r2.transactionSuccess()) {
                    village.setBank(village.getBank() + deposit);
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
                    MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.BANK_DEPOSIT,
                            "rank", res.getVillageRank().getTag(village.getState()),
                            "player", player.getName(),
                            "amount", String.valueOf(deposit));
                } else {
                    MessageUtils.tell(player, "&cAn error occurred, please tell an admin!");
                }
            } else {
                MessageUtils.tell(player, MessageUtils.Message.PLAYER_NOT_ENOUGH_MONEY, "amount", String.valueOf(deposit));
            }
        }
    }

    public boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

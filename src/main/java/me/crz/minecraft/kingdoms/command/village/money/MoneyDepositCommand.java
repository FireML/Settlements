package me.crz.minecraft.kingdoms.command.village.money;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import net.milkbowl.vault.economy.EconomyResponse;

public class MoneyDepositCommand extends VillageSubCommand {
    public MoneyDepositCommand(SubCommand parent) {
        super(parent, "deposit <amount>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        try {
            long deposit = Long.parseLong(args[0]);
            if (deposit <= 0) {
                return false;
            }
            if (KingdomsPlugin.getInstance().getEconomy().getBalance(resident.getPlayer()) >= deposit) {
                EconomyResponse r2 = KingdomsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), (double) deposit);
                if (r2.transactionSuccess()) {
                    resident.getVillage().addToBank(deposit);
                    Utils.runAsync(() -> {
                        DatabaseManager.getInstance().saveVillage(resident.getVillage());
                        DatabaseManager.getInstance().logDeposit(resident.getVillage(), resident, deposit);
                    });
                    getMessage("deposited").tell(resident.getVillage().getOnlineResidents(),
                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                            "title", resident.getDisplayTitle(),
                            "name", resident.getName(),
                            "amount", Utils.CURRENCY_FORMAT.format(deposit));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                getError("not-enough-money").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(deposit));
            }
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(resident, "input", args[0]);
            return false;
        }
        return true;
    }
}

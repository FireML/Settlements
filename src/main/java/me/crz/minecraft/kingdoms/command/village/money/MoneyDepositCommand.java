package me.crz.minecraft.kingdoms.command.village.money;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import net.milkbowl.vault.economy.EconomyResponse;

public class MoneyDepositCommand extends VillageSubCommand {
    public MoneyDepositCommand(SubCommand parent) {
        super(parent, "deposit <amount>", VillageRank.RESIDENT);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        try {
            int deposit = Integer.parseInt(args[0]);
            if (Base.getEconomy().getBalance(resident.getPlayer()) >= deposit) {
                EconomyResponse r2 = Base.getEconomy().withdrawPlayer(resident.getPlayer(), (double) deposit);
                if (r2.transactionSuccess()) {
                    resident.getVillage().setBank(resident.getVillage().getBank() + deposit);
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                    MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("deposited"),
                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                            "player", resident.getName(),
                            "amount", String.valueOf(deposit));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                resident.tell(getError("not-enough-money"), "amount", String.valueOf(deposit));
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}

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

public class MoneyWithdrawCommand extends VillageSubCommand {
    public MoneyWithdrawCommand(SubCommand parent) {
        super(parent, "withdraw <amount>", VillageRank.CHIEF, "wd");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        try {
            int withdrewInit = Integer.parseInt(args[0]);
            if (withdrewInit <= resident.getVillage().getBank()) {
                EconomyResponse r = Base.getEconomy().depositPlayer(resident.getPlayer(), (double) withdrewInit);
                if (r.transactionSuccess()) {
                    resident.getVillage().setBank(resident.getVillage().getBank() - withdrewInit);
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                    DatabaseManager.getInstance().logWithdraw(resident.getVillage(), resident, withdrewInit);
                    MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("withdrawn"),
                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                            "player", resident.getName(),
                            "amount", String.valueOf(withdrewInit));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                resident.tell(getMessage("not-enough-money"), "amount", String.valueOf(withdrewInit));
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}

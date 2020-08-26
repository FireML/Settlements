package me.crz.minecraft.kingdoms.command.village.money;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import net.milkbowl.vault.economy.EconomyResponse;

public class MoneyWithdrawCommand extends VillageSubCommand {
    public MoneyWithdrawCommand(SubCommand parent) {
        super(parent, "withdraw <amount>", "wd");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        try {
            long withdrewInit = Long.parseLong(args[0]);
            if (withdrewInit <= 0) {
                return false;
            }
            if (withdrewInit <= resident.getVillage().getBank()) {
                EconomyResponse r = Base.getEconomy().depositPlayer(resident.getPlayer(), (double) withdrewInit);
                if (r.transactionSuccess()) {
                    resident.getVillage().removeFromBank(withdrewInit);
                    Utils.runAsync(() -> {
                        DatabaseManager.getInstance().saveVillage(resident.getVillage());
                        DatabaseManager.getInstance().logWithdraw(resident.getVillage(), resident, withdrewInit);
                    });
                    getMessage("withdrawn").tell(resident.getVillage().getOnlineResidents(),
                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                            "title", resident.getDisplayTitle(),
                            "name", resident.getName(),
                            "amount", Utils.CURRENCY_FORMAT.format(withdrewInit));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                getError("not-enough-money").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(withdrewInit));
            }
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(resident, "input", args[0]);
            return false;
        }
        return true;
    }
}

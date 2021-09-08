package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class VillageDonateCommand extends ResidentSubCommand {
    public VillageDonateCommand(SubCommand parent) {
        super(parent, "donate <settlement> <amount>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }
        Player player = resident.getPlayer();
        String name = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
        Village village = VillageManager.getInstance().getFromName(name);
        if (village == null) {
            getError("village-doesnt-exist").tell(player, "name", name);
            return false;
        }
        try {
            long donate = Long.parseLong(args[args.length - 1]);
            if (donate <= 0) {
                return false;
            }
            if (SettlementsPlugin.getInstance().getEconomy().getBalance(player) >= donate) {
                EconomyResponse r2 = SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(player, (double) donate);
                if (r2.transactionSuccess()) {
                    village.addToBank(donate);
                    DatabaseManager.getInstance().saveSettlement(village);
                    Utils.runAsync(() -> DatabaseManager.getInstance().log(null, village, resident, "donate", "cash", donate));
                    if (!village.isMember(resident)) {
                        getMessage("success").tell(resident,
                                "amount", Utils.CURRENCY_FORMAT.format(donate),
                                "state", village.getStateTag(),
                                "name", village.getName()
                        );
                    }
                    getMessage("donated").tell(village.getOnlineResidents(),
                            "name", resident.getName(),
                            "amount", Utils.CURRENCY_FORMAT.format(donate));
                } else {
                    resident.tell("&cAn error occurred, please tell an admin!");
                }
            } else {
                getError("not-enough-money").tell(player, "amount", Utils.CURRENCY_FORMAT.format(donate));
            }
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(player, "input", args[args.length - 1]);
            return false;
        }
        return true;
    }
}

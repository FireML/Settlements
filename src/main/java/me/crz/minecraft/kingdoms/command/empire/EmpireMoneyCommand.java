package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

public class EmpireMoneyCommand extends EmpireSubCommand {

    public EmpireMoneyCommand(SubCommand parent) {
        super(parent, "bank <deposit|withdraw|balance|history> <amount>", "money");
        setHelpCommand(new HelpCommand(this));
        getHelpCommand().setDefaultTypeProvider(s -> s instanceof Player && ResidentManager.getInstance().doesResidentHaveEmpire((Player) s) ? "member" : "none");
        EmpireSubCommand balanceCommand = new EmpireSubCommand(this, "balance") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                getMessage("balance").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(resident.getVillage().getEmpire().getBank()));
                return true;
            }
        };
        parent.registerCommand(balanceCommand);
        registerCommand(balanceCommand);
        registerCommand(new EmpireSubCommand(this, "deposit <amount>") {
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
                            resident.getVillage().getEmpire().addToBank(deposit);
                            Utils.runAsync(() -> {
                                DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                                DatabaseManager.getInstance().logDeposit(resident.getVillage().getEmpire(), resident.getVillage(), resident, deposit);
                            });
                            getMessage("deposited").tell(resident.getVillage().getEmpire().getOnlineResidents(),
                                    "rank", resident.getEmpireRank().getTag(resident.getVillage().getEmpire()),
                                    "title", resident.getVillageTitle(),
                                    "displaytitle", resident.getDisplayTitle(),
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
        });
        registerCommand(new EmpireSubCommand(this, "withdraw <amount>") {
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
                    if (withdrewInit <= resident.getVillage().getEmpire().getBank()) {
                        EconomyResponse r = KingdomsPlugin.getInstance().getEconomy().depositPlayer(resident.getPlayer(), (double) withdrewInit);
                        if (r.transactionSuccess()) {
                            resident.getVillage().getEmpire().removeFromBank(withdrewInit);
                            Utils.runAsync(() -> {
                                DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                                DatabaseManager.getInstance().logWithdraw(resident.getVillage().getEmpire(), resident.getVillage(), resident, withdrewInit);
                            });
                            getMessage("withdrawn").tell(resident.getVillage().getEmpire().getOnlineResidents(),
                                    "rank", resident.getEmpireRank().getTag(resident.getVillage().getEmpire()),
                                    "title", resident.getVillageTitle(),
                                    "displaytitle", resident.getDisplayTitle(),
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
        });
        registerCommand(new EmpireHistoryCommand(this, LogType.BANK));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}

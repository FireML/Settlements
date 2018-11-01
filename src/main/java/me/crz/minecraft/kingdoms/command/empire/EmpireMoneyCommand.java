package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import net.milkbowl.vault.economy.EconomyResponse;

import java.text.DecimalFormat;

public class EmpireMoneyCommand extends EmpireSubCommand {
    private final static DecimalFormat DF = new DecimalFormat("#,###");

    public EmpireMoneyCommand(SubCommand parent) {
        super(parent, "bank <deposit|withdraw|balance|history> <amount>", EmpireRank.NONE, "money");
        setPermission("kingdoms.user");
        registerCommand(new EmpireSubCommand(this, "balance", EmpireRank.NONE) {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                getMessage("balance").tell(resident, "amount", DF.format(resident.getVillage().getEmpire().getBank()));
                return true;
            }
        });
        registerCommand(new EmpireSubCommand(this, "deposit <amount>", EmpireRank.NONE) {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }
                try {
                    long deposit = Long.parseLong(args[0]);
                    if (Base.getEconomy().getBalance(resident.getPlayer()) >= deposit) {
                        EconomyResponse r2 = Base.getEconomy().withdrawPlayer(resident.getPlayer(), (double) deposit);
                        if (r2.transactionSuccess()) {
                            resident.getVillage().getEmpire().setBank(resident.getVillage().getEmpire().getBank() + deposit);
                            Utils.runAsync(() -> {
                                DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                                DatabaseManager.getInstance().logDeposit(resident.getVillage().getEmpire(), resident.getVillage(), resident, deposit);
                            });
                            MessageUtils.tellResidents(resident.getVillage().getEmpire().getOnlineResidents(), getMessage("deposited"),
                                    "rank", resident.getVillageRank().getTag(resident.getVillage()),
                                    "title", resident.getTitle(),
                                    "name", resident.getName(),
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
        });
        registerCommand(new EmpireSubCommand(this, "withdraw <amount>", EmpireRank.LEADER) {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }
                try {
                    long withdrewInit = Long.parseLong(args[0]);
                    if (withdrewInit <= resident.getVillage().getEmpire().getBank()) {
                        EconomyResponse r = Base.getEconomy().depositPlayer(resident.getPlayer(), (double) withdrewInit);
                        if (r.transactionSuccess()) {
                            resident.getVillage().getEmpire().setBank(resident.getVillage().getEmpire().getBank() - withdrewInit);
                            Utils.runAsync(() -> {
                                DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                                DatabaseManager.getInstance().logWithdraw(resident.getVillage().getEmpire(), resident.getVillage(), resident, withdrewInit);
                            });
                            MessageUtils.tellResidents(resident.getVillage().getEmpire().getOnlineResidents(), getMessage("withdrawn"),
                                    "rank", resident.getVillageRank().getTag(resident.getVillage()),
                                    "title", resident.getTitle(),
                                    "name", resident.getName(),
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
        });
        registerCommand(new EmpireHistoryCommand(this, EmpireRank.NONE, LogType.BANK));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}

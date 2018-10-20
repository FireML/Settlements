package me.crz.minecraft.kingdoms.command.village.money;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHistoryCommand;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.rank.VillageRank;

import java.text.DecimalFormat;

import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageMoneyCommand extends VillageSubCommand {
    private final static DecimalFormat DF = new DecimalFormat("#,###");

    public VillageMoneyCommand(SubCommand parent) {
        super(parent, "bank <deposit|withdraw|balance|history> <amount>", "money");
        setPermission("kingdoms.user");
        registerCommand(new VillageSubCommand(this, "balance", VillageRank.RESIDENT) {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                resident.tell("&2&lVillage Balance: &r&7" + DF.format((long) resident.getVillage().getBank()));
                return true;
            }
        });
        registerCommand(new MoneyWithdrawCommand(this));
        registerCommand(new MoneyDepositCommand(this));
        registerCommand(new VillageHistoryCommand(this, VillageRank.RESIDENT, VillageHistoryCommand.Type.BANK));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}

package me.crz.minecraft.kingdoms.command.village.money;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.RankHelpCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHistoryCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class VillageMoneyCommand extends VillageSubCommand {
    private final static DecimalFormat DF = new DecimalFormat("#,###");

    public VillageMoneyCommand(SubCommand parent) {
        super(parent, "bank <deposit|withdraw|balance|history> <amount>", "money");
        setHelpCommand(new HelpCommand(this));
        for (VillageRank rank : VillageRank.values()) {
            getHelpCommand().registerCommand(new RankHelpCommand(getHelpCommand(), rank));
        }
        getHelpCommand().setDefaultTypeProvider(s -> s instanceof Player && ResidentManager.getInstance().doesResidentHaveVillage((Player) s) ? "resident" : "none");
        registerCommand(new VillageSubCommand(this, "balance") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                getMessage("balance").tell(resident, "amount", DF.format(resident.getVillage().getBank()));
                return true;
            }
        });
        registerCommand(new MoneyWithdrawCommand(this));
        registerCommand(new MoneyDepositCommand(this));
        registerCommand(new VillageHistoryCommand(this, LogType.BANK));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}

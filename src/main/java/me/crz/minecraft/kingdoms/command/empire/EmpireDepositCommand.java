package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.vault.EmpireVaultInventory;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireDepositCommand extends EmpireSubCommand {
    public EmpireDepositCommand(SubCommand parent) {
        super(parent, "deposit", EmpireRank.NONE);
        setPermission("kingdoms.user");
        registerCommand(new EmpireHistoryCommand(this, EmpireRank.NONE, LogType.VAULT));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getVillage().getEmpire().isMaxLevel()) {
            new EmpireVaultInventory(resident.getPlayer()).open();
        } else {
            resident.tell(Message.get("command.empire.upgrade.max-level-reached"));
        }
        return true;
    }
}

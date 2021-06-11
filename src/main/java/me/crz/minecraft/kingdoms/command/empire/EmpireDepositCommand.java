package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHistoryCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.leveling.DepositGUI;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.Message;

public class EmpireDepositCommand extends EmpireSubCommand {
    public EmpireDepositCommand(SubCommand parent) {
        super(parent, "deposit");
        registerCommand(new EmpireHistoryCommand(this, LogType.VAULT));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getVillage().getEmpire().isMaxLevel()) {
            new DepositGUI(resident, resident.getVillage().getEmpire()).open();
        } else {
            resident.tell(Message.get("command.empire.upgrade.max-level-reached"));
        }
        return true;
    }
}

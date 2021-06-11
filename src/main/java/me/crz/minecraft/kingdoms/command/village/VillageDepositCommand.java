package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.leveling.DepositGUI;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.resident.Resident;

public class VillageDepositCommand extends VillageSubCommand {
    public VillageDepositCommand(SubCommand parent) {
        super(parent, "deposit");
        registerCommand(new VillageHistoryCommand(this, LogType.VAULT));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getVillage().isMaxLevel()) {
            new DepositGUI(resident, resident.getVillage()).open();
        } else {
            resident.tell(Message.get("command.village.upgrade.max-level-reached"));
        }
        return true;
    }
}

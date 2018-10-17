package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.vault.VaultInventory;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageDepositCommand extends VillageSubCommand {
    public VillageDepositCommand(SubCommand parent) {
        super(parent, "deposit", VillageRank.RESIDENT);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getVillage().isMaxLevel()) {
            new VaultInventory(resident.getPlayer()).open();
        } else {
            resident.tell(Message.get("command.upgrade.max-level-reached"));
        }
        return true;
    }
}

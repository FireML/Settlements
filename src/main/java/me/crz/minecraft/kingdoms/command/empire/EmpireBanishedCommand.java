package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.BanishCommand;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireBanishedCommand extends EmpireSubCommand implements BanishCommand {
    public EmpireBanishedCommand(SubCommand parent) {
        super(parent, "banished", "banishlist", "listbanished");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listBanished(resident, resident.getVillage().getEmpire());
    }
}

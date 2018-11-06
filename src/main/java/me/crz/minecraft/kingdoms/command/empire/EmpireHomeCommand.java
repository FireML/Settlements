package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireHomeCommand extends EmpireSubCommand {
    public EmpireHomeCommand(SubCommand parent) {
        super(parent, "home", EmpireRank.NONE);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Empire empire = resident.getVillage().getEmpire();

        if (empire.getHome() != null) {
            Utils.teleport(resident.getPlayer(), empire.getHome().toBukkitLocation(), "empire", getConfigPath(), "name", empire.getName());
        } else {
            getMessage("no-home").tell(resident);
        }
        return true;
    }
}

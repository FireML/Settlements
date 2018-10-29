package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;

public class EmpireMemberListCommand extends EmpireSubCommand {
    public EmpireMemberListCommand(SubCommand parent) {
        super(parent, "memberlist", EmpireRank.NONE, "roster", "members");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            sender.tell(getMessage("head"));
            for (Village village : sender.getVillage().getEmpire().getMembers()) {
                getMessage("entry").tell(sender, village.getReplacements(this));
            }
        });
        return true;
    }
}

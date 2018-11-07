package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireHomeCommand extends ResidentSubCommand {
    public EmpireHomeCommand(SubCommand parent) {
        super(parent, "home [<name>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Empire empire;
        if (args.length < 1) {
            if (!resident.hasEmpire()) {
                MessageUtils.tell(resident, getError("empire-required"));
                return false;
            } else if (resident.getVillage().getEmpire().getHome() == null) {
                MessageUtils.tell(resident, getMessage("own-no-home"), "chief", EmpireRank.LEADER.getTag());
                return true;
            } else {
                empire = resident.getVillage().getEmpire();
            }
        } else {
            empire = EmpireManager.getInstance().getEmpireFromName(args[0]);
            if (empire == null) {
                MessageUtils.tell(resident, getError("empire-doesnt-exist"));
                return true;
            }
        }

        if (!empire.isMember(resident)
                && (empire.getAccess() == LandAccess.PRIVATE
                || (empire.getAccess() == LandAccess.ALLY && !empire.isAlly(resident))
                || (empire.getAccess() == LandAccess.TRUSTED && !empire.isTrusted(resident)))) {
            MessageUtils.tell(resident, getMessage("no-access"), "state", empire.getStateTag());
            return true;
        }

        if (empire.getHome() != null) {
            Utils.teleport(resident.getPlayer(), empire.getHome().toBukkitLocation(), empire.getStateKey(), getConfigPath(),
                    "state", empire.getStateKey(), "name", empire.getName());
        } else {
            MessageUtils.tell(resident, getMessage("no-home"), "state", empire.getStateTag());
        }
        return true;
    }
}

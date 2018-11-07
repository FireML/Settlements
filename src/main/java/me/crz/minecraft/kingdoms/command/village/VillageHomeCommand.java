package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class VillageHomeCommand extends ResidentSubCommand {
    public VillageHomeCommand(SubCommand parent) {
        super(parent, "home [<name>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Village village;
        if (args.length < 1) {
            if (!resident.hasVillage()) {
                MessageUtils.tell(resident, getError("village-required"));
                return false;
            } else if (resident.getVillage().getHome() == null) {
                MessageUtils.tell(resident, getMessage("own-no-home"), "chief", VillageRank.CHIEF.getTag(resident.getVillage()));
                return true;
            } else {
                village = resident.getVillage();
            }
        } else {
            village = VillageManager.getInstance().getVillageFromName(args[0]);
            if (village == null) {
                MessageUtils.tell(resident, getError("village-doesnt-exist"));
                return true;
            }
        }

        if (!village.isMember(resident)
                && (village.getAccess() == LandAccess.PRIVATE
                || (village.getAccess() == LandAccess.ALLY && !village.isAlly(resident))
                || (village.getAccess() == LandAccess.TRUSTED && !village.isTrusted(resident)))) {
            MessageUtils.tell(resident, getMessage("no-access"), "state", village.getStateTag());
            return true;
        }

        if (village.getHome() != null) {
            Utils.teleport(resident.getPlayer(), village.getHome().toBukkitLocation(), village.getStateKey(), getConfigPath(),
                    "state", village.getStateKey(), "name", village.getName());
        } else {
            MessageUtils.tell(resident, getMessage("no-home"), "state", village.getStateTag());
        }
        return true;
    }
}

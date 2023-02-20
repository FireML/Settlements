package de.themoep.settlements.command.empire;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Location;

public class EmpireHomeCommand extends ResidentSubCommand {
    public EmpireHomeCommand(SubCommand parent) {
        super(parent, "home [<name>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Empire empire;
        if (args.length < 1) {
            if (!resident.hasEmpire()) {
                getError("empire-required").tell(resident);
                return false;
            } else if (resident.getSettlement().getEmpire().getHome() == null) {
                getMessage("own-no-home").tell(resident, "chief", EmpireRank.getLeader().getTag());
                return true;
            } else {
                empire = resident.getSettlement().getEmpire();
            }
        } else {
            String name = String.join(" ", args);
            empire = EmpireManager.getInstance().getEmpireFromName(name);
            if (empire == null) {
                getError("empire-doesnt-exist").tell(resident, "name", name);
                return true;
            }
        }

        if (!empire.canAccess(resident)) {
            getMessage("no-access").tell(resident, "state", empire.getStateTag());
            return true;
        }

        if (empire.getHome().getWorld().equalsIgnoreCase(empire.getWorldName())
                || LandManager.getInstance().getLand(empire.getHome().toChunkLocation()) == null) {
            for (OwnedLand land : empire.getLand()) {
                if (!LandManager.getInstance().isConnected(land)) {
                    Location newHome = land.getCenterBukkitLocation();
                    SettlementsPlugin.getInstance().getLogger().info("Fixed home of empire " + empire.getName() + " from " + empire.getHome() + " to " + newHome);
                    empire.setHome(newHome);
                    empire.save();
                    break;
                }
            }
        }

        if (empire.getHome() != null) {
            Utils.teleport(this, resident.getPlayer(), empire.getHome().toBukkitLocation(), empire.getStateKey(),
                    "state", empire.getStateKey(), "name", empire.getName());
        } else {
            getMessage("no-home").tell(resident, "state", empire.getStateTag());
        }
        return true;
    }
}

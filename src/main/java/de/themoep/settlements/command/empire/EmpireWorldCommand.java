package de.themoep.settlements.command.empire;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import org.bukkit.World;

public class EmpireWorldCommand extends ResidentSubCommand {
    public EmpireWorldCommand(SubCommand parent) {
        super(parent, "world [<name>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Empire empire;
        if (args.length < 1) {
            if (!resident.hasEmpire()) {
                getError("empire-required").tell(resident);
                return false;
            } else if (resident.getSettlement().getEmpire().hasWorld()) {
                getMessage("no-world").tell(resident,
                        "chief", EmpireRank.getLeader().getTag(),
                        "state", resident.getSettlement().getEmpire().getStateTag(),
                        "name", resident.getSettlement().getEmpire().getName()
                );
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

        if (!empire.hasWorld()) {
            getError("no-world").tell(resident, "state", empire.getStateTag(), "name", empire.getName());
            return true;
        }

        World world = SettlementsPlugin.getInstance().getServer().getWorld(empire.getWorldName());
        if (world != null) {
            Utils.teleport(this, resident.getPlayer(), world.getSpawnLocation(), empire.getStateKey(),
                    "state", empire.getStateKey(), "name", empire.getName());
        } else {
            getError("no-world").tell(resident, "state", empire.getStateTag(), "name", empire.getName());
        }
        return true;
    }
}

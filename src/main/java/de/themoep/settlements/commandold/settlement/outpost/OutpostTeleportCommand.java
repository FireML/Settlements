package de.themoep.settlements.commandold.settlement.outpost;

import de.themoep.settlements.commandold.settlement.SettlementSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.gui.OutpostsGUI;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.world.NamedLocation;

public class OutpostTeleportCommand extends SettlementSubCommand {
    public OutpostTeleportCommand(SettlementSubCommand parent) {
        super(parent, "teleport <name>", "tp", "otp", "gui");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            new OutpostsGUI(resident, resident.getSettlement()).open();
            return true;
        }
        NamedLocation location = resident.getSettlement().getOutpostHomes().get(args[0].toLowerCase());
        if (location != null) {
            OwnedLand land = LandManager.getInstance().getLand(location.toChunkLocation());
            if (land != null && land.isOwnedBy(resident.getSettlement())) {
                Utils.teleport(this, resident.getPlayer(), location.toBukkitLocation(), "outpost", "name", location.getName());
            } else {
                if (resident.getSettlement().getOutpostHomes().remove(args[0].toLowerCase()) != null) {
                    DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
                }
                getError("outpost-not-claimed").tell(resident, "name", args[0], "state", resident.getSettlement().getStateTag());
            }
        } else {
            getError("outpost-not-found").tell(resident, "name", args[0]);
        }
        return true;
    }
}

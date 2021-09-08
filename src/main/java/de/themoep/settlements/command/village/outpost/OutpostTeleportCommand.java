package de.themoep.settlements.command.village.outpost;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.world.NamedLocation;

public class OutpostTeleportCommand extends VillageSubCommand {
    public OutpostTeleportCommand(VillageSubCommand parent) {
        super(parent, "teleport <name>", "tp", "otp");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        NamedLocation location = resident.getVillage().getOutpostHomes().get(args[0].toLowerCase());
        if (location != null) {
            OwnedLand land = LandManager.getInstance().getLand(location.toChunkLocation());
            if (land != null && land.isOwnedBy(resident.getVillage())) {
                Utils.teleport(this, resident.getPlayer(), location.toBukkitLocation(), "outpost", "name", location.getName());
            } else {
                if (resident.getVillage().getOutpostHomes().remove(args[0].toLowerCase()) != null) {
                    DatabaseManager.getInstance().saveSettlement(resident.getVillage());
                }
                getError("outpost-not-claimed").tell(resident, "name", args[0], "state", resident.getVillage().getStateTag());
            }
        } else {
            getError("outpost-not-found").tell(resident, "name", args[0]);
        }
        return true;
    }
}

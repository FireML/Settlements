package de.themoep.settlements.commandold.settlement.outpost;

import de.themoep.settlements.commandold.settlement.SettlementSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.world.NamedLocation;

public class OutpostRenameCommand extends SettlementSubCommand {
    public OutpostRenameCommand(SettlementSubCommand parent) {
        super(parent, "rename <oldname> <newname>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }

        if (resident.getSettlement().getOutpostHomes().containsKey(args[1].toLowerCase())) {
            getError("already-exists").tell(resident, "name", args[1]);
            return true;
        }

        NamedLocation location = resident.getSettlement().getOutpostHomes().get(args[0].toLowerCase());
        if (location == null) {
            getError("outpost-not-found").tell(resident, "name", args[0]);
            return true;
        }

        OwnedLand land = LandManager.getInstance().getLand(location.toChunkLocation());
        if (land == null || !land.isOwnedBy(resident.getSettlement())) {
            if (resident.getSettlement().getOutpostHomes().remove(args[0].toLowerCase()) != null) {
                DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
            }
            getError("outpost-not-claimed").tell(resident, "name", args[0], "state", resident.getSettlement().getStateTag());
            return true;
        }

        String oldName = location.getName();
        location.setName(args[1]);
        resident.getSettlement().getOutpostHomes().remove(oldName.toLowerCase());
        resident.getSettlement().setOutpostHome(location);
        land.setOutpost(location.getName());
        getMessage("success").tell(resident.getSettlement().getOnlineResidents(),
                "player", resident.getName(),
                "oldname", oldName,
                "newname", args[1],
                "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ()));
        DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.world.NamedLocation;

public class OutpostRenameCommand extends VillageSubCommand {
    public OutpostRenameCommand(VillageSubCommand parent) {
        super(parent, "rename <oldname> <newname>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 2) {
            return false;
        }

        if (resident.getVillage().getOutpostHomes().containsKey(args[1].toLowerCase())) {
            getError("already-exists").tell(resident, "name", args[1]);
            return true;
        }

        NamedLocation location = resident.getVillage().getOutpostHomes().get(args[0].toLowerCase());
        if (location == null) {
            getError("outpost-not-found").tell(resident, "name", args[0]);
            return true;
        }

        OwnedLand land = LandManager.getInstance().getLand(location.toChunkLocation());
        if (land == null || !land.isOwnedBy(resident.getVillage())) {
            if (resident.getVillage().getOutpostHomes().remove(args[0].toLowerCase()) != null) {
                DatabaseManager.getInstance().saveVillage(resident.getVillage());
            }
            getError("outpost-not-claimed").tell(resident, "name", args[0], "state", resident.getVillage().getStateTag());
            return true;
        }

        String oldName = location.getName();
        location.setName(args[1]);
        resident.getVillage().getOutpostHomes().remove(oldName.toLowerCase());
        resident.getVillage().setOutpostHome(location);
        land.setOutpost(location.getName());
        getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                "player", resident.getName(),
                "oldname", oldName,
                "newname", args[1],
                "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ()));
        DatabaseManager.getInstance().saveVillage(resident.getVillage());
        return true;
    }
}

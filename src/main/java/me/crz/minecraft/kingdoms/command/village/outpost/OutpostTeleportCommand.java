package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.world.NamedLocation;

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
                Utils.teleport(this, resident.getPlayer(), location.toBukkitLocation(), "outpost", getConfigPath(), "name", location.getName());
            } else {
                if (resident.getVillage().getOutpostHomes().remove(args[0].toLowerCase()) != null) {
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                }
                getError("outpost-not-claimed").tell(resident, "name", args[0], "state", resident.getVillage().getStateTag());
            }
        } else {
            getError("outpost-not-found").tell(resident, "name", args[0]);
        }
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.world.NamedLocation;

public class OutpostTeleportCommand extends VillageSubCommand {
    public OutpostTeleportCommand(VillageSubCommand parent) {
        super(parent, "teleport <name>", VillageRank.RESIDENT, "tp");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            resident.tell("/s outpost tp <name>");
            return true;
        }
        NamedLocation location = resident.getVillage().getOutpostHomes().get(args[0].toLowerCase());
        if (location != null) {
            Utils.teleport(resident.getPlayer(), location.toBukkitLocation(), "outpost", getConfigPath(), "name", location.getName());
        } else {
            resident.tell(getError("outpost-not-found"), "name", args[0]);
        }
        return true;
    }
}

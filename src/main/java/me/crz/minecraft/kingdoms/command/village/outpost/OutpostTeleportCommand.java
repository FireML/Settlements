package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ExecuteMoveUtil;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
            Location loc2 = location.toBukkitLocation();
            resident.tell(getMessage("waiting"), "seconds", String.valueOf(5));
            Player player = resident.getPlayer();
            ExecuteMoveUtil.getInstance().execute(player, () -> {
                player.teleport(loc2);
                resident.tell(getMessage("success"), "name", location.getName());
            }, 5, "Teleport");
        } else {
            resident.tell(getError("outpost-not-found"), "name", args[0]);
        }
        return true;
    }
}

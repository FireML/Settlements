package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.lock.Lock;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleBlockLocation;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.stream.Collectors;

public class VillageAdminLockCommand extends SubCommand {
    public VillageAdminLockCommand(SubCommand parent) {
        super(parent, "lock list [<world>]|get <world> <x> <y> <z>");
        setPermission("kingdoms.admin.command.lock");
        registerCommand(new SubCommand(this, "list [<world>]") {
            @Override
            public boolean run(CommandSender sender, String base, String alias, String... args) {
                Utils.runAsync(() -> {
                    if (args.length > 0) {
                        sender.sendMessage("Locks:\n" + LockManager.getInstance().getLocks(args[0]).stream()
                                .map(l -> info(l))
                                .collect(Collectors.joining("\n")));
                    } else {
                        sender.sendMessage("Locks:\n" + LockManager.getInstance().getLocks().stream()
                                .map(l -> info(l))
                                .collect(Collectors.joining("\n")));
                    }
                });
                return true;
            }
        });
        registerCommand(new SubCommand(this, "get <world> <x> <y> <z>") {
            @Override
            public boolean run(CommandSender sender, String base, String alias, String... args) {
                if (args.length < 4) {
                    return false;
                }
                try {
                    SimpleBlockLocation location = new SimpleBlockLocation(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                    sender.sendMessage("Chunk is cached: " + LockManager.getInstance().isLoaded(location.toChunkLocation()));
                    if (location.toBukkitLocation() == null) {
                        sender.sendMessage("World not loaded!");
                        return true;
                    }
                    Chunk chunk = location.toBukkitLocation().getChunk();
                    sender.sendMessage("Chunk is loaded: " + chunk.isLoaded());
                    if (!chunk.isLoaded()) {
                        chunk.load(false);
                    }
                    sender.sendMessage("Block: " + location.toBukkitLocation().getBlock().getBlockData().getAsString());
                    Lock lock = LockManager.getInstance().getLock(location.toBukkitLocation().getBlock());
                    sender.sendMessage("Lock: " + (lock != null ? info(lock) : "null"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(e.getMessage());
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        return false;
    }

    private String info(Lock lock) {
        String string = "Location: " + lock.getLocation().getKey() + "\nOwner: " + lock.getOwner().getName() + "/" + lock.getOwner().getUUID() + "\nHopper: " + lock.isAllowHoppers() + "\nAccess: ";
        if (!lock.getAllowedPlayers().isEmpty()) {
            string += lock.getAllowedPlayers().stream()
                    .map(id -> ResidentManager.getInstance().getResident(id))
                    .filter(Objects::nonNull)
                    .map(Resident::getName)
                    .collect(Collectors.joining(","));
        }
        if (!lock.getAllowedVillageIds().isEmpty()) {
            string += lock.getAllowedVillageIds().stream()
                    .map(i -> VillageManager.getInstance().getVillageFromId(i))
                    .filter(Objects::nonNull)
                    .map(Village::getName)
                    .collect(Collectors.joining(","));
        }
        if (!lock.getAllowedEmpireIds().isEmpty()) {
            string += lock.getAllowedEmpireIds().stream()
                    .map(i -> EmpireManager.getInstance().getEmpireFromId(i))
                    .filter(Objects::nonNull)
                    .map(Empire::getName)
                    .collect(Collectors.joining(","));
        }
        return string;
    }
}

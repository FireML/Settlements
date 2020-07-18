package me.crz.minecraft.kingdoms.portal;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleBlockLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PortalManager {
    private static PortalManager instance;

    private Map<IdObject, Portal> portals = new HashMap<>();
    private Table<String, SimpleBlockLocation, Portal> portalLocations = HashBasedTable.create();
    private Multimap<IdObject, IdObject> portalLinks = MultimapBuilder.hashKeys().hashSetValues().build();

    private PortalManager() {
        for (Portal portal : DatabaseManager.getInstance().getAllPortals()) {
            portals.put(portal.getId(), portal);
            portalLocations.put(portal.getLocation().getChunkKey(), portal.getLocation(), portal);
            portalLinks.putAll(portal.getId(), portal.getLinks());
            for (IdObject id : portal.getLinks()) {
                portalLinks.put(id, portal.getId());
            }
        }
        KingdomsPlugin.logDebug(portals.size() + " portals loaded!");
    }

    public static PortalManager getInstance() {
        if (instance == null) {
            instance = new PortalManager();
        }
        return instance;
    }

    public Collection<Portal> getLinks(Portal portal) {
        return portal.getLinks().stream().map(this::getPortal).collect(Collectors.toList());
    }

    public Collection<Portal> getPortals(SimpleChunkLocation location) {
        return portalLocations.row(location.getKey()).values();
    }

    public Portal getPortal(IdObject id) {
        return portals.get(id);
    }

    public Portal getPortal(Location location) {
        return getPortal(new SimpleBlockLocation(location));
    }

    private Portal getPortal(SimpleBlockLocation location) {
        return portalLocations.get(location.getChunkKey(), location);
    }

    public Portal getPortal(Block block) {
        if (block.getType() == Material.OBSIDIAN) {
            for (int i = 0; i < 6; i ++) {
                Block next = block.getRelative(BlockFace.values()[i]);
                if (next.getType() == Material.NETHER_PORTAL) {
                    block = next;
                    break;
                }
            }
        }

        if (block.getType() == Material.NETHER_PORTAL) {
            Block below = block;
            while (below.getType() == Material.NETHER_PORTAL) {
                block = below;
                below = block.getRelative(BlockFace.DOWN);
            }
            Portal portal = getPortal(block.getLocation());
            if (portal != null) {
                return portal;
            }

            Axis axis = ((Orientable) block.getBlockData()).getAxis();
            if (axis == Axis.X) {
                Block next = block;
                while (next.getType() == Material.NETHER_PORTAL) {
                    portal = getPortal(next.getLocation());
                    if (portal != null) {
                        return portal;
                    }
                    next = next.getRelative(BlockFace.EAST);
                }
                next = block;
                while (next.getType() == Material.NETHER_PORTAL) {
                    portal = getPortal(next.getLocation());
                    if (portal != null) {
                        return portal;
                    }
                    next = next.getRelative(BlockFace.WEST);
                }
            } else if (axis == Axis.Z) {
                Block next = block;
                while (next.getType() == Material.NETHER_PORTAL) {
                    portal = getPortal(next.getLocation());
                    if (portal != null) {
                        return portal;
                    }
                    next = next.getRelative(BlockFace.NORTH);
                }
                next = block;
                while (next.getType() == Material.NETHER_PORTAL) {
                    portal = getPortal(next.getLocation());
                    if (portal != null) {
                        return portal;
                    }
                    next = next.getRelative(BlockFace.SOUTH);
                }
            }
        }
        return null;
    }

    public void addPortal(List<BlockState> blocks, Entity entity) {
        List<BlockState> lowest = new ArrayList<>();

        Axis axis = null;
        BlockFace direction = null;
        for (BlockState block : blocks) {
            if (block.getType() == Material.NETHER_PORTAL) {
                if (lowest.isEmpty() || lowest.get(0).getY() == block.getY()) {
                    lowest.add(block);
                } else if (!lowest.isEmpty() && lowest.get(0).getY() > block.getY()) {
                    lowest.clear();
                    lowest.add(block);
                }
                if (direction == null && entity != null) {
                    BlockFace playerDirection = entity.getFacing();
                    axis = ((Orientable) block.getBlockData()).getAxis();
                    switch (axis) {
                        case X:
                            if (entity.getFacing().getModX() == 0) {
                                direction = playerDirection.getOppositeFace();
                            } else {
                                direction = Utils.rotateClockWise(playerDirection);
                            }
                            break;
                        case Z:
                            if (entity.getFacing().getModZ() == 0) {
                                direction = playerDirection.getOppositeFace();
                            } else {
                                direction = Utils.rotateClockWise(playerDirection);
                            }
                            break;
                    }
                }
            }
        }

        if (direction == null && axis != null) {
            if (axis == Axis.X) {
                direction = BlockFace.EAST;
            } else {
                direction = BlockFace.NORTH;
            }
        }

        if (direction != null && !lowest.isEmpty()) {
            addPortal(new Portal(new SimpleBlockLocation(lowest.get(0).getLocation()), direction));
        } else {
            throw new IllegalArgumentException("Not a valid portal provided!");
        }
    }

    private void addPortal(Portal portal) {
        portalLocations.put(portal.getLocation().getChunkKey(), portal.getLocation(), portal);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().savePortal(portal);
            if (!portal.getId().isNull()) {
                portals.put(portal.getId(), portal);
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving portal at " + portal.getLocation() + " to the database! No ID was returned.");
            }
        });
    }

    public boolean removePortal(Portal portal) {
        portalLocations.remove(portal.getLocation().getChunkKey(), portal.getLocation());
        if (!portal.getId().isNull()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried removing portal " + portal.getLocation() + " with null id?");
            for (IdObject id : portalLinks.removeAll(portal.getId())) {
                Portal linked = getPortal(id);
                if (linked != null) {
                    linked.getLinks().remove(portal.getId());
                    portalLinks.remove(linked.getId(), portal.getId());
                }
            }
            portals.remove(portal.getId());
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().deletePortals(portal));
        return true;
    }

    public boolean removePortals(Collection<Portal> toRemove) {
        for (Portal portal : toRemove) {
            portalLocations.remove(portal.getLocation().getChunkKey(), portal.getLocation());
            if (portal.getId().isNull()) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried removing portal " + portal.getLocation() + " with null id?");
                continue;
            }
            for (IdObject id : portalLinks.removeAll(portal.getId())) {
                Portal linked = getPortal(id);
                if (linked != null) {
                    linked.getLinks().remove(portal.getId());
                    portalLinks.remove(linked.getId(), portal.getId());
                }
            }
            portals.remove(portal.getId());
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().deletePortals(toRemove.toArray(new Portal[0])));
        return true;
    }

    public boolean linkPortals(Portal source, Portal target) {
        if (source.getId().isNull() || target.getId().isNull()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried linking portals with null id?");
            return false;
        }
        boolean save = false;
        save |= source.getLinks().add(target.getId());
        save |= target.getLinks().add(source.getId());

        portalLinks.put(source.getId(), target.getId());
        portalLinks.put(target.getId(), source.getId());

        if (save) {
            Utils.runAsync(() -> DatabaseManager.getInstance().savePortals(source, target));
        }
        return true;
    }

    public boolean unlinkPortals(Portal source, Portal target) {
        if (source.getId().isNull() || target.getId().isNull()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried unlinking portals with null id?");
            return false;
        }
        boolean save = false;
        save |= source.getLinks().remove(target.getId());
        save |= target.getLinks().remove(source.getId());

        portalLinks.remove(source.getId(), target.getId());
        portalLinks.remove(target.getId(), source.getId());

        if (save) {
            Utils.runAsync(() -> DatabaseManager.getInstance().savePortals(source, target));
        }
        return true;
    }

    public void teleport(Player player, Portal source, Portal target) {
        if (!target.getLinks().contains(source.getId()) || !source.getLinks().contains(target.getId())) {
            Message.get("error.portals-not-linked").tell(player);
            return;
        }

        Location location = target.getLocation().toBukkitLocation();
        Vector direction = new Vector(target.getDirection().getModX(), 0, target.getDirection().getModZ());
        location.add(direction);
        location.setDirection(direction);

        if (new PlayerPortalEvent(player, player.getLocation(), location,
                PlayerTeleportEvent.TeleportCause.PLUGIN, 0, false, 0).callEvent()) {
            location.getWorld().getChunkAtAsync(location).thenAccept(c -> {
                if (c == null) {
                    Message.get("error.destination-obstructed").tell(player);
                    KingdomsPlugin.logDebug("Could not load chunk at " + location);
                    return;
                }
                Block block = location.getBlock();
                if (!block.isPassable() || !block.getRelative(BlockFace.UP).isPassable()) {
                    Message.get("error.destination-obstructed").tell(player);
                    KingdomsPlugin.logDebug("Location " + location + " is obstructed");
                    return;
                }
                Block down;
                do {
                    down = block.getRelative(BlockFace.DOWN);
                } while (!down.getType().isSolid() && down.getY() > 0);
                if (!down.getType().isSolid()) {
                    Message.get("error.destination-obstructed").tell(player);
                    KingdomsPlugin.logDebug("Location " + location + " is has no floor");
                    return;
                }

                player.sendBlockChange(down.getLocation(), down.getBlockData());
                if (!player.teleport(down.getRelative(BlockFace.UP).getLocation().toCenterLocation())) {
                    Message.get("error.destination-obstructed").tell(player);
                    KingdomsPlugin.logDebug("Failed to teleport " + player.getName() + " to " + location);
                }
            });
        }
    }
}

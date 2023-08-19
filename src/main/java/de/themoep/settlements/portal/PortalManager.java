package de.themoep.settlements.portal;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.portal.PortalCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.WorldUtils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.world.SimpleBlockLocation;
import de.themoep.settlements.world.SimpleChunkLocation;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PortalManager {
    private static PortalManager instance;

    private Map<IdObject, Portal> portals = new HashMap<>();
    private Table<String, SimpleBlockLocation, Portal> portalLocations = HashBasedTable.create();
    private Multimap<IdObject, IdObject> portalLinks = MultimapBuilder.hashKeys().hashSetValues().build();
    private List<Player> portalCooldown = new ArrayList<>();

    private void setup() {
        for (Portal portal : DatabaseManager.getInstance().getAllPortals()) {
            portals.put(portal.getId(), portal);
            portalLocations.put(portal.getLocation().getChunkKey(), portal.getLocation(), portal);
            portalLinks.putAll(portal.getId(), portal.getLinks());
            for (IdObject id : portal.getLinks()) {
                portalLinks.put(id, portal.getId());
            }
        }
        SettlementsPlugin.logDebug(portals.size() + " portals loaded!");
        CommandManager.getInstance().registerCommand(new PortalCommand());
        SettlementsPlugin.getInstance().registerListener(new PortalListener());
        SettlementsPlugin.logDebug("Loaded PortalManager with " + portals.size() + " portals and " + portalLinks.size() + " portal links");
    }

    public static PortalManager getInstance() {
        if (!Config.Portal.ENABLED) {
            throw new IllegalStateException("Tried to get PortalManager while portals were disabled!");
        }
        return instance;
    }

    public static void load() {
        if (!Config.Portal.ENABLED) {
            throw new IllegalStateException("Tried to load PortalManager while portals were disabled!");
        }
        if (instance == null) {
            instance = new PortalManager();
            instance.setup();
        }
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
                        case X -> {
                            if (entity.getFacing().getModX() == 0) {
                                direction = playerDirection.getOppositeFace();
                            } else {
                                direction = WorldUtils.rotateClockWise(playerDirection);
                            }
                        }
                        case Z -> {
                            if (entity.getFacing().getModZ() == 0) {
                                direction = playerDirection.getOppositeFace();
                            } else {
                                direction = WorldUtils.rotateClockWise(playerDirection);
                            }
                        }
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
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving portal at " + portal.getLocation() + " to the database! No ID was returned.");
            }
        });
    }

    public boolean removePortal(Portal portal) {
        portalLocations.remove(portal.getLocation().getChunkKey(), portal.getLocation());
        if (!portal.getId().isNull()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried removing portal " + portal.getLocation() + " with null id?");
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
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried removing portal " + portal.getLocation() + " with null id?");
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
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried linking portals with null id?");
            return false;
        }
        boolean save = false;
        save |= source.getLinks().add(target.getId());
        save |= target.getLinks().add(source.getId());

        portalLinks.put(source.getId(), target.getId());
        portalLinks.put(target.getId(), source.getId());

        if (save) {
            source.save();
            target.save();
        }
        return true;
    }

    public boolean unlinkPortals(Portal source, Portal target) {
        if (source.getId().isNull() || target.getId().isNull()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried unlinking portals with null id?");
            return false;
        }
        boolean save = false;
        save |= source.getLinks().remove(target.getId());
        save |= target.getLinks().remove(source.getId());

        portalLinks.remove(source.getId(), target.getId());
        portalLinks.remove(target.getId(), source.getId());

        if (save) {
            source.save();
            target.save();
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

        PlayerPortalEvent portalEvent = new PlayerPortalEvent(player, player.getLocation(), location,
                PlayerTeleportEvent.TeleportCause.PLUGIN, 0, false, 0);
        Bukkit.getPluginManager().callEvent(portalEvent);
        if (portalCooldown.contains(player)) {
            return;
        }
        if (!portalEvent.isCancelled()) {
            Location finalLocation = portalEvent.getTo();
            SettlementsPlugin.getInstance().getServerAdapter().getChunkAtAsync(location).thenAccept(c -> {
                if (c == null) {
                    Message.get("error.destination-obstructed").tell(player);
                    SettlementsPlugin.logDebug("Could not load chunk at " + finalLocation);
                    return;
                }
                Block block = finalLocation.getBlock();
                if (!block.isPassable() || !block.getRelative(BlockFace.UP).isPassable()) {
                    Message.get("error.destination-obstructed").tell(player);
                    SettlementsPlugin.logDebug("Location " + finalLocation + " is obstructed");
                    return;
                }
                Block down = block.getRelative(BlockFace.DOWN);
                while (!down.getType().isSolid() && down.getY() > down.getWorld().getMinHeight()) {
                    down = down.getRelative(BlockFace.DOWN);
                }
                if (!down.getType().isSolid()) {
                    Message.get("error.destination-obstructed").tell(player);
                    SettlementsPlugin.logDebug("Location " + finalLocation + " is has no floor");
                    return;
                }

                player.sendBlockChange(down.getLocation(), down.getBlockData());
                portalCooldown.add(player);
                Utils.runLater(new BukkitRunnable() {
                    @Override
                    public void run() {
                        portalCooldown.remove(player);
                    }
                }, 60);
                SettlementsPlugin.getInstance().getServerAdapter().teleportAsync(player, down.getRelative(BlockFace.UP).getLocation().add(0.5, 0, 0.5)).thenAccept(success -> {
                    if (!success) {
                        Message.get("error.destination-obstructed").tell(player);
                        SettlementsPlugin.logDebug("Failed to teleport " + player.getName() + " to " + finalLocation);
                    }
                });
            });
        }
    }

    public Collection<Portal> getPortals() {
        return portals.values();
    }
}

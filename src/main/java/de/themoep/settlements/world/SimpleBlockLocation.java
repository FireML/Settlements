package de.themoep.settlements.world;

import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class SimpleBlockLocation implements ConfigurationSerializable {
    private String world;
    private int x;
    private int y;
    private int z;
    private SimpleChunkLocation chunk;

    public SimpleBlockLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SimpleBlockLocation(Location location) {
        this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public SimpleBlockLocation(SimpleLocation location) {
        this(location.getWorld(),  NumberConversions.floor(location.getX()),  NumberConversions.floor(location.getY()), NumberConversions.floor(location.getZ()));
    }

    public SimpleBlockLocation(SimpleBlockLocation location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    public SimpleBlockLocation copy() {
        return new SimpleBlockLocation(this);
    }

    public Location toBukkitLocation() {
        World world = Bukkit.getWorld(this.world);
        if (world != null) {
            return new Location(world, x, y, z);
        }
        return null;
    }

    public SimpleChunkLocation toChunkLocation() {
        if (chunk == null) {
            chunk = new SimpleChunkLocation(getWorld(), getChunkX(), getChunkZ());
        }
        return chunk;
    }

    public boolean inSameChunk(SimpleBlockLocation location) {
        return location != null
                && world.equals(location.getWorld())
                && getChunkX() == location.getChunkX()
                && getChunkZ() == location.getChunkZ();
    }

    public int getChunkX() {
        return x >> 4;
    }

    public int getChunkZ() {
        return z >> 4;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SimpleBlockLocation location)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        return location.world.equals(world) && location.x == x && location.y == y && location.z == z;
    }

    @Override
    public int hashCode() {
        int hash = world.hashCode();
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + z;
        return hash;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", getX());
        map.put("y", getY());
        map.put("z", getZ());
        map.put("world", getWorld());
        return map;
    }

    public static SimpleBlockLocation deserialize(Map<String, Object> map) {
        if (map == null
                || map.get("x") == null || map.get("y") == null || map.get("z") == null
                || map.get("world") == null) {
            SettlementsPlugin.getInstance().getLogging().warn("Could not deserialize SimpleBlockLocation! (" + map + ")");
            return null;
        }
        return new SimpleBlockLocation(
                (String) map.get("world"),
                (int) map.get("x"),
                (int) map.get("y"),
                (int) map.get("z")
        );
    }

    public String getSimpleKey() {
        return getX() + "-" + getY() + "-" + getZ();
    }

    public String getKey() {
        return getWorld() + "-" + getSimpleKey();
    }

    public String getChunkKey() {
        return getChunkX() + "-" + getWorld() + "-" + getChunkZ();
    }

    public String getSimpleChunkKey() {
        return getChunkX() + "-" + getChunkZ();
    }

    @Override
    public String toString() {
        return "SimpleBlockLocation" + serialize();
    }
}

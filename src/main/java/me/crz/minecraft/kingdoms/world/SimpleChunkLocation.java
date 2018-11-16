package me.crz.minecraft.kingdoms.world;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class SimpleChunkLocation implements ConfigurationSerializable {
    private String world;
    private int x;
    private int z;

    public SimpleChunkLocation(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public SimpleChunkLocation(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public SimpleChunkLocation(Location location) {
        this(location.getWorld().getName(), (int) location.getX() >> 4, (int) location.getZ() >> 4);
    }

    public SimpleChunkLocation copy() {
        return new SimpleChunkLocation(world, x, z);
    }

    public Chunk getChunk() {
        World world = Bukkit.getWorld(this.world);
        if (world != null) {
            return world.getChunkAt(x, z);
        }
        return null;
    }
    public boolean contains(SimpleLocation location) {
        return location != null
                && world.equals(location.getWorld())
                && x == location.getChunkX()
                && z == location.getChunkZ();
    }

    public String getWorld() {
        return world;
    }

    protected void setWorld(String world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    protected void setX(int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    protected void setZ(int z) {
        this.z = z;
    }

    public SimpleChunkLocation add(int x, int z) {
        this.x += x;
        this.z += z;
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SimpleChunkLocation)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        SimpleChunkLocation location = (SimpleChunkLocation) other;
        return location.world.equals(world) && location.x == x && location.z == z;
    }

    @Override
    public int hashCode() {
        int hash = world.hashCode();
        hash = hash * 31 + x;
        hash = hash * 31 + z;
        return hash;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", getX());
        map.put("z", getZ());
        map.put("world", getWorld());
        return map;
    }

    public static SimpleChunkLocation deserialize(Map<String, Object> map) {
        if (map == null || map.get("x") == null || map.get("z") == null || map.get("world") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize SimpleBlockLocation! (" + map + ")");
            return null;
        }
        return new SimpleChunkLocation(
                (String) map.get("world"),
                (int) map.get("x"),
                (int) map.get("z")
        );
    }

    public String getKey() {
        return x + "-" + world + "-" + z;
    }

    public String getSimpleKey() {
        return x + "-" + z;
    }

    public Location getCenterBukkitLocation() {
        World world = Bukkit.getWorld(getWorld());
        int x = getX() * 16 + 8;
        int z = getX() * 16 + 8;
        return new Location(world, x, world.getHighestBlockYAt(x, z), z).toCenterLocation();
    }
}

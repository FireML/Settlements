package me.crz.minecraft.kingdoms.world;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class SimpleLocation implements ConfigurationSerializable {
    private String world;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;
    private SimpleChunkLocation chunk;

    public SimpleLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SimpleLocation(String world, double x, double y, double z) {
        this(world, x, y, z, 0, 0);
    }

    public SimpleLocation(Location location) {
        this(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public SimpleLocation(SimpleLocation location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public SimpleLocation copy() {
        return new SimpleLocation(this);
    }

    public void copy(SimpleLocation other) {
        this.world = other.getWorld();
        this.x = other.getX();
        this.y = other.getY();
        this.z = other.getZ();
        this.pitch = other.getPitch();
        this.yaw = other.getYaw();
        this.chunk = other.toChunkLocation();
    }

    public Location toBukkitLocation() {
        World world = Bukkit.getWorld(this.world);
        if (world != null) {
            return new Location(world, x, y, z, yaw, pitch);
        }
        return null;
    }

    public SimpleChunkLocation toChunkLocation() {
        if (chunk == null) {
            chunk = new SimpleChunkLocation(getWorld(), getChunkX(), getChunkZ());
        }
        return chunk;
    }

    public boolean inSameChunk(SimpleLocation location) {
        return location != null
                && world != null && world.equals(location.getWorld())
                && getChunkX() == location.getChunkX()
                && getChunkZ() == location.getChunkZ();
    }

    public int getChunkX() {
        return (int) x >> 4;
    }

    public int getChunkZ() {
        return (int) z >> 4;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public int getBlockX() {
        return Location.locToBlock(getX());
    }

    public int getBlockY() {
        return Location.locToBlock(getY());
    }

    public int getBlockZ() {
        return Location.locToBlock(getZ());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SimpleLocation)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        SimpleLocation location = (SimpleLocation) other;
        if (location.world == null) {
            return world == null;
        }
        return location.world.equals(world) && location.x == x && location.y == y && location.z == z && location.yaw == yaw && location.pitch == pitch;
    }

    @Override
    public int hashCode() {
        int hash = world != null ? world.hashCode() : 0;
        hash = hash * 31 + Double.hashCode(x);
        hash = hash * 31 + Double.hashCode(y);
        hash = hash * 31 + Double.hashCode(z);
        hash = hash * 31 + Float.hashCode(yaw);
        hash = hash * 31 + Float.hashCode(pitch);
        return hash;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", getX());
        map.put("y", getY());
        map.put("z", getZ());
        map.put("yaw", getYaw());
        map.put("pitch", getPitch());
        map.put("world", getWorld());
        return map;
    }

    public static SimpleLocation deserialize(Map<String, Object> map) {
        if (map == null
                || map.get("x") == null || map.get("y") == null || map.get("z") == null
                || map.get("yaw") == null || map.get("pitch") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize SimpleLocation! (" + map + ")");
            return null;
        }
        if (map.get("world") == null) {
            return null;
        }
        return new SimpleLocation(
                (String) map.get("world"),
                (double) map.get("x"),
                (double) map.get("y"),
                (double) map.get("z"),
                Utils.getFloat(map.get("yaw")),
                Utils.getFloat(map.get("pitch"))
        );
    }

    @Override
    public String toString() {
        return "SimpleLocation" + serialize().toString();
    }
}

package me.crz.minecraft.kingdoms.world;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SimpleLocation {
    private String world;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

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

    public static SimpleLocation load(Document document) {
        if (document != null
                && document.get("x") != null && document.get("y") != null  && document.get("y") != null
                && document.get("yaw") != null && document.get("pitch") != null && document.get("worldName") != null) {
            return new SimpleLocation(
                    document.getString("worldName"),
                    document.getDouble("x"),
                    document.getDouble("y"),
                    document.getDouble("z"),
                    (float) document.get("yaw"),
                    (float) document.get("pitch")
            );
        }
        return null;
    }

    public static Document save(SimpleLocation location, Document document) {
        if (document == null) {
            document = new Document();
        }
        if (location != null) {
            document.put("x", location.getX());
            document.put("y", location.getY());
            document.put("z", location.getZ());
            document.put("yaw", location.getYaw());
            document.put("pitch", location.getPitch());
            document.put("worldName", location.getWorld());
        } else {
            document.put("x", null);
            document.put("y", null);
            document.put("z", null);
            document.put("yaw", null);
            document.put("pitch", null);
            document.put("worldName", null);
        }
        return document;
    }

    public SimpleLocation copy() {
        return new SimpleLocation(this);
    }

    public Location toBukkitLocation() {
        World world = Bukkit.getWorld(this.world);
        if (world != null) {
            return new Location(world, x, y, z, yaw, pitch);
        }
        return null;
    }

    public SimpleChunkLocation toChunkLocation() {
        return new SimpleChunkLocation(this);
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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SimpleLocation)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        SimpleLocation location = (SimpleLocation) other;
        return location.world.equals(world) && location.x == x && location.y == y && location.z == z && location.yaw == yaw && location.pitch == pitch;
    }

    @Override
    public int hashCode() {
        int hash = world.hashCode();
        hash = hash * 31 + Double.hashCode(x);
        hash = hash * 31 + Double.hashCode(y);
        hash = hash * 31 + Double.hashCode(z);
        hash = hash * 31 + Float.hashCode(yaw);
        hash = hash * 31 + Float.hashCode(pitch);
        return hash;
    }
}

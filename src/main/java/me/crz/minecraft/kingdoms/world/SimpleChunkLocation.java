package me.crz.minecraft.kingdoms.world;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class SimpleChunkLocation {
    private String world;
    private int x;
    private int z;

    public SimpleChunkLocation(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public SimpleChunkLocation(Location location) {
        this(location.getWorld().getName(), (int) location.getX() >> 4, (int) location.getZ() >> 4);
    }

    public SimpleChunkLocation(SimpleLocation location) {
        this(location.getWorld(), (int) location.getX() >> 4, (int) location.getZ() >> 4);
    }

    public static SimpleChunkLocation load(Document document) {
        if (document != null
                && document.get("x") != null && document.get("y") != null && document.get("worldName") != null) {
            return new SimpleChunkLocation(
                    document.getString("worldName"),
                    document.getInteger("x"),
                    document.getInteger("z")
            );
        }
        return null;
    }

    public static Document save(SimpleChunkLocation location, Document document) {
        if (document == null) {
            document = new Document();
        }
        if (location != null) {
            document.put("x", location.getX());
            document.put("z", location.getZ());
            document.put("worldName", location.getWorld());
        } else {
            document.put("x", null);
            document.put("z", null);
            document.put("worldName", null);
        }
        return document;
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

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
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

    public String getKey() {
        return x + "-" + world + "-" + z;
    }
}

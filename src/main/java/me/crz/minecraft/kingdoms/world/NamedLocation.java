package me.crz.minecraft.kingdoms.world;

import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;

public class NamedLocation extends SimpleLocation {
    private String name;

    public NamedLocation(String name, String world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
        this.name = name;
    }

    public NamedLocation(String name, String world, double x, double y, double z) {
        super(world, x, y, z);
        this.name = name;
    }

    public NamedLocation(String name, Location location) {
        super(location);
        this.name = name;
    }

    public NamedLocation(String name, SimpleLocation location) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + super.hashCode();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("name", getName());
        return map;
    }

    public static NamedLocation deserialize(Map<String, Object> map) {
        SimpleLocation simpleLocation = SimpleLocation.deserialize(map);
        if (simpleLocation == null || map.get("name") == null) {
            return null;
        }
        return new NamedLocation((String) map.get("name"), simpleLocation);
    }

}

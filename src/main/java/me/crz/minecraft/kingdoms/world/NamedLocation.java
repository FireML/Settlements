package me.crz.minecraft.kingdoms.world;

import org.bson.Document;
import org.bukkit.Location;

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

    public static NamedLocation load(Document document) {
        if (document.containsKey("name")) {
            SimpleLocation location = SimpleLocation.load(document);
            return new NamedLocation(document.getString("name"), location);
        }
        return null;
    }
}

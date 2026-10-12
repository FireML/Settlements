package de.themoep.settlements.portal;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.WorldUtils;
import de.themoep.settlements.world.SimpleBlockLocation;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Portal implements DatabaseSerializable {
    private IdObject id = IdObject.get(null);

    private final SimpleBlockLocation location;
    private final BlockFace direction;

    private Set<IdObject> links = new LinkedHashSet<>();


    public Portal(SimpleBlockLocation location, BlockFace direction) {
        this.location = location;
        this.direction = direction;
    }

    @Override
    public IdObject getId() {
        return id;
    }

    @Override
    public void setId(IdObject id) {
        this.id = id;
    }

    @Override
    public void save() {
        DatabaseManager.getInstance().savePortal(this);
    }

    public SimpleBlockLocation getLocation() {
        return location;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public Set<IdObject> getLinks() {
        return links;
    }

    public String[] getReplacements() {
        return new String[] {
                "x", String.valueOf(getLocation().getX()),
                "y", String.valueOf(getLocation().getY()),
                "z", String.valueOf(getLocation().getZ()),
                "world", getLocation().getWorld(),
                "direction", String.valueOf(getDirection()),
                "links", String.valueOf(getLinks().size())
        };
    }

    @Override
    public @NotNull LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("world", getLocation().getWorld());
        map.put("x", getLocation().getX());
        map.put("y", getLocation().getY());
        map.put("z", getLocation().getZ());
        map.put("direction", String.valueOf(getDirection().name().charAt(0)));
        map.put("links", getLinks());
        return map;
    }

    public static Portal deserialize(Map<String, Object> map) {
        if (map == null || map.get("world") == null || map.get("x") == null || map.get("y") == null || map.get("z") == null || map.get("direction") == null) {
            SettlementsPlugin.getInstance().getLogging().warn("Could not deserialize Portal! (" + map + ")");
            return null;
        }
        Portal portal = new Portal(SimpleBlockLocation.deserialize(map), WorldUtils.getBlockFace((String) map.get("direction")));
        portal.setId(IdObject.get(map));
        for (Integer id : ((Collection<Integer>) map.get("links"))) {
            portal.getLinks().add(IdObject.get(id));
        }
        return portal;
    }

    @Override
    public int hashCode() {
        if (id.isNull()) {
            return getLocation().hashCode();
        } else {
            return id.hashCode();
        }
    }

    @Override
    public String name() {
        return "Portal";
    }
}

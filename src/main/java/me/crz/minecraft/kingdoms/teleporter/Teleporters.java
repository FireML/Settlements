package me.crz.minecraft.kingdoms.teleporter;

import me.crz.minecraft.kingdoms.utils.IdObject;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.HashMap;
import java.util.Map;

public class Teleporters {
    private static Teleporters instance;
    private Map<IdObject, PrivateTeleportersGUI> privateTeleporters = new HashMap<>();

    public Teleporters() {
        super();
    }

    public PrivateTeleportersGUI getPrivateTeleporters(final Village v, final Player p) {
        if (v == null) {
            return null;
        }
        if (privateTeleporters.containsKey(v.getId())) {
            return privateTeleporters.get(v.getId());
        }
        final PrivateTeleportersGUI teleporter = new PrivateTeleportersGUI(v);
        privateTeleporters.put(v.getId(), teleporter);
        return teleporter;
    }

    synchronized public static Teleporters getInstance() {
        if (instance == null) {
            instance = new Teleporters();
        }
        return instance;
    }
}

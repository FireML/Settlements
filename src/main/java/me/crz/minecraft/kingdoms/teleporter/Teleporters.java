package me.crz.minecraft.kingdoms.teleporter;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.HashMap;
import java.util.Map;

public class Teleporters
{
    private static Teleporters instance;
    Map<String, PrivateTeleportersGUI> privateTeleporters;
    
    public Teleporters() {
        super();
        privateTeleporters = new HashMap<String, PrivateTeleportersGUI>();
    }
    
    public PrivateTeleportersGUI getPrivateTeleporters(final Village v, final Player p) {
        if (v == null) {
            return null;
        }
        if (privateTeleporters.containsKey(v.getName())) {
            return privateTeleporters.get(v.getName());
        }
        final PrivateTeleportersGUI teleporter = new PrivateTeleportersGUI(v);
        privateTeleporters.put(v.getName(), teleporter);
        return teleporter;
    }
    
    public static Teleporters getInstance() {
        if (instance == null) {
            instance = new Teleporters();
        }
        return instance;
    }
}

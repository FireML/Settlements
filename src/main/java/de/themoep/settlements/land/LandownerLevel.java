package de.themoep.settlements.land;

import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.npc.NpcType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public abstract class LandownerLevel extends LevelInfo {
    private Set<NpcType> npcs = new LinkedHashSet<>();
    private final int missions;


    public LandownerLevel(int level, ConfigurationSection config) {
        super(level, config);
        missions = config.getInt("missions", 0);
        for (String npc : config.getStringList("npcs")) {
            npcs.add(NpcType.valueOf(npc.replace('-', '_').toUpperCase(Locale.ROOT)));
        }
    }

    public Set<NpcType> getNPCs() {
        return npcs;
    }

    public int getMissions() {
        return missions;
    }
}

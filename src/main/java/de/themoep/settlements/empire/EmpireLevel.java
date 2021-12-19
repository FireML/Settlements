package de.themoep.settlements.empire;

import de.themoep.settlements.land.LandownerLevel;
import org.bukkit.configuration.ConfigurationSection;

public class EmpireLevel extends LandownerLevel {
    private final long upkeep;
    private final long upkeepReduction;
    private final int worldRadius;

    public EmpireLevel(int level, ConfigurationSection config) {
        super(level, config);
        upkeep = config.getLong("upkeep", 0);
        upkeepReduction = config.getLong("upkeep-reduction", 0);
        worldRadius = config.getInt("world-radius", 0);
    }

    public long getUpkeep() {
        return upkeep;
    }

    public long getUpkeepReduction() {
        return upkeepReduction;
    }

    public int getWorldRadius() {
        return worldRadius;
    }

    @Override
    public String toString() {
        return "EmpireLevel{" +
                "level=" + getLevel() +
                ", requirements=" + getRequirements() +
                ", upkeep=" + upkeep +
                ", upkeepReduction=" + upkeepReduction +
                ", worldRadius=" + worldRadius +
                ", missions=" + getMissions() +
                ", npcs=" + getNPCs() +
                '}';
    }
}

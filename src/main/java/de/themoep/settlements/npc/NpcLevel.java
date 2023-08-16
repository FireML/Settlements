package de.themoep.settlements.npc;

import de.themoep.settlements.leveling.LevelInfo;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class NpcLevel extends LevelInfo {
    private final long upkeep;
    private final Set<String> perks = new LinkedHashSet<>();

    public NpcLevel(int level, NpcLevel previous, ConfigurationSection config) {
        super(level, previous, config);
        upkeep = config.getLong("upkeep", 0);
        ConfigurationSection perksConfig = config.getConfigurationSection("perks");
        if (perksConfig != null) {
            for (String perk : perksConfig.getKeys(false)) {
                perks.add(perk.toLowerCase(Locale.ROOT));
            }
        }
    }

    public long getUpkeep() {
        return upkeep;
    }

    public Set<String> getPerks() {
        return perks;
    }

    @Override
    public String toString() {
        return "NpcLevel{" +
                "level=" + getLevel() +
                ", previous=" + (getPrevious() != null ? String.valueOf(getPrevious().getLevel()) : "none") +
                ", requirements=" + getRequirements() +
                ", upkeep=" + upkeep +
                ", perks=" + perks +
                '}';
    }
}

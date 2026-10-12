package de.themoep.settlements.land;

import de.themoep.settlements.leveling.LevelInfo;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class LandownerLevel extends LevelInfo {
    protected final long claimUpkeep;
    protected final Map<String, Long> worldClaimUpkeep = new LinkedHashMap<>();
    private final int chunks;
    private final int missions;
    private final int size;

    public LandownerLevel(int level, LandownerLevel previous, ConfigurationSection config) {
        super(level, previous, config);
        chunks = config.getInt("chunks", 0);
        missions = config.getInt("missions", 0);
        claimUpkeep = config.getLong("claim-upkeep", 0);
        ConfigurationSection worldUpkeepSection = config.getConfigurationSection("per-world-upkeep");
        if (worldUpkeepSection != null) {
            for (String world : worldUpkeepSection.getKeys(false)) {
                worldClaimUpkeep.put(world, worldUpkeepSection.getLong(world, claimUpkeep));
            }
        }
        size = config.getInt("size", -1);
    }

    public int getChunks() {
        return chunks;
    }

    public long getClaimUpkeep(String world) {
        return worldClaimUpkeep.getOrDefault(world, claimUpkeep);
    }

    public int getMissions() {
        return missions;
    }

    public int getSize() {
        return size;
    }

    public LandownerLevel getPrevious() {
        return (LandownerLevel) super.getPrevious();
    }
}

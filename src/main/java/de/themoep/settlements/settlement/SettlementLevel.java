package de.themoep.settlements.settlement;

import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SettlementLevel {
    protected final long claimUpkeep;
    protected final Map<String, Long> worldClaimUpkeep = new LinkedHashMap<>();
    private final int outposts;
    private final int extraMissions;
    private final int chunks;
    private final int missions;
    private final int size;
    private final int level;
    private final SettlementLevel previous;
    private final Map<String, Long> requirements = new LinkedHashMap<>();

    public SettlementLevel(int level, SettlementLevel previous, ConfigurationSection config) {
        this.level = level;
        this.previous = previous;
        ConfigurationSection requirementsConfig = null;
        if (config != null) {
            requirementsConfig = config.getConfigurationSection("requirements");
        }
        if (requirementsConfig != null) {
            for (String key : requirementsConfig.getKeys(false)) {
                long value = requirementsConfig.getLong(key, -1);
                if (value > 0) {
                    requirements.put(key.toUpperCase(Locale.ROOT), value);
                }
            }
        }
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
        outposts = config.getInt("outposts", 0);
        extraMissions = config.getInt("extra-missions", 0);
    }

    public int getOutposts() {
        return outposts;
    }

    public int getExtraMissions() {
        return extraMissions;
    }

    @Override
    public String toString() {
        return "SettlementLevel{" +
                "level=" + getLevel() +
                ", previous=" + (getPrevious() != null ? String.valueOf(getPrevious().getLevel()) : "none") +
                ", requirements=" + getRequirements() +
                ", chunks=" + getChunks() +
                ", outposts=" + outposts +
                ", claimUpkeep=" + claimUpkeep +
                ", worldClaimUpkeep=" + worldClaimUpkeep +
                ", missions=" + getMissions() +
                ", extraMissions=" + extraMissions +
                '}';
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

    public SettlementLevel getPrevious() {
        return previous;
    }

    public int getLevel() {
        return level;
    }

    public Map<String, Long> getRequirements() {
        return requirements;
    }
}

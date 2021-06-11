package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.leveling.LevelInfo;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;

public class VillageLevel extends LevelInfo {
    private final int chunks;
    private final int outposts;
    private final long claimUpkeep;
    private final Map<String, Long> worldClaimUpkeep = new LinkedHashMap<>();

    public VillageLevel(int level, ConfigurationSection config) {
        super(level, config);
        outposts = config.getInt("outposts", 0);
        chunks = config.getInt("chunks", 0);
        claimUpkeep = config.getLong("claim-upkeep", 0);
        ConfigurationSection worldUpkeepSection = config.getConfigurationSection("per-world-upkeep");
        if (worldUpkeepSection != null) {
            for (String world : worldUpkeepSection.getKeys(false)) {
                worldClaimUpkeep.put(world, worldUpkeepSection.getLong(world, claimUpkeep));
            }
        }
    }

    public int getChunks() {
        return chunks;
    }

    public int getOutposts() {
        return outposts;
    }

    public long getClaimUpkeep(String world) {
        return worldClaimUpkeep.getOrDefault(world, claimUpkeep);
    }

    @Override
    public String toString() {
        return "VillageLevel{" +
                "level=" + getLevel() +
                ", requirements=" + getRequirements() +
                ", chunks=" + chunks +
                ", outposts=" + outposts +
                ", claimUpkeep=" + claimUpkeep +
                ", worldClaimUpkeep=" + worldClaimUpkeep +
                '}';
    }
}

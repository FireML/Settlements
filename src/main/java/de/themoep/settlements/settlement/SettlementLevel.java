package de.themoep.settlements.settlement;

import de.themoep.settlements.land.LandownerLevel;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;

public class SettlementLevel extends LandownerLevel {
    private final int outposts;
    private final int extraMissions;

    public SettlementLevel(int level, ConfigurationSection config) {
        super(level, config);
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
                ", requirements=" + getRequirements() +
                ", chunks=" + getChunks() +
                ", outposts=" + outposts +
                ", claimUpkeep=" + claimUpkeep +
                ", worldClaimUpkeep=" + worldClaimUpkeep +
                ", missions=" + getMissions() +
                ", extraMissions=" + extraMissions +
                ", npcs=" + getNPCs() +
                '}';
    }
}

package de.themoep.settlements.perk;

import de.themoep.settlements.npc.NpcData;
import org.bukkit.configuration.ConfigurationSection;

public class NpcPerk extends LevelPerk<NpcData> {
    public NpcPerk(ConfigurationSection config) throws IllegalArgumentException {
        super(config);
    }
}

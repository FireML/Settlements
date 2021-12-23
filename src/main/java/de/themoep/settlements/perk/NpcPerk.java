package de.themoep.settlements.perk;

import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcType;
import org.bukkit.configuration.ConfigurationSection;

public class NpcPerk extends LevelPerk<NpcData> {
    private final NpcType npcType;

    public NpcPerk(NpcType npcType, ConfigurationSection config) throws IllegalArgumentException {
        super(config);
        this.npcType = npcType;
    }

    @Override
    public String getTag() {
        return npcType.getTag() + " NPC";
    }
}

package de.themoep.settlements.npc;

import de.themoep.settlements.leveling.LevelInfo;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class NpcLevel extends LevelInfo {

    private Set<String> perks = new LinkedHashSet<>();

    public NpcLevel(int level, ConfigurationSection config) {
        super(level, config);
        for (String perk : config.getStringList("perks")) {
            perks.add(perk.toLowerCase(Locale.ROOT));
        }
    }

    public Set<String> getPerks() {
        return perks;
    }
}

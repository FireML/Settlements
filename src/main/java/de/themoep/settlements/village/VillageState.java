package de.themoep.settlements.village;

import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.rank.Rankable;

public enum VillageState implements NameTag, Rankable<VillageState> {
    KINGDOM,
    VILLAGE;
}

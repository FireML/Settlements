package de.themoep.settlements.land;

import de.themoep.settlements.rank.Rankable;
import de.themoep.settlements.utils.NameTag;

public enum LandAccess implements NameTag, Rankable {
    PUBLIC,
    TRUSTED,
    ALLY,
    PRIVATE;
}

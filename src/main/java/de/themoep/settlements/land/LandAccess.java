package de.themoep.settlements.land;

import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.rank.Rankable;

public enum LandAccess implements NameTag, Rankable {
    PUBLIC,
    TRUSTED,
    ALLY,
    PRIVATE;
}

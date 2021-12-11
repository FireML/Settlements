package de.themoep.settlements.settlement;

import de.themoep.settlements.rank.Rankable;
import de.themoep.settlements.utils.NameTag;

public enum JoinType implements NameTag, Rankable {
    INVITE_ONLY,
    ANYONE,
    APPLY
}

package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.rank.Rankable;

public enum VillageAccess implements NameTag, Rankable {
    OPEN,
    PUBLIC,
    TRUSTED,
    ALLY,
    PRIVATE;
}

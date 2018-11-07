package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.rank.Rankable;

public enum LandAccess implements NameTag, Rankable {
    OPEN,
    PUBLIC,
    TRUSTED,
    ALLY,
    PRIVATE;
}

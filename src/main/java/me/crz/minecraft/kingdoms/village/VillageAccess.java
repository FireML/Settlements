package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Rankable;

public enum VillageAccess implements NameTag, Rankable {
    OPEN,
    PUBLIC,
    ALLY,
    PRIVATE;
}

package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.rank.Rankable;
import me.crz.minecraft.kingdoms.utils.NameTag;

public enum EmpireAccess implements NameTag, Rankable {
    OPEN,
    PUBLIC,
    ALLY,
    PRIVATE;
}

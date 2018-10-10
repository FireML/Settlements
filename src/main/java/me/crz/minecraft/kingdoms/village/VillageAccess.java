package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.utils.HumanReadable;
import me.crz.minecraft.kingdoms.utils.Rankable;

public enum VillageAccess implements HumanReadable, Rankable {
    OPEN,
    PUBLIC,
    ALLY,
    PRIVATE;
}

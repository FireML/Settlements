package me.crz.minecraft.kingdoms.integration;

import me.crz.minecraft.kingdoms.village.Resident;

public interface AfkProvider {
    boolean isAfk(Resident resident);
}

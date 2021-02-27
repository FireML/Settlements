package me.crz.minecraft.kingdoms.integration;

import me.crz.minecraft.kingdoms.resident.Resident;

public interface AfkProvider {
    boolean isAfk(Resident resident);
}

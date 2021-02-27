package me.crz.minecraft.kingdoms.integration;

import me.crz.minecraft.kingdoms.resident.Resident;

import java.util.UUID;

public interface IgnoreProvider {
    void addIgnore(Resident resident, UUID ignoredId);

    void removeIgnore(Resident resident, UUID ignoredId);
}

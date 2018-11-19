package me.crz.minecraft.kingdoms.integration;

import me.crz.minecraft.kingdoms.village.Resident;

import java.util.UUID;

public interface IgnoreProvider {
    void addIgnore(Resident resident, UUID ignoredId);

    void removeIgnore(Resident resident, UUID ignoredId);
}

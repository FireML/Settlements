package de.themoep.settlements.integration;

import de.themoep.settlements.resident.Resident;

import java.util.UUID;

public interface IgnoreProvider {
    void addIgnore(Resident resident, UUID ignoredId);

    void removeIgnore(Resident resident, UUID ignoredId);
}

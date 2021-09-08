package de.themoep.settlements.integration;

import de.themoep.settlements.resident.Resident;

public interface AfkProvider {
    boolean isAfk(Resident resident);
}

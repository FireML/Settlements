package de.themoep.settlements.integration;

import de.themoep.settlements.resident.Resident;

public interface MailProvider {
    void sendMail(String sender, Resident resident, String message);
}

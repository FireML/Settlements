package me.crz.minecraft.kingdoms.integration;

import me.crz.minecraft.kingdoms.resident.Resident;

public interface MailProvider {
    void sendMail(String sender, Resident resident, String message);
}

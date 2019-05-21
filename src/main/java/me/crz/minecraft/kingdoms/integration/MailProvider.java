package me.crz.minecraft.kingdoms.integration;

import me.crz.minecraft.kingdoms.village.Resident;

public interface MailProvider {
    void sendMail(String sender, Resident resident, String message);
}

package me.crz.minecraft.kingdoms.integration;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Containers.PlayerMail;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.UUID;

public class CmiIntegration implements IgnoreProvider, AfkProvider, MailProvider {

    private final CMI cmi;

    public CmiIntegration() {
        KingdomsPlugin.getInstance().getIgnoreProviders().add(this);
        KingdomsPlugin.getInstance().getAfkProviders().add(this);
        KingdomsPlugin.getInstance().getMailProviders().add(this);
        cmi = CMI.getInstance();
    }

    public CMI getCmi() {
        return cmi;
    }

    @Override
    public void addIgnore(Resident resident, UUID ignoredId) {
        CMIUser user = cmi.getPlayerManager().getUser(resident.getUUID());
        if (user != null) {
            user.addIgnore(ignoredId, true);
        }
    }

    @Override
    public void removeIgnore(Resident resident, UUID ignoredId) {
        CMIUser user = cmi.getPlayerManager().getUser(resident.getUUID());
        if (user != null) {
            user.removeIgnore(ignoredId);
            user.addForDelayedSave();
        }
    }

    @Override
    public boolean isAfk(Resident resident) {
        CMIUser user = cmi.getPlayerManager().getUser(resident.getUUID());
        return user != null && user.isAfk();
    }

    @Override
    public void sendMail(String sender, Resident receiver, String message) {
        CMIUser user = cmi.getPlayerManager().getUser(receiver.getUUID());
        if (user != null) {
            user.addMail(new PlayerMail(sender, System.currentTimeMillis(), message), true);
        }
    }
}

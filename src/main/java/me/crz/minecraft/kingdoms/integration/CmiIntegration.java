package me.crz.minecraft.kingdoms.integration;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.UUID;

public class CmiIntegration implements IgnoreProvider {

    private final CMI cmi;

    public CmiIntegration() {
        KingdomsPlugin.getInstance().getIgnoreProviders().add(this);
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
}

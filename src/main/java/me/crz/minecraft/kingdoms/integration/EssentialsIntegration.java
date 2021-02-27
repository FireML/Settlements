package me.crz.minecraft.kingdoms.integration;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.StringUtil;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.resident.Resident;

import java.util.UUID;

import static com.earth2me.essentials.I18n.tl;

public class EssentialsIntegration implements IgnoreProvider, AfkProvider, MailProvider {

    private final Essentials essentials;

    public EssentialsIntegration() {
        essentials = (Essentials) KingdomsPlugin.getInstance().getServer().getPluginManager().getPlugin("Essentials");
        KingdomsPlugin.getInstance().getIgnoreProviders().add(this);
        KingdomsPlugin.getInstance().getAfkProviders().add(this);
        KingdomsPlugin.getInstance().getMailProviders().add(this);
    }

    public Essentials getEssentials() {
        return essentials;
    }

    @Override
    public void addIgnore(Resident resident, UUID ignoredId) {
        User user = essentials.getUser(resident.getUUID());
        if (user != null) {
            User toUnignore = essentials.getUser(ignoredId);
            if (toUnignore != null) {
                user.setIgnoredPlayer(toUnignore, true);
            }
        }
    }

    @Override
    public void removeIgnore(Resident resident, UUID ignoredId) {
        User user = essentials.getUser(resident.getUUID());
        if (user != null) {
            User toUnignore = essentials.getUser(ignoredId);
            if (toUnignore != null) {
                user.setIgnoredPlayer(toUnignore, false);
            }
        }
    }

    @Override
    public boolean isAfk(Resident resident) {
        User user = essentials.getUser(resident.getUUID());
        return user != null && user.isAfk();
    }

    @Override
    public void sendMail(String sender, Resident receiver, String message) {
        User user = essentials.getUser(receiver.getUUID());
        if (user != null) {
            user.addMail(tl("mailMessage", (tl("mailFormat", sender, StringUtil.sanitizeString(FormatUtil.stripFormat(message))))));
        }
    }
}

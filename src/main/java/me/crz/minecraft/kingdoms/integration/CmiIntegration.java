package me.crz.minecraft.kingdoms.integration;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Containers.PlayerMail;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.landflags.LandFlagBase;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;
import java.util.logging.Level;

public class CmiIntegration implements IgnoreProvider, AfkProvider, MailProvider, Listener, LandFlagBase {

    private final CMI cmi;

    public CmiIntegration() {
        cmi = CMI.getInstance();
        if (cmi.getConfigManager().getOptimizationsSimilarCommandChecker() > 0) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "You have OptimizationsSimilarCommandChecker enabled in your CMI config." +
                    " If you want 100% from certain CMI commands then you will have to disable that!" +
                    " Unfortunately CMI does not provide a proper API to handle that securely otherwise.");
        }
        KingdomsPlugin.getInstance().getIgnoreProviders().add(this);
        KingdomsPlugin.getInstance().getAfkProviders().add(this);
        KingdomsPlugin.getInstance().getMailProviders().add(this);
        KingdomsPlugin.getInstance().getServer().getPluginManager().registerEvents(this, KingdomsPlugin.getInstance());
    }

    public CMI getCmi() {
        return cmi;
    }

    /**
     * Block CMI commands that don't have events -.-
     * @param event The pre-process event to listen on
     */
    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String[] split = event.getMessage().substring(1).split(" ");
        PluginCommand command = KingdomsPlugin.getInstance().getCommand(split[0]);
        if (command != null && "cmi".equals(command.getName()) && command.getPlugin() == cmi) {
            if (split.length > 2 && "itemframe".equalsIgnoreCase(split[1])) {
                Entity targeted = cmi.getUtilManager().getTargetEntity(event.getPlayer(), 0.0D);
                if (targeted != null && targeted.getType() == EntityType.ITEM_FRAME) {
                    Pair<Boolean, String> response = this.getCanBuildAll(event.getPlayer(), targeted.getLocation());
                    if (!response.getLeft()) {
                        MessageUtils.tell(event.getPlayer(), response.getRight());
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @Override
    public void addIgnore(Resident resident, UUID ignoredId) {
        CMIUser user = cmi.getPlayerManager().getUser(resident.getUUID());
        if (user != null) {
            user.addIgnore(ignoredId, true);
            user.addForDelayedSave();
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
            user.addForDelayedSave();
        }
    }
}

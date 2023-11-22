package de.themoep.settlements.integration;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Containers.PlayerMail;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandAccessChecker;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.FormattedCommandAlias;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import uk.firedev.poleislib.Loggers;
import uk.firedev.poleislib.utils.ComponentUtils;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

public class CmiIntegration implements IgnoreProvider, AfkProvider, MailProvider, Listener, LandAccessChecker {

    private final CMI cmi;
    private Field formatStringsField = null;

    public CmiIntegration() {
        cmi = CMI.getInstance();
        if (cmi.getConfigManager().getOptimizationsSimilarCommandChecker() > 0) {
            Loggers.log(Level.WARNING, SettlementsPlugin.getInstance().getLogger(), "You have OptimizationsSimilarCommandChecker enabled in your CMI config." +
                    " If you want 100% from certain CMI commands then you will have to disable that!" +
                    " Unfortunately CMI does not provide a proper API to handle that securely otherwise.");
        }
        SettlementsPlugin.getInstance().getIgnoreProviders().add(this);
        SettlementsPlugin.getInstance().getAfkProviders().add(this);
        SettlementsPlugin.getInstance().getMailProviders().add(this);
        SettlementsPlugin.getInstance().registerListener(this);
        try {
            formatStringsField = FormattedCommandAlias.class.getDeclaredField("formatStrings");
            formatStringsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Loggers.logException(e, SettlementsPlugin.getInstance().getLogger());
        }
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
        Command command = Bukkit.getCommandMap().getCommand(split[0]);
        if (command != null) {
            if (command instanceof PluginCommand) {
                if (!isCmiItemframeCommand(event.getMessage().substring(1))) {
                    return;
                }
            } else if (command instanceof FormattedCommandAlias && formatStringsField != null) {
                try {
                    String[] formatStrings = (String[]) formatStringsField.get(command);
                    boolean found = false;
                    for (String formatString : formatStrings) {
                        if (isCmiItemframeCommand(formatString)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return;
                    }
                } catch (IllegalAccessException e) {
                    Loggers.logException(e, SettlementsPlugin.getInstance().getLogger());
                }
            } else {
                return;
            }

            Entity targeted = cmi.getUtilManager().getTargetEntity(event.getPlayer(), 0.0D);
            if (targeted != null && targeted.getType() == EntityType.ITEM_FRAME) {
                Pair<Boolean, String> response = this.getCanBuildAll(event.getPlayer(), targeted.getLocation());
                if (!response.getLeft()) {
                    MessageUtils.tell(event.getPlayer(), ComponentUtils.parseComponent(response.getRight()));
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isCmiItemframeCommand(String commandString) {
        if (commandString.startsWith("/")) {
            commandString = commandString.substring(1);
        }
        String[] split = commandString.split(" ");
        PluginCommand command = SettlementsPlugin.getInstance().getServer().getPluginCommand(split[0]);
        return command != null && command.getPlugin() == cmi && split.length > 2 && "itemframe".equalsIgnoreCase(split[1]);
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

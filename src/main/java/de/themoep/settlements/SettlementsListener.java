package de.themoep.settlements;

import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class SettlementsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLoginCheck(AsyncPlayerPreLoginEvent event) {
        if (!SettlementsPlugin.getInstance().isRunning() && event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server not fully started yet!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // try to load the data and if it doesn't exist create it
            Resident res = ResidentManager.getInstance().getResident(event.getUniqueId());
            if (res == null) {
                res = new Resident(event.getUniqueId(), event.getName());
                res.updateLastLogin();
                ResidentManager.getInstance().addResident(res);
                DatabaseManager.getInstance().saveResidentNow(res);
            } else {
                res.updateLastLogin();
                if (!res.getName().equalsIgnoreCase(event.getName())) {
                    ResidentManager.getInstance().invalidateName(res.getName());
                    res.setName(event.getName());
                    ResidentManager.getInstance().addResident(res);
                }
                if (Config.Chat.ENABLED && Config.Chat.RESET_CHANNELS_ON_LOGIN && res.isListening(ChatChannel.GENERAL)) {
                    res.setActiveChannel(ChatChannel.GENERAL);
                    Message.get("command.chat.joined").tell(res, "name", ChatChannel.GENERAL.getTag());
                }
                res.save();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        ResidentManager.getInstance().uncachePlayer(event.getPlayer());
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            ResidentManager.getInstance().getResident(event.getPlayer()).setResourcePack(true);
        }
    }
}

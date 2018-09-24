package me.crz.minecraft.kingdoms.utils;

import org.apache.commons.io.FileUtils;

import java.nio.charset.Charset;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.io.IOException;

import org.json.JSONObject;

import java.util.Scanner;
import java.io.File;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import com.google.common.collect.HashBiMap;

import java.util.UUID;

import com.google.common.collect.BiMap;
import me.crz.minecraft.kingdoms.Base;

public class OfflinePlayerManager extends Base {
    private static OfflinePlayerManager instance;
    private BiMap<String, UUID> offlinePlayers;
    private boolean isBungee = false;
    private int uuidVersion = 0;

    public OfflinePlayerManager() {
        super();
        offlinePlayers = HashBiMap.create();
        final File players = new File(KingdomsPlugin.getInstance().getDataFolder(), "players.json");
        if (players.exists()) {
            try {
                final JSONObject object = new JSONObject(new Scanner(players).useDelimiter("\\Z").next());
                for (final String key : object.keySet()) {
                    offlinePlayers.put(key, UUID.fromString(object.getString(key)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                players.createNewFile();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        isBungee = Bukkit.spigot().getConfig().getBoolean("settings.bungeecord");
    }

    public UUID uuidFromName(final String name) {
        if (offlinePlayers.containsKey(name)) {
            return offlinePlayers.get(name);
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (uuidVersion == 0) {
            if (offlinePlayer.getUniqueId().version() == 3 && (Bukkit.getServer().getOnlineMode() || isBungee)) {
                return null;
            } else {
                return offlinePlayer.getUniqueId();
            }
        }
        if (offlinePlayer.getUniqueId().version() != uuidVersion) {
            return null;
        }
        return offlinePlayer.getUniqueId();
    }

    public void addPlayer(final Player p) {
        if (uuidVersion == 0) {
            uuidVersion = p.getUniqueId().version();
        }
        if (offlinePlayers.containsValue(p.getUniqueId())) {
            offlinePlayers.remove(offlinePlayers.inverse().get(p.getUniqueId()));
        }
        offlinePlayers.put(p.getName().toLowerCase(), p.getUniqueId());
    }

    @EventHandler
    public void on(final PlayerLoginEvent e) {
        this.addPlayer(e.getPlayer());
    }

    public void save() {
        try {
            final File file = new File(KingdomsPlugin.getInstance().getDataFolder(), "players.json");
            final JSONObject obj = new JSONObject();
            for (final Map.Entry<String, UUID> s : offlinePlayers.entrySet()) {
                obj.put(s.getKey(), s.getValue().toString());
            }
            FileUtils.writeStringToFile(file, obj.toString(), Charset.forName("UTF-8"));
        } catch (Exception ex) {
        }
    }

    public static OfflinePlayerManager getInstance() {
        if (instance == null) {
            instance = new OfflinePlayerManager();
        }
        return instance;
    }
}

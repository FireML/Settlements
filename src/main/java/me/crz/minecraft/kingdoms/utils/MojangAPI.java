package me.crz.minecraft.kingdoms.utils;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.net.URL;
import java.io.Reader;
import java.util.UUID;
import org.json.JSONObject;
import java.io.IOException;
import org.json.JSONException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import java.util.HashMap;
import java.util.Map;
import me.crz.minecraft.kingdoms.Base;

public class MojangAPI extends Base
{
    private static MojangAPI mojangAPI;
    Map<String, String> names;
    
    public MojangAPI() {
        super();
        names = new HashMap<String, String>();
    }
    
    @EventHandler
    public void on(final PlayerLoginEvent e) {
        final String uuid = e.getPlayer().getUniqueId().toString();
        if (names.containsKey(uuid) && ((String)names.get(uuid)).equals(e.getPlayer().getName())) {
            return;
        }
        names.put(uuid, e.getPlayer().getName());
    }
    
    public String getName(final String uuid) {
        try {
            if (names.containsKey(uuid)) {
                return names.get(uuid);
            }
            JSONObject jsonObject = null;
            String name = "";
            try {
                jsonObject = readJsonFromUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace((CharSequence)"-", (CharSequence)""));
                name = jsonObject.getString("name");
                names.put(uuid, name);
                return name;
            }
            catch (JSONException | IOException ex3) {
                final Exception ex2;
                final Exception e = ex2;
                e.printStackTrace();
                try {
                    name = Bukkit.getOfflinePlayer(uuid).getName();
                    names.put(uuid, name);
                    return name;
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    return "";
                }
            }
        }
        catch (Exception e2) {
            return "";
        }
    }
    
    public UUID getUniqueId(final String name) {
        JSONObject jsonObject = null;
        try {
            jsonObject = readJsonFromUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
        }
        catch (JSONException | IOException ex2) {
            final Exception ex;
            final Exception e = ex;
            e.printStackTrace();
        }
        return UUID.fromString(jsonObject.get("id").toString().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
    
    private static String readAll(final Reader rd) throws IOException {
        final StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char)cp);
        }
        return sb.toString();
    }
    
    public static JSONObject readJsonFromUrl(final String url) throws IOException, JSONException {
        final InputStream is = new URL(url).openStream();
        try {
            final BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            final String jsonText = readAll(rd);
            final JSONObject json = new JSONObject(jsonText);
            return json;
        }
        finally {
            is.close();
        }
    }
    
    public static MojangAPI getInstance() {
        if (mojangAPI == null) {
            mojangAPI = new MojangAPI();
        }
        return mojangAPI;
    }
}

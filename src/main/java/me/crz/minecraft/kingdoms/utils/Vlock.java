package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.UUID;

import org.json.JSONObject;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bukkit.Location;

public class Vlock {
    public Vlock() {
        super();
    }

    public static boolean isLocationVLock(final Location loc) {
        return Config.vlock.has(String.valueOf(loc.getX()) + "~" + loc.getY() + "~" + loc.getZ() + "~" + loc.getWorld().getName());
    }

    public static JSONObject getVLock(final Location loc) {
        if (!isLocationVLock(loc)) {
            return null;
        }
        return Config.vlock.getJSONObject(String.valueOf(loc.getX()) + "~" + loc.getY() + "~" + loc.getZ() + "~" + loc.getWorld().getName());
    }

    public static boolean isUUIDPresent(final Location loc, final UUID uuid) {
        return isLocationVLock(loc) && getVLock(loc).getString("allowed").contains(uuid.toString());
    }

    public static JSONObject getVLockNew(final Location loc) {
        final JSONObject obj = new JSONObject();
        obj.put("world", loc.getWorld().getName());
        obj.put("x", loc.getX());
        obj.put("y", loc.getY());
        obj.put("z", loc.getZ());
        obj.put("allowed", "");
        return obj;
    }

    public static void addUUID(final Location loc, final UUID uuid) {
        final JSONObject obj = isLocationVLock(loc) ? getVLock(loc) : getVLockNew(loc);
        final Set<String> allowed = new HashSet<>(Arrays.asList(obj.getString("allowed").split(",")));
        allowed.add(uuid.toString());
        obj.put("allowed", String.join(",", allowed));
        save(loc, obj);
    }

    public static void removeUUID(final Location loc, final UUID uuid) {
        final JSONObject obj = isLocationVLock(loc) ? getVLock(loc) : getVLockNew(loc);
        final Set<String> allowed = new HashSet<>(Arrays.asList(obj.getString("allowed").split(",")));
        allowed.remove(uuid.toString());
        obj.put("allowed", String.join(",", allowed));
        save(loc, obj);
    }

    public static void save(final Location loc, final JSONObject obj) {
        Config.vlock.put(String.valueOf(loc.getX()) + "~" + loc.getY() + "~" + loc.getZ() + "~" + loc.getWorld().getName(), obj);
        KingdomsPlugin.config.save(KingdomsPlugin.getInstance().getDataFolder());
    }
}

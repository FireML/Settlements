package me.crz.minecraft.kingdoms.village;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.db.DatabaseManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ResidentManager {
    private static final Object RESIDENT_LOCK = new Object();
    private static ResidentManager instance;
    private LoadingCache<UUID, Resident> residentsByUuid;
    private LoadingCache<String, Resident> residentsByName;

    public ResidentManager() {
        Expiry<? super Object, Resident> residentExpiry = new Expiry<Object, Resident>() {
            @Override
            public long expireAfterCreate(Object key, Resident resident, long currentTime) {
                return getDuration(resident);
            }

            @Override
            public long expireAfterUpdate(Object key, Resident resident, long currentTime, long currentDuration) {
                return getDuration(resident);
            }

            @Override
            public long expireAfterRead(Object key, Resident resident, long currentTime, long currentDuration) {
                return getDuration(resident);
            }

            private long getDuration(Resident resident) {
                if (resident != null) {
                    Player player = Bukkit.getPlayer(resident.getUUID());
                    if (player != null && player.isOnline()) {
                        return Long.MAX_VALUE;
                    }
                }
                return TimeUnit.SECONDS.toNanos(Config.residentCacheDuration);
            }
        };
        residentsByUuid = Caffeine.newBuilder()
                .expireAfter(residentExpiry)
                .build(uuid -> {
                    synchronized (RESIDENT_LOCK) {
                        Resident res = DatabaseManager.getInstance().getResident(uuid);
                        if (res == null) {
                            return null;
                        }

                        residentsByName.put(res.getName().toLowerCase(), res);
                        return res;
                    }
                });
        residentsByName = Caffeine.newBuilder()
                .expireAfter(residentExpiry)
                .build(name -> {
                    synchronized (RESIDENT_LOCK) {
                        Resident res = DatabaseManager.getInstance().getResident(name);
                        if (res == null) {
                            return null;
                        }

                        residentsByUuid.put(res.getUUID(), res);
                        return res;
                    }
                });
    }

    public void addResident(Resident res) {
        residentsByUuid.put(res.getUUID(), res);
        residentsByName.put(res.getName().toLowerCase(), res);
    }

    public void invalidateName(String name) {
        residentsByName.invalidate(name.toLowerCase());
    }

    public boolean isListening(Player p, ChatChannel channel) {
        return this.getResident(p).getChannels().contains(channel);
    }

    public Resident getResident(OfflinePlayer p) {
        return this.getResident(p.getUniqueId());
    }

    public Resident getResident(UUID uuid) {
        return residentsByUuid.get(uuid);
    }

    public Resident getResident(String name) {
        return residentsByName.get(name.toLowerCase());
    }

    public static ResidentManager getInstance() {
        if (instance == null) {
            instance = new ResidentManager();
        }
        return instance;
    }

    public boolean doesResidentHaveVillage(Player player) {
        return !this.getResident(player).getVillageName().equals("");
    }
}

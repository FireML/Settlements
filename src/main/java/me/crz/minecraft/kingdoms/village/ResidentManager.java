package me.crz.minecraft.kingdoms.village;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.banish.CustomRunnable;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.config.Data;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.db.DatabaseManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ResidentManager {
    private static final Object RESIDENT_LOCK = new Object();
    private static ResidentManager instance;
    private Cache<Object, Resident> residentsByUuid;
    private Cache<Object, Resident> residentsByName;

    private ResidentManager() {
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
                    if (!KingdomsPlugin.getInstance().isRunning()) {
                        return Long.MAX_VALUE;
                    }
                    if (resident.hasVillage()) {
                        return Long.MAX_VALUE;
                    }
                    if (LandManager.getInstance().ownsPlot(resident.getUUID())) {
                        return Long.MAX_VALUE;
                    }
                    Player player = Bukkit.getPlayer(resident.getUUID());
                    if (player != null && player.isOnline()) {
                        return Long.MAX_VALUE;
                    }
                }
                return TimeUnit.SECONDS.toNanos(Data.residentCacheDuration);
            }
        };
        residentsByUuid = Caffeine.newBuilder().expireAfter(residentExpiry).build();
        residentsByName = Caffeine.newBuilder().expireAfter(residentExpiry).build();
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

    public Resident getResident(Player p) {
        Resident resident = getResident(p.getUniqueId());
        if (resident == null) {
            resident = new Resident(p.getUniqueId(), p.getName());
            addResident(resident);
        }
        return resident;
    }

    public Resident getResident(OfflinePlayer p) {
        return this.getResident(p.getUniqueId());
    }

    public Resident getResident(UUID uuid) {
        Resident resident = residentsByUuid.getIfPresent(uuid);
        if (resident == null) {
            synchronized (RESIDENT_LOCK) {
                resident = DatabaseManager.getInstance().getResident(uuid);
                if (resident != null) {
                    Resident resByName = residentsByName.getIfPresent(resident.getName().toLowerCase());
                    if (resByName != null && resByName.getUUID().equals(uuid) && resByName.getLastLogin() > resident.getLastLogin()) {
                        resident = resByName;
                    } else {
                        addResident(resident);
                    }
                }
            }
        }
        return resident;
    }

    public Resident getResident(String name) {
        if (name.length() > 16) {
            try {
                return getResident(UUID.fromString(name));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        Resident resident = residentsByName.getIfPresent(name.toLowerCase());
        if (resident == null) {
            synchronized (RESIDENT_LOCK) {
                resident = DatabaseManager.getInstance().getResident(name);
                if (resident != null) {
                    Resident resByUuid = residentsByUuid.getIfPresent(resident.getUUID());
                    if (resByUuid != null) {
                        if (!resByUuid.getName().equalsIgnoreCase(resident.getName())
                                && resByUuid.getLastLogin() < resident.getLastLogin()) {
                            resByUuid.setName(resident.getName());
                        }
                        resident = resByUuid;
                        residentsByName.put(resident.getName().toLowerCase(), resident);
                    } else {
                        addResident(resident);
                    }
                }
            }
        }
        return resident;
    }

    public Resident getResidentIfPreset(UUID playerId) {
        return residentsByUuid.getIfPresent(playerId);
    }

    public String getResidentName(UUID uuid) {
        Resident resident = getResident(uuid);
        return resident != null ? resident.getName() : uuid.toString();
    }

    synchronized public static ResidentManager getInstance() {
        if (instance == null) {
            instance = new ResidentManager();
            for (Village village : VillageManager.getInstance().getVillages()) {
                village.getResidents(); // pre load all residents in a village
            }
        }
        return instance;
    }

    public boolean doesResidentHaveVillage(Player player) {
        return this.getResident(player).hasVillage();
    }

    public boolean doesResidentHaveEmpire(Player player) {
        return this.getResident(player).hasEmpire();
    }

    public void isResBanished(OwnedLand land, Player p, CustomRunnable customRunnable) {
        if (land != null) {
            LandOwner landOwner = land.getLandOwner();
            if (landOwner != null && landOwner.isBanished(p.getUniqueId())) {
                customRunnable.run(land, p, landOwner);
            }
        }
    }
}

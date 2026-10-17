package de.themoep.settlements.resident;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.banish.CustomRunnable;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import uk.firedev.chatchannels.api.ChatChannel;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ResidentManager {
    private static final Object RESIDENT_LOCK = new Object();
    private static ResidentManager instance;
    private Cache<Object, Resident> residentsById;
    private Cache<Object, Resident> residentsByUuid;
    private Cache<Object, Resident> residentsByName;

    private void setup() {
        Expiry<? super Object, Resident> residentExpiry = new Expiry<>() {
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
                if (resident != null && (keepCached(resident) || Bukkit.getPlayer(resident.getUUID()) != null)) {
                    return Long.MAX_VALUE;
                }
                return TimeUnit.SECONDS.toNanos(Config.Cache.DURATION_RESIDENT);
            }
        };
        residentsById = Caffeine.newBuilder().expireAfter(residentExpiry).build();
        residentsByUuid = Caffeine.newBuilder().expireAfter(residentExpiry).build();
        residentsByName = Caffeine.newBuilder().expireAfter(residentExpiry).build();

        for (Settlement settlement : SettlementManager.getInstance().getSettlements()) {
            settlement.getResidents(); // pre load all residents in a settlement
        }
        SettlementsPlugin.logDebug("Loaded ResidentManager");
    }

    private boolean keepCached(Resident resident) {
        return !SettlementsPlugin.getInstance().isRunning()
                || resident.hasSettlement()
                || !resident.getFriends().isEmpty()
                || !InviteManager.getInstance().getInvites(resident).isEmpty()
                || LandManager.getInstance().ownsPlot(resident.getUUID());
    }

    public void addResident(Resident res) {
        if (!res.getId().isNull()) {
            residentsById.put(res.getId(), res);
        }
        residentsByUuid.put(res.getUUID(), res);
        residentsByName.put(res.getName().toLowerCase(), res);

        if (res.hasSettlement() && res.getMissionData().isEmpty()) {
            res.getSettlement().distributeNewMissions(res);
        }
    }

    public void uncachePlayer(Player player) {
        Resident resident = getResidentIfPresent(player.getUniqueId());
        if (resident != null) {
            resident.save();
            if (!keepCached(resident)) {
                if (!resident.getId().isNull()) {
                    residentsById.invalidate(resident.getId());
                }
                residentsByUuid.invalidate(resident.getUUID());
                invalidateName(resident.getName());
            }
        } else {
            invalidateName(player.getName());
        }
    }

    public void invalidateName(String name) {
        residentsByName.invalidate(name.toLowerCase());
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

    public Resident getResident(IdObject id) {
        if (id.value() instanceof UUID) {
            return getResident((UUID) id.value());
        }
        Resident resident = residentsById.getIfPresent(id);
        if (resident == null) {
            synchronized (RESIDENT_LOCK) {
                resident = DatabaseManager.getInstance().getResident((Integer) id.value());
                if (resident != null) {
                    addResident(resident);
                }
            }
        }
        return resident;
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
                } else {
                    Player player = SettlementsPlugin.getInstance().getServer().getPlayer(uuid);
                    if (player != null) {
                        resident = new Resident(player.getUniqueId(), player.getName());
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

    public Resident getResidentIfPresent(UUID playerId) {
        return residentsByUuid.getIfPresent(playerId);
    }

    public String getResidentName(UUID uuid) {
        Resident resident = getResident(uuid);
        return resident != null ? resident.getName() : uuid.toString();
    }

    public static ResidentManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new ResidentManager();
            instance.setup();
        }
    }

    public boolean doesResidentHaveSettlement(Player player) {
        return this.getResident(player).hasSettlement();
    }

    public void isResBanished(OwnedLand land, Player p, CustomRunnable.Player customRunnable) {
        if (land != null) {
            Settlement landOwner = land.getSettlement();
            Resident resident = ResidentManager.getInstance().getResident(p);
            if (landOwner != null && landOwner.isBanished(resident)) {
                customRunnable.run(land, p, landOwner);
            }
        }
    }

    public void isResBanished(OwnedLand land, UUID u, CustomRunnable.UUID customRunnable) {
        if (land != null) {
            Settlement landOwner = land.getSettlement();
            Resident resident = ResidentManager.getInstance().getResident(u);
            if (landOwner != null && landOwner.isBanished(resident)) {
                customRunnable.run(land, u, landOwner);
            }
        }
    }

    public Collection<Resident> getCachedResidents() {
        return residentsByUuid.asMap().values();
    }
}

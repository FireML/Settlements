package me.crz.minecraft.kingdoms.lock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleBlockLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class LockManager implements LockProvider {
    private static LockManager instance;
    private final Set<Material> lockable = EnumSet.noneOf(Material.class);
    private final Map<String, HashBasedTable<String, String, Lock>> lockTable = new HashMap<>();
    private final Multimap<UUID, SimpleBlockLocation> playerLocks = MultimapBuilder.hashKeys().hashSetValues().build();

    private final Cache<UUID, String> interactActions = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    private final Cache<UUID, String> toggledActions = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    private LockManager() {
        loadLockables();
        for (Lock lock : DatabaseManager.getInstance().getAllLocks()) {
            cacheLock(lock);
        }
        KingdomsPlugin.getInstance().getLockProviders().add(this);
    }

    public void loadLockables() {
        lockable.clear();
        for (String string : KingdomsPlugin.getInstance().getConfig().getStringList("lockable")) {
            Material type = Material.matchMaterial(string);
            if (type != null) {
                lockable.add(type);
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, string + " is not a valid Material name!");
            }
        }
    }

    public boolean isLockable(Material type) {
        return lockable.contains(type);
    }

    public void saveLock(Lock... locks) {
        for (Lock land : locks) {
            cacheLock(land);
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLocks(locks));
    }

    private void cacheLock(Lock lock) {
        lockTable.computeIfAbsent(lock.getLocation().getWorld(), s -> HashBasedTable.create()).put(lock.getLocation().getSimpleChunkKey(), lock.getLocation().getSimpleKey(), lock);
        playerLocks.put(lock.getOwnerId(), lock.getLocation());
    }

    public Lock registerNewLock(Player owner, Block block) {
        return registerNewLock(owner.getUniqueId(), new SimpleBlockLocation(block.getLocation()), block.getType());
    }

    public Lock registerNewLock(UUID owner, SimpleBlockLocation location, Material type) {
        Lock lock = new Lock(owner, location, type);
        saveLock(lock);
        return lock;
    }

    public Lock getLock(Block block) {
        Lock lock = getLock(block.getLocation());
        if (lock == null) {
            return null;
        }

        if (block.getType() != lock.getBlock() || !isLockable(block.getType())) {
            deleteLock(lock);
            return null;
        }

        return lock;
    }

    public Lock getLock(Location location) {
        if (lockTable.containsKey(location.getWorld().getName())) {
            return lockTable.get(location.getWorld().getName())
                    .get((location.getBlockX() >> 4) + "-" + (location.getBlockZ() >> 4), location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockZ());
        }
        return null;
    }

    public Lock getLock(SimpleBlockLocation location) {
        if (lockTable.containsKey(location.getWorld())) {
            return lockTable.get(location.getWorld()).get(location.toChunkLocation().getSimpleKey(), location.getSimpleKey());
        }
        return null;
    }

    public void deleteLock(Lock lock) {
        if (lockTable.containsKey(lock.getLocation().getWorld())) {
            lockTable.get(lock.getLocation().getWorld()).remove(lock.getLocation().getSimpleChunkKey(), lock.getLocation().getSimpleKey());
        }
        playerLocks.remove(lock.getOwnerId(), lock.getLocation());
        Utils.runAsync(() -> DatabaseManager.getInstance().deleteLocks(lock));
    }

    @Override
    public int removeInChunk(Collection<SimpleChunkLocation> chunks) {
        List<Lock> locks = new ArrayList<>();
        for (SimpleChunkLocation chunk : chunks) {
            HashBasedTable<String, String, Lock> table = lockTable.get(chunk.getWorld());
            if (table != null) {
                locks.addAll(table.row(chunk.getSimpleKey()).values());
                table.row(chunk.getSimpleKey()).clear();
            }
        }
        for (Lock lock : locks) {
            playerLocks.remove(lock.getOwnerId(), lock.getLocation());
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().deleteLocks(locks));
        return locks.size();
    }

    public static LockManager getInstance() {
        if (instance == null) {
            instance = new LockManager();
        }
        return instance;
    }

    public void addAction(UUID playerId, String action) {
        interactActions.put(playerId, action);
    }

    public boolean toggleAction(UUID playerId, String action) {
        String currentAction = toggledActions.getIfPresent(playerId);
        if (action.equals(currentAction)) {
            toggledActions.invalidate(playerId);
            return false;
        } else {
            toggledActions.put(playerId, action);
            return true;
        }
    }

    private String getAction(UUID playerId) {
        String action = interactActions.getIfPresent(playerId);
        if (action == null) {
            action = toggledActions.getIfPresent(playerId);
        }
        return action;
    }

    public String pullAction(UUID playerId) {
        String action = getAction(playerId);
        if (action != null) {
            interactActions.invalidate(playerId);
        }
        return action;
    }
}

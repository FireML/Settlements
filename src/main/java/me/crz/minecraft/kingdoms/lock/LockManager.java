package me.crz.minecraft.kingdoms.lock;

import co.aikar.timings.Timing;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.world.SimpleBlockLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LockManager implements LockProvider {
    private static final String LOADING_KEY = "LOADING_MARKER";
    private static LockManager instance;
    private final Set<Material> lockable = EnumSet.noneOf(Material.class);
    private boolean alternativeHopperProtection = true;
    private final Map<String, Cache<SimpleChunkLocation, Map<String, Lock>>> lockTable = new ConcurrentHashMap<>();
    private final Expiry<SimpleChunkLocation, Map<String, Lock>> chunkExpiry = new Expiry<SimpleChunkLocation, Map<String, Lock>>() {
        @Override
        public long expireAfterCreate(@Nonnull SimpleChunkLocation key, @Nonnull Map<String, Lock> value, long currentTime) {
            return getDuration(key, value);
        }

        @Override
        public long expireAfterUpdate(@Nonnull SimpleChunkLocation key, @Nonnull Map<String, Lock> value, long currentTime, long currentDuration) {
            return getDuration(key, value);
        }

        @Override
        public long expireAfterRead(@Nonnull SimpleChunkLocation key, @Nonnull Map<String, Lock> value, long currentTime, long currentDuration) {
            return getDuration(key, value);
        }

        private long getDuration(SimpleChunkLocation chunk, Map<String, Lock> value) {
            if (chunk != null) {
                World world = Bukkit.getWorld(chunk.getWorld());
                if (world == null) {
                    return 0;
                }

                if (!KingdomsPlugin.getInstance().isRunning() || value.containsKey(LOADING_KEY) || world.isChunkLoaded(chunk.getX(), chunk.getZ())) {
                    return Long.MAX_VALUE;
                }
            }
            return TimeUnit.SECONDS.toNanos(60);
        }
    };

    private final Cache<UUID, String> interactActions = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    private final Cache<UUID, String> toggledActions = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    private LockManager() {
        loadConfig();
        loadChunks(Bukkit.getWorlds().stream()
                .map(World::getLoadedChunks)
                .flatMap((Function<Chunk[], Stream<Chunk>>) Arrays::stream)
                .map(SimpleChunkLocation::new)
                .toArray(SimpleChunkLocation[]::new));
        KingdomsPlugin.getInstance().getLockProviders().add(this);
    }

    /**
     * Load chunks if they aren't loaded yet
     * Runs database operation in a new async thread if called on the main thread; otherwise it blocks the current thread
     * @param chunks The chunks to load locks in
     * @return The new async task or null if it was already run
     */
    public BukkitTask loadChunks(SimpleChunkLocation... chunks) {
        Set<SimpleChunkLocation> notLoaded = new HashSet<>();
        for (SimpleChunkLocation chunk : chunks) {
            // Mark all chunks that did not have locks as cached
            boolean isPresent = false;
            Cache<SimpleChunkLocation, Map<String, Lock>> worldCache;
            try (Timing t = KingdomsPlugin.getInstance().getAndStartTiming("LockManager create world chunk cache")) {
                worldCache = lockTable.get(chunk.getWorld());
                if (worldCache == null) {
                    worldCache = Caffeine.newBuilder().expireAfter(chunkExpiry).build();
                    lockTable.put(chunk.getWorld(), worldCache);
                } else {
                    isPresent = true;
                }
            }
            try (Timing t = KingdomsPlugin.getInstance().getAndStartTiming("LockManager check chunk lock cache")) {
                isPresent = isPresent && worldCache.getIfPresent(chunk) != null;
            }
            try (Timing t = KingdomsPlugin.getInstance().getAndStartTiming("LockManager create chunk lock cache")) {
                if (!isPresent) {
                    notLoaded.add(chunk);
                    worldCache.put(chunk, new ConcurrentHashMap<>(ImmutableMap.of(LOADING_KEY, new Lock(null, null, null))));
                }
            }
        }
        return Utils.runAsync(() -> {
            // Load locks in chunks
            for (Lock l : DatabaseManager.getInstance().getLocks(notLoaded)) {
                cacheLock(l);
            }
            for (SimpleChunkLocation chunk : notLoaded) {
                Map<String, Lock> chunkMap = lockTable.get(chunk.getWorld()).getIfPresent(chunk);
                if (chunkMap != null) {
                    chunkMap.remove(LOADING_KEY);
                }
            }
        });
    }

    public boolean isLoaded(SimpleChunkLocation chunk) {
        return lockTable.containsKey(chunk.getWorld()) && lockTable.get(chunk.getWorld()).getIfPresent(chunk) != null;
    }

    public void unloadWorld(World world) {
        lockTable.remove(world.getName());
    }

    public void loadConfig() {
        alternativeHopperProtection = KingdomsPlugin.getInstance().getConfig().getBoolean("alternative-hopper-protection");
        lockable.clear();
        for (String string : KingdomsPlugin.getInstance().getConfig().getStringList("lockable")) {
            if (string.startsWith("*")) {
                for (Material material : Material.values()) {
                    if (material.name().endsWith(string.substring(1).toUpperCase())) {
                        lockable.add(material);
                    }
                }
            } else if (string.endsWith("*")) {
                for (Material material : Material.values()) {
                    if (material.name().startsWith(string.substring(0, string.length() - 1).toUpperCase())) {
                        lockable.add(material);
                    }
                }
            } else {
                Material type = Material.matchMaterial(string);
                if (type != null) {
                    lockable.add(type);
                } else {
                    KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, string + " is not a valid Material name!");
                }
            }
        }
    }

    public boolean isLockable(Material type) {
        return lockable.contains(type);
    }

    public void saveLock(Lock... locks) {
        for (Lock lock : locks) {
            cacheLock(lock);
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLocks(locks));
    }

    private void cacheLock(Lock lock) {
        if (lock.isValid()) {
            Map<String, Lock> lockMap = lockTable.computeIfAbsent(lock.getLocation().getWorld(), s -> Caffeine.newBuilder().expireAfter(chunkExpiry).build())
                    .get(lock.getLocation().toChunkLocation(), location -> new ConcurrentHashMap<>());
            lockMap.put(lock.getLocation().getSimpleKey(), lock);
            lockMap.remove(LOADING_KEY);
        }
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
        return getLock(block.getLocation(), block.getBlockData());
    }

    public Lock getLock(InventoryHolder holder) {
        if (holder instanceof DoubleChest) {
            return getLock((DoubleChest) holder);
        }
        if (holder instanceof BlockState) {
            return getLock(((BlockState) holder).getLocation(), ((BlockState) holder).getBlockData());
        }
        return null;
    }

    private Lock getLock(DoubleChest doubleChest) {
        Lock lock = null;
        if (doubleChest.getLeftSide() instanceof BlockState) {
            lock = getLock(((BlockState) doubleChest.getLeftSide()).getLocation(), ((BlockState) doubleChest.getLeftSide()).getType());
        }
        if (lock == null && doubleChest.getRightSide() instanceof BlockState) {
            lock = getLock(((BlockState) doubleChest.getRightSide()).getLocation(), ((BlockState) doubleChest.getRightSide()).getType());
        }
        return lock;
    }

    private Lock getLock(Location location, BlockData data) {
        Lock lock = getLock(location, data.getMaterial());
        if (lock == null && data instanceof Bisected) {
            lock = getLock(location.add(0, ((Bisected) data).getHalf() == Bisected.Half.TOP ? -1 : 1, 0), data.getMaterial());
        }
        if (lock == null && data instanceof Chest && ((Chest) data).getType() != Chest.Type.SINGLE) {
            Vector mod;
            if (((Chest) data).getType() == Chest.Type.LEFT) {
                // Get mod to the RIGHT
                mod = new Vector(
                        ((Chest) data).getFacing().getModZ() * -1,
                        0,
                        ((Chest) data).getFacing().getModX()
                );
            } else if (((Chest) data).getType() == Chest.Type.RIGHT) {
                // Get mod to the LEFT
                mod = new Vector(
                        ((Chest) data).getFacing().getModZ(),
                        0,
                        ((Chest) data).getFacing().getModX() * -1
                );
            } else {
                mod = new Vector(0, 0, 0);
            }
            lock = getLock(location.add(mod), data.getMaterial());
        }
        return lock;
    }

    private Lock getLock(Location bukkitLocation, Material type) {
        Lock lock = null;
        if (lockTable.containsKey(bukkitLocation.getWorld().getName())) {
            SimpleBlockLocation location = new SimpleBlockLocation(bukkitLocation);
            Map<String, Lock> chunkMap = lockTable.get(location.getWorld()).getIfPresent(location.toChunkLocation());
            if (chunkMap != null && !chunkMap.containsKey(LOADING_KEY)) {
                // Chunk was cached
                lock = chunkMap.get(location.getSimpleKey());
            } else {
                if (chunkMap == null) {
                    // Chunk was not cached yet, load it
                    loadChunks(location.toChunkLocation());
                }

                if (isLockable(type)) {
                    // Return an empty lock if chunk isn't cached yet to block any interaction
                    return new Lock(Utils.ZERO_UUID, location, type);
                }
            }
        }

        if (lock != null && (type != lock.getBlock() || !isLockable(type))) { // lock is not valid
            deleteLock(lock);
            return null;
        }
        return lock;
    }

    public void deleteLock(Lock lock) {
        if (lockTable.containsKey(lock.getLocation().getWorld())) {
            Map<String, Lock> chunkMap = lockTable.get(lock.getLocation().getWorld()).getIfPresent(lock.getLocation().toChunkLocation());
            if (chunkMap != null) {
                chunkMap.remove(lock.getLocation().getSimpleKey(), lock);
            }
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().deleteLocks(lock));
    }

    @Override
    public boolean isLocked(Block block) {
        return getLock(block) != null;
    }

    @Override
    public boolean canAccess(Player player, Block block) {
        Lock lock = getLock(block);
        return lock == null || lock.canAccess(player);
    }

    @Override
    public void removeInChunks(Collection<? extends SimpleChunkLocation> chunks) {
        for (SimpleChunkLocation chunk : chunks) {
            if (lockTable.containsKey(chunk.getWorld())) {
                lockTable.get(chunk.getWorld()).invalidate(chunk);
            }
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().deleteLocksInChunks(chunks));
    }

    @Override
    public void removeInWorld(World world) {
        lockTable.remove(world.getName());
        Utils.runAsync(() -> DatabaseManager.getInstance().deleteLocks(world.getName()));
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

    public Collection<Lock> getLocks() {
        return lockTable.values().stream()
                .map(c -> c.asMap().values())
                .flatMap((Function<Collection<Map<String, Lock>>, Stream<Lock>>) maps
                        -> maps.stream().map(Map::values).flatMap(Collection::stream)).collect(Collectors.toSet());
    }

    public Collection<Lock> getLocks(String world) {
        if (lockTable.get(world) != null) {
            return lockTable.get(world).asMap().values().stream()
                    .flatMap((Function<Map<String, Lock>, Stream<Lock>>) stringLockMap
                            -> stringLockMap.values().stream()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    public boolean isAlternativeHopperProtection() {
        return alternativeHopperProtection;
    }
}

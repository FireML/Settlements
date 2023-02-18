package de.themoep.settlements.lock;

import co.aikar.timings.lib.MCTiming;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.lock.LockCommand;
import de.themoep.settlements.command.lock.UnlockCommand;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.world.SimpleBlockLocation;
import de.themoep.settlements.world.SimpleChunkLocation;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LockManager implements LockProvider {
    private static final String LOADING_KEY = "LOADING_MARKER";
    private static LockManager instance;
    private BukkitTask loadQueueTask = null;
    private final BlockingQueue<SimpleChunkLocation> loadQueue = new LinkedBlockingQueue<>();
    // Ideally this would use a Guava table but synchronized ones were only added in Guava 28 and Minecraft/Spigot uses 21
    private final Map<String, Map<SimpleChunkLocation, Map<String, Lock>>> lockTable = new ConcurrentHashMap<>();

    private final Cache<UUID, String> interactActions = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();
    private final Cache<UUID, String> toggledActions = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    private void setup() {
        loadChunks(Bukkit.getWorlds().stream()
                .map(World::getLoadedChunks)
                .flatMap((Function<Chunk[], Stream<Chunk>>) Arrays::stream)
                .map(SimpleChunkLocation::new)
                .toArray(SimpleChunkLocation[]::new));
        SettlementsPlugin.getInstance().getLockProviders().add(this);

        CommandManager.getInstance().registerCommand(new LockCommand());
        CommandManager.getInstance().registerCommand(new UnlockCommand());

        SettlementsPlugin.getInstance().registerListener(new LockListener());

        SettlementsPlugin.logDebug("Loaded LockManager");
    }

    /**
     * Load chunks if they aren't loaded yet
     * Runs database operation in a new async thread if called on the main thread; otherwise it blocks the current thread
     * @param chunks The chunks to load locks in
     */
    public void loadChunks(SimpleChunkLocation... chunks) {
        Set<SimpleChunkLocation> notLoaded = new HashSet<>();
        for (SimpleChunkLocation chunk : chunks) {
            // Mark all chunks that did not have locks as cached
            boolean isPresent = false;
            Map<SimpleChunkLocation, Map<String, Lock>> worldCache;
            try (MCTiming ignored1 = SettlementsPlugin.getInstance().getAndStartTiming("LockManager create world chunk cache")) {
                worldCache = lockTable.get(chunk.getWorld());
                if (worldCache == null) {
                    worldCache = new ConcurrentHashMap<>();
                    lockTable.put(chunk.getWorld(), worldCache);
                } else {
                    isPresent = true;
                }
            }
            try (MCTiming ignored = SettlementsPlugin.getInstance().getAndStartTiming("LockManager check chunk lock cache")) {
                isPresent = isPresent && worldCache.containsKey(chunk);
            }
            try (MCTiming ignored = SettlementsPlugin.getInstance().getAndStartTiming("LockManager create chunk lock cache")) {
                if (!isPresent) {
                    notLoaded.add(chunk);
                    worldCache.put(chunk, new ConcurrentHashMap<>(ImmutableMap.of(LOADING_KEY, new Lock(null, null, null))));
                }
            }
        }
        queueChunkLoad(notLoaded);
    }

    private void queueChunkLoad(Collection<SimpleChunkLocation> chunks) {
        loadQueue.addAll(chunks);
        if (loadQueueTask == null || loadQueueTask.isCancelled()) {
            loadQueueTask = Bukkit.getScheduler().runTaskLaterAsynchronously(SettlementsPlugin.getInstance(), () -> {
                loadQueueTask = null;
                if (!loadQueue.isEmpty()) {
                    // Load locks in chunks
                    Set<SimpleChunkLocation> toLoad = new LinkedHashSet<>();
                    loadQueue.drainTo(toLoad);
                    for (Lock l : DatabaseManager.getInstance().getLocks(toLoad)) {
                        cacheLock(l);
                    }
                    for (SimpleChunkLocation chunk : toLoad) {
                        Map<SimpleChunkLocation, Map<String, Lock>> worldMap = lockTable.get(chunk.getWorld());
                        if (worldMap != null) {
                            Map<String, Lock> chunkMap = worldMap.get(chunk);
                            if (chunkMap != null) {
                                chunkMap.remove(LOADING_KEY);
                            }
                        }
                    }
                }
            }, Config.Lock.LOAD_QUEUE_TICKS);
        }
    }

    public void unloadChunks(SimpleChunkLocation... chunks) {
        for (SimpleChunkLocation chunk : chunks) {
            Map<SimpleChunkLocation, Map<String, Lock>> worldMap = lockTable.get(chunk.getWorld());
            if (worldMap != null) {
                worldMap.remove(chunk);
            }
        }
    }

    public boolean isLoaded(SimpleChunkLocation chunk) {
        Map<SimpleChunkLocation, Map<String, Lock>> worldMap = lockTable.get(chunk.getWorld());
        return worldMap != null && worldMap.containsKey(chunk);
    }

    public void unloadWorld(World world) {
        lockTable.remove(world.getName());
    }

    public void saveLock(Lock... locks) {
        for (Lock lock : locks) {
            cacheLock(lock);
        }
        DatabaseManager.getInstance().saveLocks(Arrays.asList(locks));
    }

    private void cacheLock(Lock lock) {
        if (lock.isValid()) {
            Map<String, Lock> lockMap = lockTable.computeIfAbsent(lock.getLocation().getWorld(), s -> new ConcurrentHashMap<>())
                    .computeIfAbsent(lock.getLocation().toChunkLocation(), location -> new ConcurrentHashMap<>());
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
        Map<SimpleChunkLocation, Map<String, Lock>> worldMap = lockTable.get(bukkitLocation.getWorld().getName());
        if (worldMap != null) {
            SimpleBlockLocation location = new SimpleBlockLocation(bukkitLocation);
            Map<String, Lock> chunkMap = worldMap.get(location.toChunkLocation());
            if (chunkMap != null && !chunkMap.containsKey(LOADING_KEY)) {
                // Chunk was cached
                lock = chunkMap.get(location.getSimpleKey());
            } else {
                if (chunkMap == null) {
                    // Chunk was not cached yet, load it
                    loadChunks(location.toChunkLocation());
                }

                if (Config.Lock.BLOCKS.contains(type)) {
                    // Return an empty lock if chunk isn't cached yet to block any interaction
                    return new Lock(Utils.ZERO_UUID, location, type);
                }
            }
        }

        if (lock != null && (type != lock.getBlock() || !Config.Lock.BLOCKS.contains(type))) { // lock is not valid
            deleteLock(lock);
            return null;
        }
        return lock;
    }

    public void deleteLock(Lock lock) {
        Map<SimpleChunkLocation, Map<String, Lock>> worldMap = lockTable.get(lock.getLocation().getWorld());
        if (worldMap != null) {
            Map<String, Lock> chunkMap = worldMap.get(lock.getLocation().toChunkLocation());
            if (chunkMap != null) {
                chunkMap.remove(lock.getLocation().getSimpleKey(), lock);
            }
        }
        DatabaseManager.getInstance().deleteLocks(lock);
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
            Map<SimpleChunkLocation, Map<String, Lock>> worldMap = lockTable.get(chunk.getWorld());
            if (worldMap != null) {
                worldMap.remove(chunk);
            }
        }
        DatabaseManager.getInstance().deleteLocksInChunks(chunks);
    }

    @Override
    public void removeInWorld(World world) {
        lockTable.remove(world.getName());
        DatabaseManager.getInstance().deleteLocks(world.getName());
    }

    @Override
    public void renameWorld(String oldName, String newName) {
        Map<SimpleChunkLocation, Map<String, Lock>> locks = lockTable.remove(oldName);
        if (locks != null) {
            lockTable.merge(newName, locks, (m1, m2) -> {
                m1.putAll(m2);
                return m1;
            });
            Utils.runAsync(() -> DatabaseManager.getInstance().renameLockWorld(oldName, newName));
        }
    }

    public static LockManager getInstance() {
        if (!Config.Lock.ENABLED) {
            throw new IllegalStateException("Tried to get LockManager while locks were disabled!");
        }
        return instance;
    }

    public static void load() {
        if (!Config.Lock.ENABLED) {
            throw new IllegalStateException("Tried to get LockManager while locks were disabled!");
        }
        if (instance == null) {
            instance = new LockManager();
            instance.setup();
        }
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

    public boolean hasToggledAction(UUID playerId) {
        return getToggledAction(playerId) != null;
    }

    public String getToggledAction(UUID playerId) {
        return toggledActions.getIfPresent(playerId);
    }

    private String getAction(UUID playerId) {
        String action = interactActions.getIfPresent(playerId);
        if (action == null) {
            action = getToggledAction(playerId);
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
                .map(Map::values)
                .flatMap((Function<Collection<Map<String, Lock>>, Stream<Lock>>) maps
                        -> maps.stream().map(Map::values).flatMap(Collection::stream)).collect(Collectors.toSet());
    }

    public Collection<Lock> getLocks(String world) {
        if (lockTable.get(world) != null) {
            return lockTable.get(world).values().stream()
                    .flatMap((Function<Map<String, Lock>, Stream<Lock>>) stringLockMap
                            -> stringLockMap.values().stream()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }
}

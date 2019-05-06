package me.crz.minecraft.kingdoms.lock;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.common.TargetParser;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.integration.ShopProvider;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.landflags.LandFlagBase;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LockListener implements Listener, LandFlagBase, TargetParser {

    // chunk load caching
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        LockManager.getInstance().loadChunks(new SimpleChunkLocation(event.getChunk()));
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        LockManager.getInstance().loadChunks(
                Arrays.stream(event.getWorld().getLoadedChunks())
                        .map(SimpleChunkLocation::new)
                        .toArray(SimpleChunkLocation[]::new));
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        LockManager.getInstance().unloadWorld(event.getWorld());
    }

    // block destruction

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Lock lock = LockManager.getInstance().getLock(event.getBlock());
        if (lock != null && !lock.isOwner(event.getPlayer()) && !event.getPlayer().hasPermission("kingdoms.bypass.lock")) {
            event.setCancelled(true);
            Resident owner = lock.getOwner();
            getError("locked").tell(event.getPlayer(),
                    "name", owner != null ? owner.getName() : "-",
                    "title", owner != null? owner.getTitle() : "",
                    "block", NameTag.getTag(lock.getBlock())
            );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        for (Iterator<BlockState> it = event.getBlocks().iterator(); it.hasNext();) {
            Lock lock = LockManager.getInstance().getLock(it.next().getBlock());
            if (lock != null) {
                it.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (containsLocked(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (containsLocked(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Lock lock = LockManager.getInstance().getLock(event.getBlock());
        if (lock != null) {
            event.setCancelled(true);
        } else {
            event.blockList().removeIf(b -> LockManager.getInstance().getLock(b) != null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(b -> LockManager.getInstance().getLock(b) != null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Lock lock = LockManager.getInstance().getLock(event.getBlock());
        if (lock != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Lock lock = LockManager.getInstance().getLock(event.getBlock());
        if (lock != null) {
            event.setCancelled(true);
        }
    }

    private boolean containsLocked(List<Block> blocks) {
        for (Block block : blocks) {
            Lock lock = LockManager.getInstance().getLock(block);
            if (lock != null) {
                return true;
            }
        }
        return false;
    }

    // block interacting

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Lock lock = LockManager.getInstance().getLock(event.getClickedBlock());
        if (lock != null) {
            if (!lock.canAccess(event.getPlayer())) {
                event.setCancelled(true);
                Utils.runAsync(() -> {
                    Resident owner = lock.getOwner();
                    getError("locked").tell(event.getPlayer(),
                            "name", owner != null ? owner.getName() : "-",
                            "title", owner != null? owner.getTitle() : "",
                            "block", NameTag.getTag(lock.getBlock())
                    );
                });
            }
        }
        if (event.getHand() == EquipmentSlot.HAND) {
            String action = LockManager.getInstance().pullAction(event.getPlayer().getUniqueId());
            if (action != null) {
                event.setCancelled(true);
                if ("lock".equalsIgnoreCase(action)) {
                    if (lock == null) {
                        boolean limitedToVillage = KingdomsPlugin.getInstance().getConfig().getBoolean("only-lock-in-settlement");
                        OwnedLand land = LandManager.getInstance().getLand(event.getClickedBlock().getLocation());
                        if (land == null && limitedToVillage) {
                            getError("only-in-claim").tell(event.getPlayer());
                        } else if (!LockManager.getInstance().isLockable(event.getClickedBlock().getType())) {
                            getError("not-lockable").tell(event.getPlayer(), "block", NameTag.getTag(event.getClickedBlock().getType()));
                        } else {
                            if (!LockProvider.canAccessCheck(event.getPlayer(), event.getClickedBlock())) {
                                getError("locked").tell(event.getPlayer(),
                                        "name", "someone",
                                        "title", "",
                                        "block", NameTag.getTag(event.getClickedBlock().getType())
                                );
                                return;
                            }
                            Pair<Boolean, String> response = getCanBuildAll(event.getPlayer(), event.getClickedBlock().getLocation());
                            if (response.getLeft()) {
                                Lock l = LockManager.getInstance().registerNewLock(event.getPlayer(), event.getClickedBlock());
                                getMessage("new-lock").tell(event.getPlayer(), "block", NameTag.getTag(l.getBlock()));
                            } else {
                                MessageUtils.tell(event.getPlayer(), response.getRight());
                            }
                        }
                    } else if (lock.canAccess(event.getPlayer())) {
                        Utils.runAsync(() -> {
                            Resident owner = lock.getOwner();
                            getError("locked").tell(event.getPlayer(),
                                    "name", owner != null ? owner.getName() : "-",
                                    "title", owner != null? owner.getTitle() : "",
                                    "block", NameTag.getTag(lock.getBlock())
                            );
                        });
                    }
                } else if (action.toLowerCase().startsWith("add")) {
                    Lock l;
                    if (lock == null) {
                        if (ShopProvider.isShopCheck(event.getClickedBlock()) && LockProvider.canAccessCheck(event.getPlayer(), event.getClickedBlock())) {
                            l = LockManager.getInstance().registerNewLock(event.getPlayer(), event.getClickedBlock());
                        } else {
                            getError("not-locked").tell(event.getPlayer(), "block", NameTag.getTag(event.getClickedBlock().getType()));
                            return;
                        }
                    } else {
                        l = lock;
                    }

                    if (l.isOwner(event.getPlayer())) {
                        Targetable target = getTarget(event.getPlayer(), action.substring("add ".length()));
                        if (target != null) {
                            if (l.grantAccess(target)) {
                                getMessage("access.granted").tell(event.getPlayer(), "block", NameTag.getTag(l.getBlock()), "name", target.getName());
                                Utils.runAsync(() -> DatabaseManager.getInstance().saveLocks(l));
                            } else {
                                getMessage("access.already-granted").tell(event.getPlayer(), "block", NameTag.getTag(l.getBlock()), "name", target.getName());
                            }
                        }
                    } else if (l.canAccess(event.getPlayer())) {
                        Utils.runAsync(() -> {
                            Resident owner = l.getOwner();
                            getError("locked").tell(event.getPlayer(),
                                    "name", owner != null ? owner.getName() : "-",
                                    "title", owner != null? owner.getTitle() : "",
                                    "block", NameTag.getTag(l.getBlock())
                            );
                        });
                    }
                } else if (lock == null) {
                    getError("not-locked").tell(event.getPlayer(), "block", NameTag.getTag(event.getClickedBlock().getType()));
                } else if  ("unlock".equalsIgnoreCase(action)) {
                    if (lock.canAdministrate(event.getPlayer())) {
                        LockManager.getInstance().deleteLock(lock);
                        getMessage("unlocked").tell(event.getPlayer(), "block", NameTag.getTag(lock.getBlock()));
                    } else {
                        getError("cant-unlock").tell(event.getPlayer(), "block", NameTag.getTag(lock.getBlock()));
                    }
                } else if ("hoppers".equalsIgnoreCase(action)) {
                    if (lock.isOwner(event.getPlayer())) {
                        lock.setAllowHoppers(!lock.isAllowHoppers());
                        getMessage("hoppers." + (lock.isAllowHoppers() ? "enabled" : ".disabled")).tell(event.getPlayer(), "block", NameTag.getTag(lock.getBlock()));
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveLocks(lock));
                    } else if (lock.canAccess(event.getPlayer())) {
                        Utils.runAsync(() -> {
                            Resident owner = lock.getOwner();
                            getError("locked").tell(event.getPlayer(),
                                    "name", owner != null ? owner.getName() : "-",
                                    "title", owner != null? owner.getTitle() : "",
                                    "block", NameTag.getTag(lock.getBlock())
                            );
                        });
                    }
                } else if (action.toLowerCase().startsWith("remove")) {
                    if (lock.isOwner(event.getPlayer())) {
                        Targetable target = getTarget(event.getPlayer(), action.substring("remove ".length()));
                        if (target != null) {
                            if (lock.revokeAccess(target)) {
                                getMessage("access.revoked").tell(event.getPlayer(), "block", NameTag.getTag(lock.getBlock()), "name", target.getName());
                                Utils.runAsync(() -> DatabaseManager.getInstance().saveLocks(lock));
                            } else {
                                getMessage("access.already-revoked").tell(event.getPlayer(), "block", NameTag.getTag(lock.getBlock()), "name", target.getName());
                            }
                        }
                    } else if (lock.canAccess(event.getPlayer())) {
                        Utils.runAsync(() -> {
                            Resident owner = lock.getOwner();
                            getError("locked").tell(event.getPlayer(),
                                    "name", owner != null ? owner.getName() : "-",
                                    "title", owner != null? owner.getTitle() : "",
                                    "block", NameTag.getTag(lock.getBlock())
                            );
                        });
                    }
                } else if (action.equalsIgnoreCase("info")) {
                    if (lock.canAccess(event.getPlayer()) || lock.canAdministrate(event.getPlayer())) {
                        Utils.runAsync(() -> {
                            Resident owner = lock.getOwner();
                            getMessage("info.general").tell(event.getPlayer(),
                                    "title", owner != null? owner.getTitle() : "",
                                    "owner", owner != null ? owner.getName() : "-",
                                    "hoppers", MessageUtils.yesOrNo(lock.isAllowHoppers())
                            );
                            if (!lock.getAllowedPlayers().isEmpty()) {
                                event.getPlayer().sendMessage(lock.getAllowedPlayers().stream()
                                        .map(id -> ResidentManager.getInstance().getResident(id))
                                        .filter(Objects::nonNull)
                                        .map(r -> getMessage("info.entry").getReplacedText("name", r.getName(), "title", r.getTitle()))
                                        .collect(Collectors.joining(getMessage("info.separator").getText())));
                            }
                            if (!lock.getAllowedVillageIds().isEmpty()) {
                                event.getPlayer().sendMessage(lock.getAllowedVillageIds().stream()
                                        .map(i -> VillageManager.getInstance().getVillageFromId(i))
                                        .filter(Objects::nonNull)
                                        .map(v -> getMessage("info.entry").getReplacedText("name", v.getName(), "title", v.getStateTag()))
                                        .collect(Collectors.joining(getMessage("info.separator").getText())));
                            }
                            if (!lock.getAllowedEmpireIds().isEmpty()) {
                                event.getPlayer().sendMessage(lock.getAllowedEmpireIds().stream()
                                        .map(i -> EmpireManager.getInstance().getEmpireFromId(i))
                                        .filter(Objects::nonNull)
                                        .map(e -> getMessage("info.entry").getReplacedText("name", e.getName(), "title", e.getStateTag()))
                                        .collect(Collectors.joining(getMessage("info.separator").getText())));
                            }
                        });
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Lock lock = LockManager.getInstance().getLock(event.getBlock());
        if (lock != null && !lock.canAccess(event.getPlayer())) {
            event.setCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Lock lock = LockManager.getInstance().getLock(event.getInventory().getHolder());
            if (lock != null && !lock.canAccess((Player) event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    // block item moving

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (LockManager.getInstance().isAlternativeHopperProtection()
                && !(event.getSource().getHolder() instanceof HopperMinecart || event.getDestination().getHolder() instanceof HopperMinecart)) {
            return;
        }
        Lock sourceLock = LockManager.getInstance().getLock(event.getSource().getHolder());
        if (sourceLock != null && !sourceLock.isAllowHoppers()) {
            event.setCancelled(true);
            return;
        }
        Lock destinationLock = LockManager.getInstance().getLock(event.getDestination().getHolder());
        if (destinationLock != null && !destinationLock.isAllowHoppers()) {
            event.setCancelled(true);
            return;
        }
    }

    // block placement if alternative hopper protection is enabled

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!LockManager.getInstance().isAlternativeHopperProtection()) {
            return;
        }

        if (event.getBlock().getType() == Material.HOPPER) {
            if (checkForHopperLock(event.getPlayer(), event.getBlock().getRelative(BlockFace.UP))) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getBlock().getType() == Material.HOPPER || event.getBlock().getType() == Material.DROPPER) {
            if (checkForHopperLock(event.getPlayer(), event.getBlock().getRelative(((Directional) event.getBlock().getBlockData()).getFacing()))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean checkForHopperLock(Player player, Block block) {
        if (!block.isEmpty() && block.getState(false) instanceof InventoryHolder) {
            // we use the alternative hopper protection, check if the hopper is placed below a container!
            Lock lock = LockManager.getInstance().getLock(block);
            if (lock != null && !lock.isAllowHoppers() && !lock.canAccess(player)) {
                getMessage("hoppers.disabled").tell(player, "block", NameTag.getTag(block.getType()));
                return true;
            }
        }
        return false;
    }

    @Override
    public String name() {
        return "lock";
    }
}

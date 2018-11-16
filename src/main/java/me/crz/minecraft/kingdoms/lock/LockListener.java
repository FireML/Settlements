package me.crz.minecraft.kingdoms.lock;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.common.TargetParser;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
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
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
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

import java.util.Arrays;
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

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        for (BlockState block : event.getBlocks()) {
            Lock lock = LockManager.getInstance().getLock(block);
            if (lock != null && !lock.isOwner(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (containsLocked(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (containsLocked(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Lock lock = LockManager.getInstance().getLock(event.getBlock());
        if (lock != null || containsLocked(event.blockList())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if ( containsLocked(event.blockList())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Lock lock = LockManager.getInstance().getLock(event.getBlock());
        if (lock != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Lock lock = LockManager.getInstance().getLock(event.getClickedBlock());
        Resident owner = null;
        if (lock != null) {
            owner = lock.getOwner();
            if (!lock.canAccess(event.getPlayer())) {
                event.setCancelled(true);
                getError("locked").tell(event.getPlayer(),
                        "name", owner != null ? owner.getName() : "-",
                        "title", owner != null? owner.getTitle() : "",
                        "block", NameTag.getTag(lock.getBlock())
                );
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
                            Pair<Boolean, String> response = getCanBuildAll(event.getPlayer(), event.getClickedBlock().getLocation());
                            if (response.getLeft()) {
                                Lock l = LockManager.getInstance().registerNewLock(event.getPlayer(), event.getClickedBlock());
                                getMessage("new-lock").tell(event.getPlayer(), "block", NameTag.getTag(l.getBlock()));
                            } else {
                                MessageUtils.tell(event.getPlayer(), response.getRight());
                            }
                        }
                    } else if (lock.canAccess(event.getPlayer())) {
                        getError("locked").tell(event.getPlayer(),
                                "name", owner != null ? owner.getName() : "-",
                                "title", owner != null? owner.getTitle() : "",
                                "block", NameTag.getTag(lock.getBlock())
                        );
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
                        lock.setAllowHoppers(lock.isAllowHoppers());
                        getMessage("hoppers." + (lock.isAllowHoppers() ? "enabled" : ".disabled")).tell(event.getPlayer(), "block", NameTag.getTag(lock.getBlock()));
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveLocks(lock));
                    } else {
                        getError("locked").tell(event.getPlayer(), "name", owner.getName(), "title", owner.getTitle(), "block", NameTag.getTag(lock.getBlock()));
                    }
                } else if (action.toLowerCase().startsWith("add")) {
                    if (lock.isOwner(event.getPlayer())) {
                        Targetable target = getTarget(event.getPlayer(), action.substring("add ".length()));
                        if (target != null) {
                            if (lock.grantAccess(target)) {
                                getMessage("access.granted").tell(event.getPlayer(), "block", NameTag.getTag(lock.getBlock()), "name", target.getName());
                                Utils.runAsync(() -> DatabaseManager.getInstance().saveLocks(lock));
                            } else {
                                getMessage("access.already-granted").tell(event.getPlayer(), "block", NameTag.getTag(lock.getBlock()), "name", target.getName());
                            }
                        }
                    } else {
                        getError("locked").tell(event.getPlayer(),
                                "name", owner != null ? owner.getName() : "-",
                                "title", owner != null? owner.getTitle() : "",
                                "block", NameTag.getTag(lock.getBlock())
                        );
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
                    } else {
                        getError("locked").tell(event.getPlayer(),
                                "name", owner != null ? owner.getName() : "-",
                                "title", owner != null? owner.getTitle() : "",
                                "block", NameTag.getTag(lock.getBlock())
                        );
                    }
                } else if (action.equalsIgnoreCase("info")) {
                    if (lock.canAccess(event.getPlayer()) || lock.canAdministrate(event.getPlayer())) {
                        getMessage("info").tell(event.getPlayer(),
                                "title", owner != null? owner.getTitle() : "",
                                "owner", owner != null ? owner.getName() : "-",
                                "hoppers", MessageUtils.yesOrNo(lock.isAllowHoppers())
                        );
                        if (!lock.getAllowedPlayers().isEmpty()) {
                            event.getPlayer().sendMessage(lock.getAllowedPlayers().stream()
                                    .map(id -> ResidentManager.getInstance().getResident(id))
                                    .filter(Objects::nonNull)
                                    .map(r -> getMessage("entry").getReplacedText("name", r.getName(), "title", r.getTitle()))
                                    .collect(Collectors.joining(getMessage("separator").getText())));
                        }
                        if (!lock.getAllowedVillageIds().isEmpty()) {
                            event.getPlayer().sendMessage(lock.getAllowedVillageIds().stream()
                                    .map(i -> VillageManager.getInstance().getVillageFromId(i))
                                    .map(v -> getMessage("entry").getReplacedText("name", v.getName(), "title", v.getStateTag()))
                                    .collect(Collectors.joining(getMessage("separator").getText())));
                        }
                        if (!lock.getAllowedEmpireIds().isEmpty()) {
                            event.getPlayer().sendMessage(lock.getAllowedEmpireIds().stream()
                                    .map(i -> EmpireManager.getInstance().getEmpireFromId(i))
                                    .map(e -> getMessage("entry").getReplacedText("name", e.getName(), "title", e.getStateTag()))
                                    .collect(Collectors.joining(getMessage("separator").getText())));
                        }
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
        if (event.getPlayer() instanceof Player && event.getInventory().getHolder() instanceof BlockState) {
            BlockState state = (BlockState) event.getInventory().getHolder();
            Lock lock = LockManager.getInstance().getLock(state);
            if (lock != null && !lock.canAccess((Player) event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    // block item moving

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (event.getDestination().getHolder() instanceof BlockState) {
            Lock lock = LockManager.getInstance().getLock(((BlockState) event.getDestination().getHolder()));
            if (lock != null && !lock.isAllowHoppers()) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getSource().getHolder() instanceof BlockState) {
            Lock lock = LockManager.getInstance().getLock(((BlockState) event.getSource().getHolder()));
            if (lock != null && !lock.isAllowHoppers()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public String name() {
        return "lock";
    }
}

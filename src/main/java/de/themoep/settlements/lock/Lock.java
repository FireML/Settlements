package de.themoep.settlements.lock;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.MaterialUtils;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.world.SimpleBlockLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

public class Lock implements DatabaseSerializable {
    private IdObject id = IdObject.get(null);
    private final UUID owner;
    private final SimpleBlockLocation location;
    private final Material block;

    private Set<UUID> allowedPlayers = new LinkedHashSet<>();

    private Set<IdObject> allowedSettlements = new LinkedHashSet<>();
    private Set<IdObject> allowedEmpires = new LinkedHashSet<>();
    private boolean allowHoppers = false;

    public Lock(UUID owner, SimpleBlockLocation location, Material block) {
        this.owner = owner;
        this.location = location;
        this.block = block;
    }

    public UUID getOwnerId() {
        return owner;
    }

    public Resident getOwner() {
        return isValid() ? ResidentManager.getInstance().getResident(getOwnerId()) : null;
    }

    public boolean isOwner(Player player) {
        return isOwner(player.getUniqueId());
    }

    public boolean isOwner(Resident resident) {
        return isOwner(resident.getUUID());
    }

    public boolean isOwner(UUID playerId) {
        return getOwnerId().equals(playerId);
    }

    public SimpleBlockLocation getLocation() {
        return location;
    }

    public IdObject getId() {
        return id;
    }

    public void setId(IdObject id) {
        if (id == null) {
            id = IdObject.get(null);
        }
        this.id = id;
    }

    @Override
    public void save() {
        DatabaseManager.getInstance().saveLock(this);
    }

    public boolean canAccess(Player player) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (canAccess(resident)) {
            return true;
        }
        if (resident.hasSettlement() && canAccess(resident.getSettlement())) {
            return true;
        }
        if (resident.hasEmpire() && canAccess(resident.getSettlement().getEmpire())) {
            return true;
        }
        return player.hasPermission("settlements.bypass.lock");
    }

    public boolean canAccess(Targetable target) {
        if (target instanceof Resident) {
            return isOwner((Resident) target) || allowedPlayers.contains(((Resident) target).getUUID());
        } else if (target instanceof Settlement) {
            return allowedSettlements.contains(((Settlement) target).getId());
        } else if (target instanceof Empire) {
            return allowedEmpires.contains(((Empire) target).getId());
        }
        return false;
    }

    public boolean grantAccess(Targetable target) {
        if (target instanceof Resident) {
            return allowedPlayers.add(((Resident) target).getUUID());
        } else if (target instanceof Settlement) {
            return allowedSettlements.add(((Settlement) target).getId());
        } else if (target instanceof Empire) {
            return allowedEmpires.add(((Empire) target).getId());
        }
        return false;
    }

    public boolean revokeAccess(Targetable target) {
        if (target instanceof Resident) {
            return allowedPlayers.remove(((Resident) target).getUUID());
        } else if (target instanceof Settlement) {
            return allowedSettlements.remove(((Settlement) target).getId());
        } else if (target instanceof Empire) {
            return allowedEmpires.remove(((Empire) target).getId());
        }
        return false;
    }

    public boolean canAdministrate(Player player) {
        if (isOwner(player)) {
            return true;
        }

        OwnedLand land = LandManager.getInstance().getLand(getLocation().toChunkLocation());
        if (land != null) {
            Resident resident = ResidentManager.getInstance().getResident(player);
            if (land.getLandOwner().canAdminLocks(resident)) {
                return true;
            }
        }

        return player.hasPermission("settlements.bypass.lock");
    }

    public boolean isAllowHoppers() {
        return allowHoppers;
    }

    public void setAllowHoppers(boolean allowHoppers) {
        this.allowHoppers = allowHoppers;
    }

    public Material getBlock() {
        return block;
    }

    public Collection<UUID> getAllowedPlayers() {
        return allowedPlayers;
    }

    public Set<IdObject> getAllowedSettlementIds() {
        return allowedSettlements;
    }

    public Set<IdObject> getAllowedEmpireIds() {
        return allowedEmpires;
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("x", getLocation().getX());
        map.put("y", getLocation().getY());
        map.put("z", getLocation().getZ());
        map.put("world", getLocation().getWorld());
        map.put("owner", getOwnerId());
        map.put("block", getBlock().name());
        map.put("allowHoppers", isAllowHoppers());
        List<String> allowed = new ArrayList<>();
        getAllowedPlayers().stream().map(UUID::toString).forEach(allowed::add);
        getAllowedSettlementIds().stream().map(id -> "settlement:" + id).forEach(allowed::add);
        getAllowedEmpireIds().stream().map(id -> "empire:" + id).forEach(allowed::add);
        map.put("allowed", allowed);
        return map;
    }

    public static Lock deserialize(Map<String, Object> map) {
        if (map == null || map.get("owner") == null || map.get("block") == null || map.get("allowHoppers") == null || map.get("allowed") == null) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Lock! (" + map + ")");
            return null;
        }
        Lock lock = new Lock(
                UUID.fromString((String) map.get("owner")),
                SimpleBlockLocation.deserialize(map),
                MaterialUtils.getMaterial(((String) map.get("block")).toUpperCase())
        );
        if (lock.getLocation() == null) {
            return null;
        }
        lock.setId(IdObject.get(map));
        lock.setAllowHoppers(Utils.getBoolean(map.get("allowHoppers")));
        for (String s : ((Collection<String>) map.get("allowed"))) {
            String[] split = s.split(":", 2);
            try {
                if (split.length == 1) {
                    lock.getAllowedPlayers().add(UUID.fromString(split[0]));
                } else {
                    IdObject id = IdObject.get(Integer.parseInt(split[1]));
                    switch (split[0]) {
                        case "settlement":
                            Settlement settlement = SettlementManager.getInstance().getFromId(id);
                            if (settlement != null) {
                                lock.getAllowedSettlementIds().add(id);
                            }
                            break;
                        case "empire":
                            Empire empire = EmpireManager.getInstance().getEmpireFromId(id);
                            if (empire != null) {
                                lock.getAllowedEmpireIds().add(id);
                            }
                            break;
                    }
                }
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Allowed entry " + s + " could not be parsed for lock " + lock.getLocation().getKey() + "! " + e.getMessage());
            }
        }
        return lock;
    }

    public boolean isValid() {
        return owner != null && !owner.equals(Utils.ZERO_UUID);
    }

    @Override
    public String name() {
        return "Lock";
    }
}

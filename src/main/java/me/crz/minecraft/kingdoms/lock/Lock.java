package me.crz.minecraft.kingdoms.lock;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseSerializable;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleBlockLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class Lock implements DatabaseSerializable {
    private IdObject id = IdObject.get(null);
    private final UUID owner;
    private final SimpleBlockLocation location;
    private final Material block;

    private Set<UUID> allowedPlayers = new LinkedHashSet<>();

    private Set<IdObject> allowedVillages = new LinkedHashSet<>();
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
    public String getDbDescription() {
        return getLocation().getKey();
    }

    public boolean canAccess(Player player) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (canAccess(resident)) {
            return true;
        }
        if (resident.hasVillage() && canAccess(resident.getVillage())) {
            return true;
        }
        if (resident.hasEmpire() && canAccess(resident.getVillage().getEmpire())) {
            return true;
        }
        return player.hasPermission("kingdoms.bypass.lock");
    }

    public boolean canAccess(Targetable target) {
        if (target instanceof Resident) {
            return isOwner((Resident) target) || allowedPlayers.contains(((Resident) target).getUUID());
        } else if (target instanceof Village) {
            return allowedVillages.contains(((Village) target).getId());
        } else if (target instanceof Empire) {
            return allowedEmpires.contains(((Empire) target).getId());
        }
        return false;
    }

    public boolean grantAccess(Targetable target) {
        if (target instanceof Resident) {
            return allowedPlayers.add(((Resident) target).getUUID());
        } else if (target instanceof Village) {
            return allowedVillages.add(((Village) target).getId());
        } else if (target instanceof Empire) {
            return allowedEmpires.add(((Empire) target).getId());
        }
        return false;
    }

    public boolean revokeAccess(Targetable target) {
        if (target instanceof Resident) {
            return allowedPlayers.remove(((Resident) target).getUUID());
        } else if (target instanceof Village) {
            return allowedVillages.remove(((Village) target).getId());
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

        return player.hasPermission("kingdoms.bypass.lock");
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

    public Set<IdObject> getAllowedVillageIds() {
        return allowedVillages;
    }

    public Set<IdObject> getAllowedEmpireIds() {
        return allowedEmpires;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("x", getLocation().getX());
        map.put("y", getLocation().getY());
        map.put("z", getLocation().getZ());
        map.put("world", getLocation().getWorld());
        map.put("owner", getOwnerId());
        map.put("block", getBlock().name());
        map.put("allowHoppers", isAllowHoppers());
        List<String> allowed = new ArrayList<>();
        getAllowedPlayers().stream().map(UUID::toString).forEach(allowed::add);
        getAllowedVillageIds().stream().map(id -> "village:" + id).forEach(allowed::add);
        getAllowedEmpireIds().stream().map(id -> "empire:" + id).forEach(allowed::add);
        map.put("allowed", allowed);
        return map;
    }

    public static Lock deserialize(Map<String, Object> map) {
        if (map == null || map.get("owner") == null || map.get("block") == null || map.get("allowHoppers") == null || map.get("allowed") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Lock! (" + map + ")");
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
                        case "village":
                            Village village = VillageManager.getInstance().getVillageFromId(id);
                            if (village != null) {
                                lock.getAllowedVillageIds().add(id);
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
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Allowed entry " + s + " could not be parsed for lock " + lock.getLocation().getKey() + "! " + e.getMessage());
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

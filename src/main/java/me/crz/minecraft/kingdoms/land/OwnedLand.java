package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.village.PlotMode;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;

public class OwnedLand implements ConfigurationSerializable {
    private IdObject id;
    private Village ownerVillage;
    private UUID plotOwner = null;
    private SimpleChunkLocation location;
    private int unclaimIn = 0;
    private int price;
    private boolean forSale;
    private boolean pvp = false;
    private boolean mobSpawning = false;
    private boolean fireSpread = false;
    private boolean explosions = false;
    private String outpost = null;
    private String description = "";
    private PlotBuildState buildState = PlotBuildState.RESIDENTS;
    private EnumMap<Use, PlotBuildState> use = new EnumMap<>(Use.class);
    private SimpleChunkLocation linkedTo = null;
    private Set<SimpleChunkLocation> linked = new HashSet<>();
    private boolean embassy;

    public OwnedLand(Village ownerVillage, SimpleChunkLocation location) {
        this(ownerVillage, location, null, 0, false);
    }

    public OwnedLand(Village ownerVillage, SimpleChunkLocation location, UUID plotOwner, int price, boolean forSale) {
        this.ownerVillage = ownerVillage;
        this.location = location;
        this.plotOwner = plotOwner;
        this.forSale = forSale;
        this.price = price;
    }

    public PlotBuildState getUseState(Use u) {
        return use.getOrDefault(u, PlotBuildState.RESIDENTS);
    }

    public void setUseState(Use u, PlotBuildState state) {
        use.put(u, state);
    }

    public Map<Use, PlotBuildState> getUseStates() {
        return use;
    }

    public PlotBuildState getBuildState() {
        return buildState;
    }

    public void setBuildState(PlotBuildState state) {
        buildState = state;
    }

    public boolean isOutpost() {
        return outpost != null;
    }

    public void setOutpost(String name ) {
        if (name != null && name.isEmpty()) {
            outpost = null;
        } else {
            outpost = name;
        }
    }

    public String getOutpostName() {
        if (!isOutpost()) {
            return "-";
        }
        return outpost;
    }

    public Village getOwnerVillage() {
        return ownerVillage;
    }

    public void setOwnerVillage(Village ownerVillage) {
        this.ownerVillage = ownerVillage;
    }

    public boolean isOwnedBy(Village village) {
        return getOwnerVillage() != null && getOwnerVillage().equals(village);
    }

    public int getX() {
        return location.getX();
    }

    public void setX(int x) {
        this.location.setX(x);
    }

    public int getZ() {
        return location.getZ();
    }

    public void setZ(int z) {
        this.location.setZ(z);
    }

    public String getWorldName() {
        return location.getWorld();
    }

    public void setWorldName(String worldName) {
        location.setWorld(worldName);
    }

    public int getUnclaimIn() {
        return unclaimIn;
    }

    public void setUnclaimIn(int unclaimIn) {
        this.unclaimIn = unclaimIn;
    }

    /**
     * To ensure data consistency you should be using {@link LandManager#setPlotOwner(OwnedLand, UUID)} instead!
     */
    public void setPlotOwner(UUID plotOwner) {
        this.plotOwner = plotOwner;
    }

    public UUID getPlotOwner() {
        return plotOwner;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public boolean isForSale() {
        return forSale;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isEmbassy() {
        return embassy;
    }

    public void setEmbassy(boolean embassy) {
        this.embassy = embassy;
    }

    public SimpleChunkLocation getLocation() {
        return location;
    }

    public Chunk getChunk() {
        return location.getChunk();
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        Entity[] entities;
        for (int length = (entities = this.getChunk().getEntities()).length, i = 0; i < length; ++i) {
            Entity entity = entities[i];
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public boolean isMobSpawning() {
        return mobSpawning;
    }

    public void setMobSpawning(boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public void setFireSpread(boolean fireSpread) {
        this.fireSpread = fireSpread;
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(boolean explosions) {
        this.explosions = explosions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.length() > 255 ? description.substring(0, 255) : description;
    }

    public SimpleChunkLocation getLinkedTo() {
        return linkedTo;
    }

    public boolean isLinkedTo(OwnedLand land) {
        return land != null && land.getLocation().equals(linkedTo);
    }

    public void setLinkedTo(OwnedLand land) {
        if (land != null) {
            setLinkedToLocation(land.getLocation());
        } else {
            setLinkedToLocation(null);
        }
    }

    public void setLinkedToLocation(SimpleChunkLocation connectedTo) {
        this.linkedTo = connectedTo;
    }

    public Set<SimpleChunkLocation> getLinked() {
        return linked;
    }

    public boolean addLinked(OwnedLand land) {
        return linked.add(land.getLocation());
    }

    public boolean removeLinked(OwnedLand land) {
        return linked.remove(land.getLocation());
    }

    public boolean hasLink(OwnedLand land) {
        return linked.contains(land.getLocation());
    }

    public void setId(IdObject id) {
        this.id = id;
    }

    public IdObject getId() {
        return id;
    }

    public void highlightLand(Player player) {
        Chunk chunk = this.getChunk();
        ArrayList<Location> edgeBlocks = new ArrayList<>();
        for (int i = 0; i < 16; ++i) {
            for (int ii = -1; ii <= 10; ++ii) {
                edgeBlocks.add(chunk.getBlock(i, (int) player.getLocation().getY(), 15).getLocation());
                edgeBlocks.add(chunk.getBlock(i, (int) player.getLocation().getY(), 0).getLocation());
                edgeBlocks.add(chunk.getBlock(0, (int) player.getLocation().getY(), i).getLocation());
                edgeBlocks.add(chunk.getBlock(15, (int) player.getLocation().getY(), i).getLocation());
            }
        }
        ArrayList<Material> edgeMaterials = new ArrayList<>();
        for (Location edgeBlock : edgeBlocks) {
            edgeMaterials.add(edgeBlock.getBlock().getType());
            new BukkitRunnable() {
                public void run() {
                    Bukkit.getWorld(edgeBlock.getWorld().getName()).getBlockAt(edgeBlock).setType(Material.REDSTONE_BLOCK);
                }
            }.runTask(KingdomsPlugin.getInstance());
        }
        new BukkitRunnable() {
            public void run() {
                for (int i = 0; i < edgeMaterials.size(); ++i) {
                    Bukkit.getWorld(edgeBlocks.get(i).getWorld().getName()).getBlockAt(edgeBlocks.get(i)).setType(edgeMaterials.get(i));
                }
            }
        }.runTaskLater(KingdomsPlugin.getInstance(), 40L);
    }

    public boolean isPlot() {
        return this.getPlotOwner() != null;
    }

    public boolean isPlotOwner(Player player) {
        return player != null && isPlotOwner(player.getUniqueId());
    }

    public boolean isPlotOwner(Resident res) {
        return res != null && isPlotOwner(res.getUUID());
    }

    public boolean isPlotOwner(UUID uuid) {
        return uuid != null && uuid.equals(getPlotOwner());
    }

    public String getKey() {
        return location.getKey();
    }

    public String getSimpleKey() {
        return location.getSimpleKey();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ownerVillage", getOwnerVillage().getName());
        map.put("settlementId", getOwnerVillage().getId().value());
        map.put("x", getX());
        map.put("z", getZ());
        map.put("world", getWorldName());
        map.put("unclaimIn", getUnclaimIn());
        map.put("owner", getPlotOwner() != null ? getPlotOwner() : new UUID(0,0));
        map.put("price", getPrice());
        map.put("forSale", isForSale());
        map.put("embassy", isEmbassy());
        map.put("description", getDescription());
        map.put("pvp", isPvp());
        map.put("fireSpread", isFireSpread());
        map.put("explosions", isExplosions());
        map.put("mobSpawning", isMobSpawning());
        map.put("buildState", getBuildState().name());
        map.put("linkX", getLinkedTo() != null ? getLinkedTo().getX() : null);
        map.put("linkZ", getLinkedTo() != null ? getLinkedTo().getZ() : null);
        map.put("uses", getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
        return map;
    }

    public static OwnedLand deserialize(Map<String, Object> map) {
        if (map == null || map.get("world") == null || map.get("x") == null || map.get("z") == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize OwnedLand! (" + map + ")");
            return null;
        }
        OwnedLand land = new OwnedLand(
                map.get("ownerVillage") != null
                        ? VillageManager.getInstance().getVillageFromName((String) map.get("ownerVillage"))
                        : VillageManager.getInstance().getVillageFromId(IdObject.get(map.get("settlementId"))),
                new SimpleChunkLocation((String) map.get("world"), (Integer) map.get("x"), (Integer) map.get("z"))
        );
        land.setId(IdObject.get(map));
        land.setUnclaimIn((Integer) map.get("unclaimIn"));
        String plotOwnerStr = (String) map.get("owner");
        if (plotOwnerStr != null && !plotOwnerStr.isEmpty()) {
            land.setPlotOwner(UUID.fromString(plotOwnerStr));
            if (land.getPlotOwner().equals(new UUID(0, 0))) {
                land.setPlotOwner(null);
            }
        }
        land.setPrice((Integer) map.get("price"));
        land.setForSale((Boolean) map.get("forSale"));
        land.setEmbassy((Boolean) map.get("embassy"));
        land.setDescription((String) map.get("description"));
        land.setPvp((Boolean) map.get("pvp"));
        land.setFireSpread((Boolean) map.get("fireSpread"));
        land.setExplosions((Boolean) map.get("explosions"));
        land.setMobSpawning((Boolean) map.get("mobSpawning"));
        land.setBuildState(Utils.parseEnum((String) map.get("buildState"), PlotBuildState.RESIDENTS));

        if (map.get("linkX") != null && map.get("linkZ") != null) {
            land.setLinkedToLocation(new SimpleChunkLocation(land.getWorldName(), (Integer) map.get("connX"), (Integer) map.get("connZ")));
        }

        Map<String, Object> uses = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            if (uses.containsKey(u.name())) {
                land.setUseState(u, Utils.parseEnum((String) uses.get(u.name()), PlotBuildState.RESIDENTS));
            }
        }
        for (NamedLocation o : land.getOwnerVillage().getOutpostHomes().values()) {
            if (land.getLocation().contains(o)) {
                land.setOutpost(o.getName());
            }
        }
        return land;
    }

    public boolean syncLinked(OwnedLand land) {
        if (!isOwnedBy(land.getOwnerVillage()) || !isPlotOwner(land.getPlotOwner()) || !isLinkedTo(land)) {
            return false;
        }
        boolean r = false;
        for (PlotMode mode : PlotMode.values()) {
            if (mode.get(this) != mode.get(land)) {
                mode.set(mode.get(land), this);
                r = true;
            }
        }
        for (Use use : Use.values()) {
            if (getUseState(use) != land.getUseState(use)) {
                setUseState(use, land.getUseState(use));
                r = true;
            }
        }
        return r;
    }

    public boolean containsHome() {
        return getLocation().contains(getOwnerVillage().getVillageHome());
    }

    @Override
    public int hashCode() {
        return getLocation().hashCode();
    }
}

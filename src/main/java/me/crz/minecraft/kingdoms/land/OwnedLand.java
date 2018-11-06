package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.landflags.BuildState;
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

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.landflags.Use;

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
    private boolean buff = true;
    private String outpost = null;
    private String description = "";
    private BuildState buildState = BuildState.PRIVATE;
    private EnumMap<Use, BuildState> use = new EnumMap<>(Use.class);
    private boolean embassy;
    private UUID link;

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

    public BuildState getUseState(Use u) {
        return use.getOrDefault(u, u.getDefault());
    }

    public void setUseState(Use u, BuildState state) {
        if (state == u.getDefault()) {
            use.remove(u);
        } else {
            use.put(u, state);
        }
    }

    public Map<Use, BuildState> getUseStates() {
        return use;
    }

    public BuildState getBuildState() {
        return buildState;
    }

    public void setBuildState(BuildState state) {
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

    public boolean setPvp(boolean pvp) {
        if (this.pvp != pvp) {
            this.pvp = pvp;
            return true;
        }
        return false;
    }

    public boolean isMobSpawning() {
        return mobSpawning;
    }

    public boolean setMobSpawning(boolean mobSpawning) {
        if (this.mobSpawning != mobSpawning) {
            this.mobSpawning = mobSpawning;
            return true;
        }
        return false;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public boolean setFireSpread(boolean fireSpread) {
        if (this.fireSpread != fireSpread) {
            this.fireSpread = fireSpread;
            return true;
        }
        return false;
    }

    public boolean isExplosions() {
        return explosions;
    }

    public boolean setExplosions(boolean explosions) {
        if (this.explosions != explosions) {
            this.explosions = explosions;
            return true;
        }
        return false;
    }

    public boolean setBuff(boolean buff) {
        if (this.buff != buff) {
            this.buff = buff;
            return true;
        }
        return false;
    }

    public boolean isBuff() {
        return buff;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.length() > 255 ? description.substring(0, 255) : description;
    }

    public Collection<SimpleChunkLocation> getLinked() {
        return LandManager.getInstance().getLinked(this);
    }

    public boolean removeLink() {
        return LandManager.getInstance().removeLink(this);
    }

    public boolean isLinked(OwnedLand land) {
        return land != null && hasLink() && getLink().equals(land.getLink());
    }

    public UUID getLink() {
        return link;
    }

    /**
     * Use {@link LandManager#addLink(OwnedLand, UUID)} to ensure data consistency!
     */
    public void setLink(UUID link) {
        this.link = link;
    }

    public boolean hasLink() {
        return getLink() != null;
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
        map.put("buff", isBuff());
        map.put("buildState", getBuildState().name());
        map.put("link", getLink() != null ? getLink().toString() : null);
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
        land.setBuff((Boolean) map.get("buff"));
        land.setBuildState(Utils.parseEnum((String) map.get("buildState"), BuildState.PRIVATE));

        if (map.get("link") != null && !((String) map.get("link")).isEmpty()) {
            land.setLink(UUID.fromString((String) map.get("link")));
        }

        Map<String, Object> uses = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            if (uses.containsKey(u.name())) {
                land.setUseState(u, Utils.parseEnum((String) uses.get(u.name()), BuildState.PRIVATE));
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
        if (!isOwnedBy(land.getOwnerVillage()) || !isPlotOwner(land.getPlotOwner()) || !isLinked(land)) {
            return false;
        }
        boolean r = false;
        if (getBuildState() != land.getBuildState()) {
            setBuildState(land.getBuildState());
            r = true;
        }
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

    public String[] getReplacements() {
        return new String[] {
                "state", getOwnerVillage().getState().getTag(),
                "description", getDescription(),
                "settlement", getOwnerVillage().getName(),
                "spawning", String.valueOf(isMobSpawning()),
                "firespread", String.valueOf(isFireSpread()),
                "explosions", String.valueOf(isExplosions()),
                "pvp", String.valueOf(isPvp()),
                "build", getBuildState().getTag(),
                "embassy", String.valueOf(isEmbassy())
        };
    }

    @Override
    public int hashCode() {
        return getLocation().hashCode();
    }
}

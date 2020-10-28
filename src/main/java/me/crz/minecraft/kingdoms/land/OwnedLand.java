package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.DatabaseSerializable;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
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

public class OwnedLand extends SimpleChunkLocation implements DatabaseSerializable, ModeAble {
    private IdObject id = IdObject.get(null);
    private LandOwner landOwner;
    private UUID plotOwner = null;
    private int unclaimIn = 0;
    private int price = 0;
    private boolean forSale = false;
    private int tax = 0;
    private Map<PlotMode, Boolean> modes = new EnumMap<>(PlotMode.class);
    private String outpost = null;
    private String name = "";
    private String description = "";
    private BuildState buildState = BuildState.PRIVATE;
    private EnumMap<Use, BuildState> use = new EnumMap<>(Use.class);
    private UUID link;

    public OwnedLand(LandOwner landOwner, SimpleChunkLocation location) {
        super(location.getWorld(), location.getX(), location.getZ());
        this.landOwner = landOwner;
    }

    public BuildState getUseState(Use u) {
        return use.getOrDefault(u, landOwner.getUseState(u));
    }

    public boolean setUseState(Use u, BuildState state) {
        return use.put(u, state) != null;
    }

    public boolean hasUseState(Use u) {
        return use.containsKey(u);
    }

    public boolean unsetUseState(Use u) {
        return use.remove(u) != null;
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

    public LandOwner getLandOwner() {
        return landOwner;
    }

    public void setLandOwner(LandOwner landOwner) {
        this.landOwner = landOwner;
    }

    public boolean isOwnedBy(LandOwner landOwner) {
        return getLandOwner() != null && getLandOwner().equals(landOwner);
    }

    public boolean isOwnedBy(Resident resident) {
        return getLandOwner() != null && getLandOwner().isMember(resident);
    }

    public String getWorldName() {
        return getWorld();
    }

    public void setWorldName(String worldName) {
        setWorld(worldName);
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

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public boolean isEmbassy() {
        return getMode(PlotMode.embassy);
    }

    public void setEmbassy(boolean embassy) {
        setMode(PlotMode.embassy, embassy);
    }

    public SimpleChunkLocation getLocation() {
        return this;
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

    @Override
    public Map<PlotMode, Boolean> getModes() {
        return modes;
    }

    public boolean unsetMode(PlotMode mode) {
        return getModes().remove(mode) != null;
    }

    public boolean hasMode(PlotMode mode) {
        return getModes().containsKey(mode);
    }

    @Override
    public boolean getMode(PlotMode mode) {
        return getModes().getOrDefault(mode, getLandOwner().getMode(mode));
    }

    @Override
    public boolean setMode(PlotMode mode, boolean value) {
        if (ModeAble.super.setMode(mode, value)) {
            if (KingdomsPlugin.getInstance().isRunning() && mode == PlotMode.mobSpawning && !value) {
                World world = Bukkit.getWorld(getWorldName());
                if (world != null && world.isChunkLoaded(getX(), getZ())) {
                    for (Entity entity : getChunk().getEntities()) {
                        if (entity instanceof LivingEntity && Utils.isHostile((LivingEntity) entity)) {
                            entity.remove();
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        int maxLength = Math.min(Config.Plot.NAME_LENGTH, 255);
        this.name = ChatColor.stripColor(name).length() > maxLength ? name.substring(0, maxLength) : name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = ChatColor.stripColor(description).length() > Config.Plot.DESCRIPTION_LENGTH ? description.substring(0, Config.Plot.DESCRIPTION_LENGTH) : description;
    }

    public Collection<OwnedLand> getLinked() {
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
        if (id == null) {
            id = IdObject.get(null);
        }
        this.id = id;
    }

    @Override
    public String getDbName() {
        return "claim";
    }

    @Override
    public String getDbDescription() {
        return getKey();
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("landOwner", getLandOwner().getName());
        map.put("settlementId", getLandOwner() instanceof Village && !getLandOwner().getId().isNull() ? getLandOwner().getId().value() : -1);
        map.put("x", getX());
        map.put("z", getZ());
        map.put("world", getWorldName());
        map.put("unclaimIn", getUnclaimIn());
        map.put("owner", getPlotOwner() != null ? getPlotOwner() : Utils.ZERO_UUID);
        map.put("price", getPrice());
        map.put("forSale", isForSale());
        map.put("tax", getTax());
        map.put("name", getName());
        map.put("description", getDescription());
        map.put("embassy", isEmbassy());
        map.put("modes", getModes().entrySet().stream().filter(e -> e.getKey().appliesTo(PlotMode.AppliesTo.LAND_OWNER)).collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)));
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
        SimpleChunkLocation location = new SimpleChunkLocation((String) map.get("world"), (Integer) map.get("x"), (Integer) map.get("z"));
        LandOwner landOwner = EmpireManager.getInstance().getEmpireFromWorld(location.getWorld());
        if (landOwner == null) {
            landOwner = map.get("landOwner") != null
                    ? VillageManager.getInstance().getVillageFromName((String) map.get("landOwner"))
                    : VillageManager.getInstance().getVillageFromId(IdObject.get(map.get("settlementId")));
        }
        if (landOwner == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not find owner for OwnedLand! Removing it from database... (" + map + ")");
            DatabaseManager.getInstance().deleteLand(IdObject.get(map));
            return null;
        }
        OwnedLand land = new OwnedLand(landOwner, location);
        land.setId(IdObject.get(map));
        land.setUnclaimIn((Integer) map.get("unclaimIn"));
        String plotOwnerStr = (String) map.get("owner");
        if (plotOwnerStr != null && !plotOwnerStr.isEmpty()) {
            land.setPlotOwner(UUID.fromString(plotOwnerStr));
            if (land.getPlotOwner().equals(Utils.ZERO_UUID)) {
                land.setPlotOwner(null);
            }
        }
        land.setName((String) map.get("name"));
        land.setDescription((String) map.get("description"));
        land.setPrice((Integer) map.get("price"));
        land.setForSale((Boolean) map.get("forSale"));
        land.setTax((Integer) map.get("tax"));
        land.setEmbassy((Boolean) map.get("embassy"));
        Map<String, Object> modes = (Map<String, Object>) map.get("modes");
        for (PlotMode mode : PlotMode.values()) {
            if (modes.containsKey(mode.name())) {
                land.setMode(mode, (Boolean) modes.get(mode.name()));
            }
        }
        land.setBuildState(Utils.parseEnum((String) map.get("buildState"), land.getBuildState()));

        if (map.get("link") != null && !((String) map.get("link")).isEmpty()) {
            land.setLink(UUID.fromString((String) map.get("link")));
        }

        Map<String, Object> uses = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            if (uses.containsKey(u.name())) {
                land.setUseState(u, Utils.parseEnum((String) uses.get(u.name()), u.getDefault(land.getBuildState())));
            }
        }
        if (land.getLandOwner() instanceof Village) {
            for (NamedLocation o : ((Village) land.getLandOwner()).getOutpostHomes().values()) {
                if (land.contains(o)) {
                    land.setOutpost(o.getName());
                }
            }
        }
        return land;
    }

    public boolean syncLinked(OwnedLand land) {
        if (!isOwnedBy(land.getLandOwner()) || !isPlotOwner(land.getPlotOwner()) || !isLinked(land)) {
            return false;
        }
        boolean r = false;
        if (!getName().equals(land.getName())) {
            setName(land.getName());
            r = true;
        }
        if (!getDescription().equals(land.getDescription())) {
            setDescription(land.getDescription());
            r = true;
        }
        if (getBuildState() != land.getBuildState()) {
            setBuildState(land.getBuildState());
            r = true;
        }
        for (PlotMode mode : PlotMode.values(PlotMode.AppliesTo.LAND)) {
            if (land.hasMode(mode)) {
                r |= setMode(mode, land.getMode(mode));
            } else {
                r |= unsetMode(mode);
            }
        }
        for (Use use : Use.values()) {
            if (land.hasUseState(use)) {
                r |= setUseState(use, land.getUseState(use));
            } else if (hasUseState(use)) {
                r |= unsetUseState(use);
            }
        }
        return r;
    }

    public boolean containsHome() {
        return getLandOwner() != null && contains(getLandOwner().getHome());
    }

    public String[] getReplacements() {
        return new String[] {
                "state", getLandOwner().getStateTag(),
                "name", getName(),
                "description", getDescription(),
                "landowner", getLandOwner().getName(),
                "hostilespawning", String.valueOf(getMode(PlotMode.mobSpawning)),
                "passivespawning", String.valueOf(getMode(PlotMode.passiveSpawning)),
                "firespread", String.valueOf(getMode(PlotMode.fireSpread)),
                "explosions", String.valueOf(getMode(PlotMode.explosions)),
                "pvp", String.valueOf(getMode(PlotMode.pvp)),
                "buff", String.valueOf(getMode(PlotMode.buff)),
                "build", isPlot() || getBuildState().appliesTo(BuildState.AppliesTo.get(getLandOwner())) ? getBuildState().getTag() : getLandOwner().getBuildState().getTag(),
                "buildsource", isPlot() || getBuildState().appliesTo(BuildState.AppliesTo.get(getLandOwner())) ? Message.get("names.plot").getText() : getLandOwner().getStateTag(),
                "embassy", String.valueOf(isEmbassy()),
                "x", String.valueOf(getX()),
                "z", String.valueOf(getZ()),
                "world", getWorldName(),
                "tax", String.valueOf(tax),
                "price", String.valueOf(price)
        };
    }

    @Override
    public int hashCode() {
        // Do not change or everything breaks!
        return super.hashCode();
    }

    public boolean canAdmin(Resident resident) {
        return getLandOwner().canAdminPlots(resident);
    }

    public ConfigMessages getMapKey() {
        return new ConfigMessages() {
            @Override
            public String type() {
                return "command";
            }

            @Override
            public String name() {
                return getLandOwner() != null ? getLandOwner().getKey() + ".map" : "village.map";
            }
        };
    }
}

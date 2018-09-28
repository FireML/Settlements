package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.landflags.Use;
import org.bson.Document;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;

public class OwnedLand {
    private String ownerVillage;
    private UUID plotOwner = null;
    private SimpleChunkLocation location;
    private int price;
    private boolean forSale;
    private boolean pvp = false;
    private boolean mobSpawning = false;
    private boolean fireSpread = false;
    private boolean explosions = false;
    private boolean build = false;
    private boolean isOutpost = false;
    private PlotBuildState buildState = PlotBuildState.RESIDENTS;
    private EnumMap<Use, PlotBuildState> use = new EnumMap<>(Use.class);

    public OwnedLand(String ownerVillage, SimpleChunkLocation location) {
        this(ownerVillage, location, null, 0, false);
    }

    public OwnedLand(String ownerVillage, SimpleChunkLocation location, UUID plotOwner, int price, boolean forSale) {
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

    public PlotBuildState getBuildState() {
        return buildState;
    }

    public void setBuildState(PlotBuildState state) {
        buildState = state;
    }

    public boolean getIsOutpost() {
        return isOutpost;
    }

    public void setIsOutpost(boolean b) {
        isOutpost = b;
    }

    public boolean getBuild() {
        return build;
    }

    public void setBuild(boolean b) {
        build = b;
    }

    public String getOwnerVillage() {
        return ownerVillage;
    }

    public void setOwnerVillage(String ownerVillage) {
        this.ownerVillage = ownerVillage;
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
        return isPlotOwner(player.getUniqueId());
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
}

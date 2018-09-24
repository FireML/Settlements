package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.landflags.Use;
import org.bson.Document;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;

public class OwnedLand {
    private String ownerVillage;
    private String worldName;
    private String plotOwner;
    private int x;
    private int z;
    private int price;
    private boolean forSale;
    private boolean pvp;
    private boolean mobSpawning;
    private boolean fireSpread;
    private boolean explosions;
    private boolean build;
    private boolean isOutpost;
    private PlotBuildState buildState;
    private Document document;
    private Document use;

    public OwnedLand(final Document doc) {
        super();
        document = doc;
        ownerVillage = document.getString("ownerVillage");
        x = document.getInteger("x");
        z = document.getInteger("z");
        worldName = document.getString("worldName");
        plotOwner = document.getString("plotOwner");
        price = document.getInteger("price");
        forSale = document.getBoolean("forSale");
        mobSpawning = document.getBoolean("mobSpawning");
        pvp = doc.getBoolean("pvp");
        fireSpread = doc.getBoolean("fireSpread");
        explosions = doc.getBoolean("explosions");
        if (doc.containsKey("build")) {
            build = doc.getBoolean("build");
        } else {
            build = false;
        }
        if (doc.containsKey("isOutpost")) {
            isOutpost = doc.getBoolean("isOutpost");
        } else {
            isOutpost = false;
        }
        if (doc.containsKey("buildState")) {
            buildState = PlotBuildState.valueOf(doc.getString("buildState"));
        } else {
            buildState = PlotBuildState.RESIDENTS;
        }
        if (doc.containsKey("use")) {
            use = (Document) doc.get("use");
        } else {
            final Document useDoc = new Document();
            Use[] values;
            for (int length = (values = Use.values()).length, i = 0; i < length; ++i) {
                final Use u = values[i];
                useDoc.put(u.name(), PlotBuildState.RESIDENTS.name());
            }
            document.put("use", useDoc);
            use = useDoc;
        }
    }

    public OwnedLand(final String ownerVillage, final int x, final int z, final String worldName, final String plotOwner, final int price, final boolean forSale) {
        super();
        this.ownerVillage = ownerVillage;
        this.x = x;
        this.z = z;
        this.worldName = worldName;
        this.plotOwner = plotOwner;
        this.forSale = forSale;
        this.price = price;
        pvp = false;
        mobSpawning = false;
        fireSpread = false;
        explosions = false;
        build = false;
        isOutpost = false;
        buildState = PlotBuildState.RESIDENTS;
        (document = new Document()).put("ownerVillage", ownerVillage);
        document.put("x", x);
        document.put("z", z);
        document.put("worldName", worldName);
        document.put("plotOwner", plotOwner);
        document.put("price", price);
        document.put("forSale", forSale);
        document.put("pvp", pvp);
        document.put("mobSpawning", mobSpawning);
        document.put("fireSpread", fireSpread);
        document.put("explosions", explosions);
        document.put("build", build);
        document.put("isOutpost", isOutpost);
        document.put("buildState", buildState.name());
        final Document useDoc = new Document();
        for (Use u : Use.values()) {
            useDoc.put(u.name(), PlotBuildState.RESIDENTS.name());
        }
        document.put("use", useDoc);
        use = useDoc;
    }

    public PlotBuildState getUseState(final Use u) {
        if (use.containsKey(u.name())) {
            return PlotBuildState.valueOf(use.getString(u.name()));
        }
        return PlotBuildState.RESIDENTS;
    }

    public void setUseState(final Use u, final PlotBuildState state) {
        use.put(u.name(), state.name());
        document.put("use", use);
    }

    public PlotBuildState getBuildState() {
        return buildState;
    }

    public void setBuildState(final PlotBuildState state) {
        buildState = state;
        document.put("buildState", state.name());
    }

    public boolean getIsOutpost() {
        return isOutpost;
    }

    public void setIsOutpost(final Boolean b) {
        isOutpost = b;
        document.put("isOutpost", b);
    }

    public boolean getBuild() {
        return build;
    }

    public void setBuild(final Boolean b) {
        build = b;
        document.put("build", b);
    }

    public String getOwnerVillage() {
        return ownerVillage;
    }

    public void setOwnerVillage(final String ownerVillage) {
        this.ownerVillage = ownerVillage;
        document.put("ownerVillage", ownerVillage);
    }

    public int getX() {
        return x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getZ() {
        return z;
    }

    public void setZ(final int z) {
        this.z = z;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(final String worldName) {
        this.worldName = worldName;
    }

    public void setPlotOwner(final String plotOwner) {
        this.plotOwner = plotOwner;
        document.put("plotOwner", plotOwner);
    }

    public String getPlotOwner() {
        return plotOwner;
    }

    public void setForSale(final boolean forSale) {
        this.forSale = forSale;
        document.put("forSale", forSale);
    }

    public boolean isForSale() {
        return forSale;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(final int price) {
        this.price = price;
        document.put("price", price);
    }

    public Chunk getChunk() {
        return Bukkit.getServer().getWorld(worldName).getChunkAt(x, z);
    }

    public List<Player> getPlayers() {
        final List<Player> players = new ArrayList<>();
        Entity[] entities;
        for (int length = (entities = this.getChunk().getEntities()).length, i = 0; i < length; ++i) {
            final Entity entity = entities[i];
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }

    public boolean isPvp() {
        return pvp;
    }

    public void setPvp(final boolean pvp) {
        this.pvp = pvp;
        document.put("pvp", pvp);
    }

    public boolean isMobSpawning() {
        return mobSpawning;
    }

    public void setMobSpawning(final boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
        document.put("mobSpawning", mobSpawning);
        LandManager.getInstance().saveMobData(this, mobSpawning);
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public void setFireSpread(final boolean fireSpread) {
        this.fireSpread = fireSpread;
        document.put("fireSpread", fireSpread);
    }

    public boolean isExplosions() {
        return explosions;
    }

    public void setExplosions(final boolean explosions) {
        this.explosions = explosions;
        document.put("explosions", explosions);
    }

    public void highlightLand(final Player player) {
        final Chunk chunk = this.getChunk();
        final ArrayList<Location> edgeBlocks = new ArrayList<>();
        for (int i = 0; i < 16; ++i) {
            for (int ii = -1; ii <= 10; ++ii) {
                edgeBlocks.add(chunk.getBlock(i, (int) player.getLocation().getY(), 15).getLocation());
                edgeBlocks.add(chunk.getBlock(i, (int) player.getLocation().getY(), 0).getLocation());
                edgeBlocks.add(chunk.getBlock(0, (int) player.getLocation().getY(), i).getLocation());
                edgeBlocks.add(chunk.getBlock(15, (int) player.getLocation().getY(), i).getLocation());
            }
        }
        final ArrayList<Material> edgeMaterials = new ArrayList<>();
        for (final Location edgeBlock : edgeBlocks) {
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

    public Document getDocument() {
        return document;
    }

    public boolean isPlot() {
        return !this.getPlotOwner().equals("");
    }
}

package me.crz.minecraft.kingdoms.land;

import java.util.Iterator;
import org.bukkit.plugin.Plugin;
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

public class OwnedLand
{
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
        ownerVillage = document.getString((Object)"ownerVillage");
        x = document.getInteger((Object)"x");
        z = document.getInteger((Object)"z");
        worldName = document.getString((Object)"worldName");
        plotOwner = document.getString((Object)"plotOwner");
        price = document.getInteger((Object)"price");
        forSale = document.getBoolean((Object)"forSale");
        mobSpawning = document.getBoolean((Object)"mobSpawning");
        pvp = doc.getBoolean((Object)"pvp");
        fireSpread = doc.getBoolean((Object)"fireSpread");
        explosions = doc.getBoolean((Object)"explosions");
        if (doc.containsKey((Object)"build")) {
            build = doc.getBoolean((Object)"build");
        }
        else {
            build = false;
        }
        if (doc.containsKey((Object)"isOutpost")) {
            isOutpost = doc.getBoolean((Object)"isOutpost");
        }
        else {
            isOutpost = false;
        }
        if (doc.containsKey((Object)"buildState")) {
            buildState = PlotBuildState.valueOf(doc.getString((Object)"buildState"));
        }
        else {
            buildState = PlotBuildState.RESIDENTS;
        }
        if (doc.containsKey((Object)"use")) {
            use = (Document)doc.get((Object)"use");
        }
        else {
            final Document useDoc = new Document();
            Use[] values;
            for (int length = (values = Use.values()).length, i = 0; i < length; ++i) {
                final Use u = values[i];
                useDoc.put(u.name(), (Object)PlotBuildState.RESIDENTS.name());
            }
            document.put("use", (Object)useDoc);
            use = useDoc;
        }
    }
    
    public OwnedLand(final String ownerVillage, final int x, final int z, final String worldName, final String plotOwner, final int price, final boolean forSale) {
        super();
        ownerVillage = ownerVillage;
        x = x;
        z = z;
        worldName = worldName;
        plotOwner = plotOwner;
        forSale = forSale;
        price = price;
        pvp = false;
        mobSpawning = false;
        fireSpread = false;
        explosions = false;
        build = false;
        isOutpost = false;
        buildState = PlotBuildState.RESIDENTS;
        (document = new Document()).put("ownerVillage", (Object)ownerVillage);
        document.put("x", (Object)x);
        document.put("z", (Object)z);
        document.put("worldName", (Object)worldName);
        document.put("plotOwner", (Object)plotOwner);
        document.put("price", (Object)price);
        document.put("forSale", (Object)forSale);
        document.put("pvp", (Object)pvp);
        document.put("mobSpawning", (Object)mobSpawning);
        document.put("fireSpread", (Object)fireSpread);
        document.put("explosions", (Object)explosions);
        document.put("build", (Object)build);
        document.put("isOutpost", (Object)isOutpost);
        document.put("buildState", (Object)buildState.name());
        final Document useDoc = new Document();
        Use[] values;
        for (int length = (values = Use.values()).length, i = 0; i < length; ++i) {
            final Use u = values[i];
            useDoc.put(u.name(), (Object)PlotBuildState.RESIDENTS.name());
        }
        document.put("use", (Object)useDoc);
        use = useDoc;
    }
    
    public PlotBuildState getUseState(final Use u) {
        if (use.containsKey((Object)u.name())) {
            return PlotBuildState.valueOf(use.getString((Object)u.name()));
        }
        return PlotBuildState.RESIDENTS;
    }
    
    public void setUseState(final Use u, final PlotBuildState state) {
        use.put(u.name(), (Object)state.name());
        document.put("use", (Object)use);
    }
    
    public PlotBuildState getBuildState() {
        return buildState;
    }
    
    public void setBuildState(final PlotBuildState state) {
        buildState = state;
        document.put("buildState", (Object)state.name());
    }
    
    public boolean getIsOutpost() {
        return isOutpost;
    }
    
    public void setIsOutpost(final Boolean b) {
        isOutpost = b;
        document.put("isOutpost", (Object)b);
    }
    
    public boolean getBuild() {
        return build;
    }
    
    public void setBuild(final Boolean b) {
        build = b;
        document.put("build", (Object)b);
    }
    
    public String getOwnerVillage() {
        return ownerVillage;
    }
    
    public void setOwnerVillage(final String ownerVillage) {
        this.ownerVillage = ownerVillage;
        document.put("ownerVillage", (Object)ownerVillage);
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
        document.put("plotOwner", (Object)plotOwner);
    }
    
    public String getPlotOwner() {
        return plotOwner;
    }
    
    public void setForSale(final boolean forSale) {
        this.forSale = forSale;
        document.put("forSale", (Object)forSale);
    }
    
    public boolean isForSale() {
        return forSale;
    }
    
    public int getPrice() {
        return price;
    }
    
    public void setPrice(final int price) {
        this.price = price;
        document.put("price", (Object)price);
    }
    
    public Chunk getChunk() {
        return Bukkit.getServer().getWorld(worldName).getChunkAt(x, z);
    }
    
    public List<Player> getPlayers() {
        final List<Player> players = new ArrayList<Player>();
        Entity[] entities;
        for (int length = (entities = this.getChunk().getEntities()).length, i = 0; i < length; ++i) {
            final Entity entity = entities[i];
            if (entity instanceof Player) {
                players.add((Player)entity);
            }
        }
        return players;
    }
    
    public boolean isPvp() {
        return pvp;
    }
    
    public void setPvp(final boolean pvp) {
        this.pvp = pvp;
        document.put("pvp", (Object)pvp);
    }
    
    public boolean isMobSpawning() {
        return mobSpawning;
    }
    
    public void setMobSpawning(final boolean mobSpawning) {
        this.mobSpawning = mobSpawning;
        document.put("mobSpawning", (Object)mobSpawning);
        LandManager.getInstance().saveMobData(this, mobSpawning);
    }
    
    public boolean isFireSpread() {
        return fireSpread;
    }
    
    public void setFireSpread(final boolean fireSpread) {
        this.fireSpread = fireSpread;
        document.put("fireSpread", (Object)fireSpread);
    }
    
    public boolean isExplosions() {
        return explosions;
    }
    
    public void setExplosions(final boolean explosions) {
        this.explosions = explosions;
        document.put("explosions", (Object)explosions);
    }
    
    public void highlightLand(final Player player) {
        final Chunk chunk = this.getChunk();
        final ArrayList<Location> edgeBlocks = new ArrayList<Location>();
        for (int i = 0; i < 16; ++i) {
            for (int ii = -1; ii <= 10; ++ii) {
                edgeBlocks.add(chunk.getBlock(i, (int)player.getLocation().getY(), 15).getLocation());
                edgeBlocks.add(chunk.getBlock(i, (int)player.getLocation().getY(), 0).getLocation());
                edgeBlocks.add(chunk.getBlock(0, (int)player.getLocation().getY(), i).getLocation());
                edgeBlocks.add(chunk.getBlock(15, (int)player.getLocation().getY(), i).getLocation());
            }
        }
        final ArrayList<Material> edgeMaterials = new ArrayList<Material>();
        for (final Location edgeBlock : edgeBlocks) {
            edgeMaterials.add(edgeBlock.getBlock().getType());
            new BukkitRunnable() {
                final /* synthetic */ OwnedLand this$0;
                private final /* synthetic */ Location val$edgeBlock;
                
                OwnedLand$1() {
                    this$0 = this$0;
                    super();
                }
                
                public void run() {
                    Bukkit.getWorld(edgeBlock.getWorld().getName()).getBlockAt(edgeBlock).setType(Material.REDSTONE_BLOCK);
                }
            }.runTask((Plugin)KingdomsPlugin.getInstance());
        }
        new BukkitRunnable() {
            final /* synthetic */ OwnedLand this$0;
            private final /* synthetic */ ArrayList val$edgeMaterials;
            private final /* synthetic */ ArrayList val$edgeBlocks;
            
            OwnedLand$2() {
                this$0 = this$0;
                super();
            }
            
            public void run() {
                for (int i = 0; i < edgeMaterials.size(); ++i) {
                    Bukkit.getWorld(((Location)edgeBlocks.get(i)).getWorld().getName()).getBlockAt((Location)edgeBlocks.get(i)).setType((Material)edgeMaterials.get(i));
                }
            }
        }.runTaskLater((Plugin)KingdomsPlugin.getInstance(), 40L);
    }
    
    public Document getDocument() {
        return document;
    }
    
    public boolean isPlot() {
        return !this.getPlotOwner().equals("");
    }
}

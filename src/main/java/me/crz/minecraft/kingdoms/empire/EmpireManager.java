package me.crz.minecraft.kingdoms.empire;

import co.aikar.timings.lib.MCTiming;
import io.papermc.lib.PaperLib;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.land.BuildState;
import me.crz.minecraft.kingdoms.land.Use;
import me.crz.minecraft.kingdoms.lock.LockProvider;
import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class EmpireManager {
    private static EmpireManager instance;
    private final Map<String, Empire> empires = new ConcurrentHashMap<>();
    private final Map<IdObject, Empire> empireIds = new ConcurrentHashMap<>();
    private final Map<UUID, Empire> empireUuids = new ConcurrentHashMap<>();
    private final Map<String, Empire> empireWorlds = new ConcurrentHashMap<>();

    private Map<String, EmpirePerk> perks;
    private Map<String, EmpirePerk> perkTags;


    public EmpireManager() {
        super();
        loadPerks();
        for (Empire empire : DatabaseManager.getInstance().getAllEmpires()) {
            if (Config.Empire.World.RENAME) {
                World world = KingdomsPlugin.getInstance().getServer().getWorld(empire.getWorldName());
                if (world != null) {
                    writeIdFile(world, empire);
                }
            }
            empires.put(empire.getName().replace(' ', '_').toLowerCase(), empire);
            empireIds.put(empire.getId(), empire);
            empireUuids.put(empire.getUUID(), empire);
            if (empire.isAbandoned()) {
                long timeAbandoned = System.currentTimeMillis() - empire.getCreated();
                long abandonTime = TimeUnit.MINUTES.toMillis(Config.Empire.Upkeep.ABANDON_TIME);
                long delay = timeAbandoned > abandonTime ? 0 : abandonTime - timeAbandoned;
                KingdomsPlugin.logDebug(empire.getStateTag() + " " + empire.getName() + " will be removed after " + Config.Empire.Upkeep.ABANDON_TIME + " minutes in " + delay + "!");
                Utils.runLater(() -> {
                    KingdomsPlugin.getInstance().getLogger().log(Level.INFO, empire.getStateTag() + " " + empire.getName() + " was removed because it has been abandoned for " + Config.Empire.Upkeep.ABANDON_TIME + " minutes!");
                    deleteEmpire(empire);
                    MessageUtils.broadcast(Message.get("broadcast.empire-deletion"), "name", empire.getName());
                }, delay, TimeUnit.MILLISECONDS);
            }
        }

        // Loop through all worlds to find empire ones
        for (File world : KingdomsPlugin.getInstance().getServer().getWorldContainer().listFiles(f -> f.isDirectory())) {
            Empire empire = getEmpireFromWorld(world);
            if (empire != null) {
                empire.setWorldName(world.getName());
            }
        }
    }

    private void writeIdFile(World world, Empire empire) {
        empireWorlds.put(world.getWorldFolder().getName(), empire);
        File idFile = new File(world.getWorldFolder(), "empire.id");
        if (!idFile.exists()) {
            try (FileWriter writer = new FileWriter(idFile)) {
                writer.write(String.valueOf(empire.getId().value()));
            } catch (IOException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to write empire id into world folder " + world.getName(), e);
            }
        }
    }

    public void loadPerks() {
        perks = new LinkedHashMap<>();
        perkTags = new LinkedHashMap<>();
        for (String perkName : ConfigManager.getInstance().getEmpirePerksConfig().getKeys(false)) {
            try {
                EmpirePerk perk = new EmpirePerk(ConfigManager.getInstance().getEmpirePerksConfig().getConfigurationSection(perkName));
                perks.put(perk.name().toLowerCase(), perk);
                perkTags.put(perk.getTag().toLowerCase(), perk);
            } catch (IllegalArgumentException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Config for Perk " + perkName + " was invalid! " + e.getMessage());
            }
        }
    }

    public void loadEmpireWorlds() {
        try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Empire world creation")) {
            for (Empire empire : getEmpires()) {
                createEmpireWorld(empire);
            }
        }
    }

    synchronized public static EmpireManager getInstance() {
        if (instance == null) {
            instance = new EmpireManager();
        }
        return instance;
    }

    public Collection<Empire> getEmpires () {
        return empireIds.values();
    }

    public Empire getEmpireFromName(String name) {
        return empires.get(name.replace(' ', '_').toLowerCase());
    }

    public Empire getEmpireFromId(IdObject id) {
        if (id == null || id.isNull()) {
            return null;
        }
        return empireIds.get(id);
    }
    public Empire getEmpireFromUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return empireUuids.get(uuid);
    }

    public Empire getEmpireFromPlayer(Player player) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        return village != null ? village.getEmpire() : null;
    }

    public EmpirePerk getPerk(String name) {
        EmpirePerk perk = perks.get(name.toLowerCase());
        if (perk == null) {
            perk = perkTags.get(name.toLowerCase());
        }
        return perk;
    }

    public Collection<EmpirePerk> getPerks() {
        return perks.values();
    }

    public void registerNewEmpire(Empire empire) {
        if (!empire.getId().isNull()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving empire " + empire.getName() + " to the database! It already had an ID defined?");
            return;
        }
        empire.setBank(Config.Empire.START_BALANCE);
        empire.getCapital().setEmpire(empire);
        empire.getCapital().getChief().setEmpireRank(EmpireRank.getLeader());
        empires.put(empire.getName().replace(' ', '_').toLowerCase(), empire);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveEmpire(empire);
            DatabaseManager.getInstance().saveVillage(empire.getCapital());
            DatabaseManager.getInstance().saveResident(empire.getCapital().getChief());
            if (!empire.getId().isNull()) {
                if (Config.Empire.World.RENAME) {
                    empire.setWorldName(Config.Empire.World.PREFIX + empire.getName() + Config.Empire.World.SUFFIX);
                } else {
                    empire.setWorldName(Config.Empire.World.PREFIX + "id" + empire.getId().value() + Config.Empire.World.SUFFIX);
                }
                empireIds.put(empire.getId(), empire);
                empireUuids.put(empire.getUUID(), empire);
                Utils.runSync(() -> {
                    try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Empire world creation")) {
                        createEmpireWorld(empire);
                        Message.get("empire-world-generated").tell(empire.getOnlineResidents());
                    }
                });
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving empire " + empire.getName() + " to the database! No ID was returned.");
            }
        });
    }

    private World createEmpireWorld(Empire empire) {
        String worldName = empire.getWorldName();
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            WorldCreator worldCreator = new WorldCreator(worldName);
            if (!Config.Empire.World.GENERATOR.isEmpty()) {
                worldCreator.generator(Config.Empire.World.GENERATOR);
                if (!Config.Empire.World.GENERATOR_SETTINGS.isEmpty()) {
                    worldCreator.generatorSettings(Config.Empire.World.GENERATOR_SETTINGS);
                }
            }
            if (!Config.Empire.World.SEED.isEmpty()) {
                try {
                    worldCreator.seed(Long.parseLong(Config.Empire.World.SEED));
                } catch (NumberFormatException e) {
                    worldCreator.seed(Config.Empire.World.SEED.hashCode());
                }
            }
            worldCreator.generateStructures(Config.Empire.World.STRUCTURES);
            if (Config.Empire.World.ENVIRONMENT != null) {
                worldCreator.environment(Config.Empire.World.ENVIRONMENT);
            }
            if (Config.Empire.World.TYPE != null) {
                worldCreator.type(Config.Empire.World.TYPE);
            }
            world = Bukkit.createWorld(worldCreator);
            if (world == null) {
                return null;
            }
        }
        writeIdFile(world, empire);
        world.getWorldBorder().setCenter(0, 0);
        world.getWorldBorder().setSize(empire.getWorldRadius() * 2);
        if (empire.getHome() == null) {
            empire.setHome(new SimpleLocation(worldName, 0, world.getHighestBlockYAt(0, 0), 0));
        } else {
            empire.getHome().setWorld(worldName);
        }
        world.setSpawnLocation(empire.getHome().toBukkitLocation());
        if (KingdomsPlugin.getInstance().getMultiverse() != null) {
            KingdomsPlugin.getInstance().getMultiverse().importWorld(world);
        }
        return world;
    }

    public void deleteEmpire(Empire empire) {
        empires.remove(empire.getName().toLowerCase());
        empireIds.remove(empire.getId());
        empireUuids.remove(empire.getUUID());
        empireWorlds.remove(empire.getWorldName());
        KingdomsPlugin.getInstance().addNewlyDeleted(empire);
        for (Village member : empire.getMembers()) {
            if (empire.equals(member.getEmpire())) {
                member.setEmpire(null);
            } else {
                empire.getMemberIds().remove(member.getId());
            }
        }
        World empireWorld = Bukkit.getWorld(empire.getWorldName());
        if (empireWorld != null) {
            try (MCTiming ignored = KingdomsPlugin.getInstance().getAndStartTiming("Empire world removal")) {
                for (Player player : empireWorld.getPlayers()) {
                    Utils.teleportToSpawn(player);
                }
                for (LockProvider lockProvider : KingdomsPlugin.getInstance().getLockProviders()) {
                    lockProvider.removeInWorld(empireWorld);
                }
                deleteWorld(empireWorld);
            }
        }
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteEmpire(empire);
            DatabaseManager.getInstance().saveVillages(empire.getMembers());
        });
    }

    private void deleteWorld(World empireWorld) {
        if (KingdomsPlugin.getInstance().getMultiverse() == null) {
            if (Bukkit.unloadWorld(empireWorld, true)) {
                try {
                    FileUtils.deleteDirectory(empireWorld.getWorldFolder());
                } catch (IOException e) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could delete the files of world " + empireWorld.getName(), e);
                }
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not unload and delete the world " + empireWorld.getName());
            }
        } else if (!KingdomsPlugin.getInstance().getMultiverse().deleteWorld(empireWorld)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Multiverse could not unload and delete the world " + empireWorld.getName());
        }
    }

    void abandonEmpire(Empire empire) {
        KingdomsPlugin.getInstance().addNewlyAbandoned(empire);

        // Remove members
        Set<Village> modifiedVillages = new HashSet<>();
        for (Village member : empire.getMembers()) {
            member.setEmpire(null);
            modifiedVillages.add(member);
        }
        empire.getMemberIds().clear();

        // Update ranks
        Set<Resident> modifiedResidents = new HashSet<>();
        for (Resident resident : empire.getResidents()) {
            if (resident.getEmpireRank() != EmpireRank.getDefault()) {
                resident.setEmpireRank(EmpireRank.getDefault());
                modifiedResidents.add(resident);
            }
        }
        Collection<LandOwner> allies = empire.getAllies();
        empire.getAlliedEmpireIds().clear();
        empire.getAlliedVillageIds().clear();
        for (LandOwner ally : allies) {
            ally.removeAlly(empire);
        }
        empire.setAccess(LandAccess.PUBLIC);
        for (Use u : Use.values()) {
            empire.setUseState(u, BuildState.PUBLIC);
        }
        for (PlotMode mode : PlotMode.values()) {
            if (mode.appliesTo(PlotMode.AppliesTo.LAND_OWNER)) {
                empire.setMode(mode, Utils.getPlotMode(mode, "abandoned." + empire.getStateKey()));
            }
        }
        World empireWorld = Bukkit.getWorld(empire.getWorldName());
        if (empireWorld != null) {
            for (LockProvider lockProvider : KingdomsPlugin.getInstance().getLockProviders()) {
                lockProvider.removeInWorld(empireWorld);
            }
        }
        LandManager.getInstance().removeWorld(empire.getWorldName());
        for (Perk perk : empire.getPerks()) {
            if (perk.hasEffects()) {
                PerkData perkData = empire.getPerkData(perk);
                if (perkData != null && perkData.isActive()) {
                    perkData.toggle();
                    empire.removeEffects(perk);
                }
            }
        }
        empire.getPerksMap().clear();

        empire.setCreated(System.currentTimeMillis());
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveEmpire(empire);
            DatabaseManager.getInstance().save(allies);
            DatabaseManager.getInstance().saveVillages(modifiedVillages);
            DatabaseManager.getInstance().saveResident(modifiedResidents);

            Utils.runLater(() -> {
                KingdomsPlugin.getInstance().getLogger().log(Level.INFO, empire.getStateTag() + " " + empire.getName() + " was removed because it has been abandoned for " + Config.Empire.Upkeep.ABANDON_TIME + " minutes!");
                deleteEmpire(empire);
            }, Config.Empire.Upkeep.ABANDON_TIME, TimeUnit.MINUTES);
        });
    }

    /**
     * Should not be used directly! Use {@link Empire#removeMember(Targetable)}!
     */
    void leaveEmpire(Village village) {
        Empire empire = village.getEmpire();
        if (empire == null) {
            return;
        }
        village.setEmpire(null);
        List<Resident> toSave = new ArrayList<>();
        for (Resident member : village.getResidents()) {
            if (member.getEmpireRank() != EmpireRank.getDefault()) {
                toSave.add(member);
                member.setEmpireRank(EmpireRank.getDefault());
            }
            member.removeChannel(ChatChannel.EMPIRE);
            Player player = member.getPlayer();
            if (player != null) {
                Empire e = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
                if (e != null && !e.canAccess(member)) {
                    Utils.teleportToSpawn(player);
                }
            }
        }
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(village);
            DatabaseManager.getInstance().saveResident(toSave);
        });
    }

    public Empire getEmpireFromWorld(File worldFolder) {
        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            return null;
        }
        return empireWorlds.computeIfAbsent(worldFolder.getName(), worldName -> {
            File idFile = new File(worldFolder, "empire.id");
            if (idFile.exists()) {
                try (BufferedReader in = new BufferedReader(new FileReader(idFile))) {
                    String idStr = in.readLine();
                    if (idStr != null) {
                        int id = Integer.parseInt(idStr);
                        Empire empire = getEmpireFromId(IdObject.get(id));
                        if (empire != null) {
                            return empire;
                        } else {
                            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "No world " + worldName + " specified empire with ID " + id + " but no empire with such an id was found?");
                        }
                    }
                } catch (IOException | NumberFormatException e) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to read empire id file from world folder " + worldName, e);
                }
            }
            return null;
        });
    }

    public Empire getEmpireFromWorld(World world) {
        if (world == null) {
            return null;
        }
        return getEmpireFromWorld(world.getWorldFolder());
    }

    public Empire getEmpireFromWorld(String worldName) {
        if (empireWorlds.containsKey(worldName)) {
            return empireWorlds.get(worldName);
        }
        return getEmpireFromWorld(new File(KingdomsPlugin.getInstance().getServer().getWorldContainer(), worldName));
    }

    public boolean changeName(Empire empire, String name) {
        String oldName = empire.getName();
        World world = Bukkit.getWorld(empire.getWorldName());
        empire.setName(name);

        if (Config.Empire.World.RENAME) {
            empire.setInRegen(true);
            empire.setWorldName(Config.Empire.World.PREFIX + empire.getName() + Config.Empire.World.SUFFIX);
            if (!renameWorld(empire, world)) {
                empire.setName(oldName);
                empire.setInRegen(false);
                return false;
            }
            empire.setInRegen(false);
        }

        empires.remove(oldName.replace(' ', '_').toLowerCase());
        empires.put(name.replace(' ', '_').toLowerCase(), empire);

        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        return true;
    }

    private boolean renameWorld(Empire empire, World world) {
        if (world == null) {
            return false;
        }

        String oldWorldName = world.getName();
        if (KingdomsPlugin.getInstance().getMultiverse() != null) {
            World newWorld = KingdomsPlugin.getInstance().getMultiverse().renameWorld(world, empire.getWorldName());
            if (newWorld == null) {
                return false;
            }
            if (empire.getHome() == null) {
                empire.setHome(new SimpleLocation(empire.getWorldName(), 0, world.getHighestBlockYAt(0, 0), 0));
            } else {
                empire.getHome().setWorld(empire.getWorldName());
            }
            newWorld.setSpawnLocation(empire.getHome().toBukkitLocation());
        } else {
            Map<Player, Location> positions = new LinkedHashMap<>();
            for (Player player : world.getPlayers()) {
                positions.put(player, player.getLocation());
                Utils.teleportToSpawn(player);
            }
            File worldFolder = world.getWorldFolder();
            File newFolder = new File(Bukkit.getWorldContainer(), empire.getWorldName());
            if (newFolder.exists()) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "World with name " + empire.getWorldName() + " already exists?");
                return false;
            }
            Bukkit.unloadWorld(world, true);
            try {
                FileUtils.moveDirectory(worldFolder, newFolder);
            } catch (IOException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while copying world " + worldFolder.getName() + " to " + empire.getWorldName() + "!", e);
                return false;
            }

            World newWorld = createEmpireWorld(empire);

            for (Map.Entry<Player, Location> e : positions.entrySet()) {
                if (e.getKey().isOnline()) {
                    e.getValue().setWorld(newWorld);
                    PaperLib.teleportAsync(e.getKey(), e.getValue());
                }
            }
        }
        LandManager.getInstance().renameWorld(oldWorldName, empire.getWorldName());
        KingdomsPlugin.getInstance().getLockProviders().forEach(lp -> lp.renameWorld(oldWorldName, empire.getWorldName()));
        return true;
    }

    public boolean regenWorld(Empire empire) {
        World world = Bukkit.getWorld(empire.getWorldName());
        empire.setInRegen(true);
        deleteWorld(world);
        createEmpireWorld(empire);
        empire.setInRegen(false);
        return false;
    }
}

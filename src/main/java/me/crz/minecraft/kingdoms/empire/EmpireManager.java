package me.crz.minecraft.kingdoms.empire;

import co.aikar.timings.Timing;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.lock.LockProvider;
import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class EmpireManager {
    private static EmpireManager instance;
    private final Map<String, Empire> empires = new HashMap<>();
    private final Map<IdObject, Empire> empireIds = new HashMap<>();

    private Map<String, EmpirePerk> perks;
    private Map<String, EmpirePerk> perkTags;

    private Map<String, EmpireRank> ranks = new LinkedHashMap<>();

    private EmpireRank leaderRank = null;
    private EmpireRank defaultRank = null;

    public EmpireManager() {
        super();
        loadPerks();
        loadRanks();
        for (Empire empire : DatabaseManager.getInstance().getAllEmpires()) {
            empires.put(empire.getName().replace(' ', '_').toLowerCase(), empire);
            empireIds.put(empire.getId(), empire);
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

    private void loadRanks() {
        Configuration config = ConfigManager.getInstance().getSettlementRanksConf().getConfig();
        for (String rankName : config.getKeys(false)) {
            ranks.put(rankName.toLowerCase(), new EmpireRank(rankName, config.getConfigurationSection(rankName)));
        }
        EmpireRank lastRank = null;
        for (EmpireRank rank : ranks.values()) {
            if (leaderRank == null) {
                leaderRank = rank;
            }
            if (lastRank != null) {
                lastRank.setInherits(rank.name());
                rank.setParent(lastRank.name());
                rank.setOrdinal(lastRank.ordinal() + 1);
            } else {
                rank.setOrdinal(0);
            }
            lastRank = rank;
        }
        defaultRank = lastRank;
    }

    public void loadEmpireWorlds() {
        try (Timing ignored = KingdomsPlugin.getInstance().getAndStartTiming("Empire world creation")) {
            for (Empire empire : getEmpires()) {
                createEmpireWorld(empire);
            }
        }
    }

    public static EmpireManager getInstance() {
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

    public Collection<EmpireRank> getRanks() {
        return ranks.values();
    }

    public EmpireRank getRank(String name) {
        return name != null ? ranks.get(name.toLowerCase()) : null;
    }

    public EmpireRank getLeaderRank() {
        return leaderRank;
    }

    public EmpireRank getDefaultRank() {
        return defaultRank;
    }

    public void registerNewEmpire(Empire empire) {
        if (!empire.getId().isNull()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving empire " + empire.getName() + " to the database! It already had an ID defined?");
            return;
        }
        empire.getCapital().setEmpire(empire);
        empire.getCapital().getChief().setEmpireRank(getLeaderRank());
        empires.put(empire.getName().replace(' ', '_').toLowerCase(), empire);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveEmpire(empire);
            DatabaseManager.getInstance().saveVillage(empire.getCapital());
            DatabaseManager.getInstance().saveResident(empire.getCapital().getChief());
            if (!empire.getId().isNull()) {
                empireIds.put(empire.getId(), empire);
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving empire " + empire.getName() + " to the database! No ID was returned.");
            }
        });
        try (Timing ignored = KingdomsPlugin.getInstance().getAndStartTiming("Empire world creation")) {
            createEmpireWorld(empire);
            Message.get("empire-world-generated").tell(empire.getOnlineResidents());
        }
    }

    private World createEmpireWorld(Empire empire) {
        String worldName = empire.getWorldName();
        World world = Bukkit.getWorld(worldName);
        ConfigurationSection worldConfig = KingdomsPlugin.getInstance().getConfig().getConfigurationSection("empires-world");
        if (world == null) {
            WorldCreator worldCreator = new WorldCreator(worldName);
            if (worldConfig.isString("generator")) {
                worldCreator.generator(worldConfig.getString("generator"));
                if (worldConfig.isString("generator-settings")) {
                    worldCreator.generatorSettings(worldConfig.getString("generator-settings"));
                }
            }
            if (worldConfig.isSet("seed")) {
                if (worldConfig.isLong("seed")) {
                    worldCreator.seed(worldConfig.getLong("seed"));
                } else {
                    worldCreator.seed(worldConfig.getString("seed").hashCode());
                }
            }
            if (worldConfig.isBoolean("structures")) {
                worldCreator.generateStructures(worldConfig.getBoolean("structures"));
            }
            if (worldConfig.isString("environment")) {
                try {
                    worldCreator.environment(World.Environment.valueOf(worldConfig.getString("environment").toUpperCase()));
                } catch (IllegalArgumentException e) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, worldConfig.getString("environment") + " is not a valid Environment for the empires world!");
                }
            }
            if (worldConfig.isString("type")) {
                try {
                    worldCreator.type(WorldType.valueOf(worldConfig.getString("type").toUpperCase()));
                } catch (IllegalArgumentException e) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, worldConfig.getString("type") + " is not a valid type for the empires world!");
                }
            }
            world = Bukkit.createWorld(worldCreator);
        }
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
        MessageUtils.broadcast(Message.get("broadcast.empire-deletion"), "name", empire.getName());
        for (Village member : empire.getMembers()) {
            if (empire.equals(member.getEmpire())) {
                member.setEmpire(null);
            } else {
                empire.getMemberIds().remove(member.getId());
            }
        }
        World empireWorld = Bukkit.getWorld(empire.getWorldName());
        if (empireWorld != null) {
            try (Timing ignored = KingdomsPlugin.getInstance().getAndStartTiming("Empire world removal")) {
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
                if (!empireWorld.getWorldFolder().delete()) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could delete the files of world " + empireWorld.getName());
                }
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not unload and delete the world " + empireWorld.getName());
            }
        } else if (!KingdomsPlugin.getInstance().getMultiverse().deleteWorld(empireWorld)) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Multiverse could not unload and delete the world " + empireWorld.getName());
        }
    }

    public void abandonEmpire(Empire empire) {
        MessageUtils.broadcast(Message.get("broadcast.empire-abandoned"), "name", empire.getName());

        // Remove members
        Set<Village> modifiedVillages = new HashSet<>();
        for (Village member : empire.getMembers()) {
            member.setEmpire(null);
            modifiedVillages.add(member);
        }
        empire.getMemberIds().clear();

        // Update ranks
        Set<Resident> modifiedResidents = new HashSet<>();
        empire.getCapital().getChief().setEmpireRank(getDefaultRank());
        modifiedResidents.add(empire.getCapital().getChief());
        for (Resident senator : empire.getSenators()) {
            senator.setEmpireRank(EmpireRank.MEMBER);
            empire.removeSenator(senator);
            modifiedResidents.add(senator);
        }
        empire.getSenatorIds().clear();
        empire.setAccess(LandAccess.PUBLIC);
        World empireWorld = Bukkit.getWorld(empire.getWorldName());
        if (empireWorld != null) {
            for (LockProvider lockProvider : KingdomsPlugin.getInstance().getLockProviders()) {
                lockProvider.removeInWorld(empireWorld);
            }
        }
        LandManager.getInstance().removeWorld(empire.getWorldName());
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveEmpire(empire);
            DatabaseManager.getInstance().saveVillages(modifiedVillages);
            DatabaseManager.getInstance().saveResident(modifiedResidents);
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
            if (member.getEmpireRank().above(getDefaultRank())) {
                toSave.add(member);
                member.setEmpireRank(getDefaultRank());
                empire.removeSenator(member);
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

    public Empire getEmpireFromWorld(World world) {
        if (world == null) {
            return null;
        }
        return getEmpireFromWorld(world.getName());
    }

    public Empire getEmpireFromWorld(String worldName) {
        if (worldName.startsWith(KingdomsPlugin.getInstance().getEmpiresWorldPrefix())
                && worldName.endsWith(KingdomsPlugin.getInstance().getEmpiresWorldSuffix())) {
            return getEmpireFromName(worldName.substring(
                    KingdomsPlugin.getInstance().getEmpiresWorldPrefix().length(),
                    worldName.length() - KingdomsPlugin.getInstance().getEmpiresWorldSuffix().length()
            ));
        }
        return null;
    }

    public boolean changeName(Empire empire, String name) {
        String oldName = empire.getName();
        World world = Bukkit.getWorld(empire.getWorldName());
        empire.setName(name);

        empire.setInRegen(true);
        if (!renameWorld(empire, world)) {
            empire.setName(oldName);
            empire.setInRegen(false);
            return false;
        }
        empire.setInRegen(false);

        empires.remove(oldName.replace(' ', '_').toLowerCase());
        empires.put(name.replace(' ', '_').toLowerCase(), empire);

        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        return true;
    }

    private boolean renameWorld(Empire empire, World world) {
        if (world == null) {
            return false;
        }

        if (KingdomsPlugin.getInstance().getMultiverse() != null) {
            if (!KingdomsPlugin.getInstance().getMultiverse().renameWorld(world, empire.getWorldName())) {
                return false;
            }
            if (empire.getHome() == null) {
                empire.setHome(new SimpleLocation(empire.getWorldName(), 0, world.getHighestBlockYAt(0, 0), 0));
            } else {
                empire.getHome().setWorld(empire.getWorldName());
            }
            world.setSpawnLocation(empire.getHome().toBukkitLocation());
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
                    e.getKey().teleport(e.getValue());
                }
            }
        }
        LandManager.getInstance().renameWorld(world.getName(), empire.getWorldName());
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

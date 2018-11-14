package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class EmpireManager {
    private static EmpireManager instance;
    private final Map<String, Empire> empires = new HashMap<>();
    private final Map<IdObject, Empire> empireIds = new HashMap<>();

    private Map<String, EmpirePerk> perks;
    private Map<String, EmpirePerk> perkTags;

    public EmpireManager() {
        super();
        loadPerks();
        for (Empire empire : DatabaseManager.getInstance().getAllEmpires()) {
            empires.put(empire.getName().replace(' ', '_').toLowerCase(), empire);
            empireIds.put(empire.getId(), empire);
            createEmpireWorld(empire);
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

    public void registerNewEmpire(Empire empire) {
        if (!empire.getId().isNull()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving empire " + empire.getName() + " to the database! It already had an ID defined?");
            return;
        }
        createEmpireWorld(empire);

        empires.put(empire.getName().replace(' ', '_').toLowerCase(), empire);

        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveEmpire(empire);
            if (!empire.getId().isNull()) {
                empireIds.put(empire.getId(), empire);
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving empire " + empire.getName() + " to the database! No ID was returned.");
            }
        });
    }

    private void createEmpireWorld(Empire empire) {
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
        }
        world.setSpawnLocation(empire.getHome().toBukkitLocation());

        Message.get("empire-world-generated").tell(empire.getOnlineResidents());
    }

    public Collection<EmpirePerk> getPerks() {
        return perks.values();
    }

    public void deleteEmpire(Empire empire) {
        empires.remove(empire.getName().toLowerCase());
        empireIds.remove(empire.getId());
        MessageUtils.broadcast(Message.get("broadcast.empire-deletion"), "name", empire.getName());
        for (Village member : empire.getMembers()) {
            member.setEmpire(null);
        }
        World empireWorld = Bukkit.getWorld(empire.getWorldName());
        if (empireWorld != null) {
            for (Player player : empireWorld.getPlayers()) {
                Utils.teleportToSpawn(player);
            }
            if (Bukkit.unloadWorld(empireWorld, true)) {
                empireWorld.getWorldFolder().delete();
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not unload and delete the world of empire " + empire.getName());
            }
        }
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteEmpire(empire);
            DatabaseManager.getInstance().saveVillages(empire.getMembers());
        });
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
        empire.getCapital().getChief().setEmpireRank(EmpireRank.NONE);
        modifiedResidents.add(empire.getCapital().getChief());
        for (Resident senator : empire.getSenators()) {
            senator.setEmpireRank(EmpireRank.NONE);
            modifiedResidents.add(senator);
        }
        empire.getSenatorIds().clear();
        empire.setAccess(LandAccess.PUBLIC);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveEmpire(empire);
            DatabaseManager.getInstance().saveVillages(modifiedVillages);
            DatabaseManager.getInstance().saveResident(modifiedResidents);
        });
    }

    public void leaveEmpire(Village village) {
        village.setEmpire(null);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
    }

    public Empire getEmpireFromWorld(World world) {
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

    public void changeName(Empire empire, String name) {
        empires.remove(empire.getName().replace(' ', '_').toLowerCase());
        empire.setName(name);
        empires.put(empire.getName().replace(' ', '_').toLowerCase(), empire);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
    }
}

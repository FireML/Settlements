package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class EmpireManager {
    private static EmpireManager instance;
    private final Map<String, Empire> empires = new HashMap<>();
    private final Map<IdObject, Empire> empireIds = new HashMap<>();

    private Map<String, Perk> perks;
    private Map<String, Perk> perkTags;

    public EmpireManager() {
        super();
        loadPerks();
        for (Empire empire : DatabaseManager.getInstance().getAllEmpires()) {
            empires.put(empire.getName().toLowerCase(), empire);
            empireIds.put(empire.getId(), empire);
        }
    }

    public void loadPerks() {
        perks = new LinkedHashMap<>();
        perkTags = new LinkedHashMap<>();
        for (String perkName : ConfigManager.getInstance().getPerksConfig().getKeys(false)) {
            try {
                Perk perk = new Perk(ConfigManager.getInstance().getEmpirePerksConfig().getConfigurationSection(perkName));
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
        return empires.values();
    }

    public Empire getEmpireFromName(String name) {
        return empires.get(name.toLowerCase());
    }

    public Empire getEmpireFromId(IdObject id) {
        return empireIds.get(id);
    }

    public Perk getPerk(String name) {
        Perk perk = perks.get(name.toLowerCase());
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
        empires.put(empire.getName().toLowerCase(), empire);

        createEmpireWorld(empire);

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
        if (worldConfig.isInt("border-radius")) {
            world.getWorldBorder().setCenter(0, 0);
            world.getWorldBorder().setSize(worldConfig.getInt("border-radius") * 2);
        }
        world.setSpawnLocation(0, world.getHighestBlockYAt(0, 0), 0);
        empire.setHome(world.getSpawnLocation());

        Message.get("empire-world-generated").tell(empire.getOnlineResidents());
    }

    /**
     * Check whether or not a empire can level up
     * @param empire The Empire to check
     * @param res
     * @return Whether or not the empire was leveled up
     */
    public boolean checkLevelUp(Empire empire, Resident res) {
        if (!empire.canLevelUp()) {
            return false;
        }

        empire.setLevel(empire.getLevel() + 1);
        MessageUtils.tellResidents(empire.getOnlineResidents(), Message.get("levels.upgrade"), "level", Integer.toString(empire.getLevel()), "state", "Empire");
        removeLevelRequirements(empire, res);

        for (Perk perk : getPerks()) {
            if (empire.getLevel() == perk.getLevel()) {
                if (perk.getUnlockCost().isEmpty()) {
                    MessageUtils.tellResidents(empire.getOnlineResidents(), Message.get("levels.perk-unlocked"), "perk", perk.getTag());
                    empire.unlockPerk(perk);
                } else {
                    MessageUtils.tellResidents(empire.getOnlineResidents(), Message.get("levels.perk-available"), "perk", perk.getTag());
                }
            }
        }

        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        return true;
    }

    private void removeLevelRequirements(Empire empire, Resident res) {
        Map<String, Long> levelReqs = Levels.getEmpireLevelRequirements(empire.getLevel());
        if (empire.remove(levelReqs)) {
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveEmpire(empire);
                DatabaseManager.getInstance().logLevelup(empire, res, levelReqs);
            });
        }
    }


    public Collection<Perk> getPerks() {
        return perks.values();
    }

    public void deleteEmpire(Empire empire) {
        empires.remove(empire.getName().toLowerCase());
        empireIds.remove(empire.getId());
        MessageUtils.broadcast(Message.get("broadcast.empire-deletion"), "name", empire.getName());
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteEmpire(empire);
            for (Village member : empire.getMembers()) {
                member.setEmpire(null);
            }
        });
    }

    public void leaveEmpire(Village village) {
        village.setEmpire(null);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(village);
        });
    }

    public Empire getEmpireFromWorld(World world) {
        if (world.getName().startsWith(KingdomsPlugin.getInstance().getEmpiresWorldPrefix())
                && world.getName().endsWith(KingdomsPlugin.getInstance().getEmpiresWorldSuffix())) {
            return getEmpireFromName(world.getName().substring(
                    KingdomsPlugin.getInstance().getEmpiresWorldPrefix().length(),
                    world.getName().length() - KingdomsPlugin.getInstance().getEmpiresWorldSuffix().length()
            ));
        }
        return null;
    }

    public void changeName(Empire empire, String name) {
        empires.remove(empire.getName().toLowerCase());
        empire.setName(name);
        empires.put(empire.getName().toLowerCase(), empire);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
    }
}

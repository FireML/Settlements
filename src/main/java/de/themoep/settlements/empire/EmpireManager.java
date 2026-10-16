package de.themoep.settlements.empire;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.Use;
import de.themoep.settlements.land.events.LandownerAbandonedEvent;
import de.themoep.settlements.land.events.LandownerCreatedEvent;
import de.themoep.settlements.land.events.LandownerDisbandEvent;
import de.themoep.settlements.land.events.LandownerLeveledEvent;
import de.themoep.settlements.perk.EmpirePerk;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.world.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EmpireManager {
    private static EmpireManager instance;
    private final Map<String, Empire> empires = new ConcurrentHashMap<>();
    private final Map<IdObject, Empire> empireIds = new ConcurrentHashMap<>();
    private final Map<UUID, Empire> empireUuids = new ConcurrentHashMap<>();
    private final Map<String, Empire> empireWorlds = new ConcurrentHashMap<>();

    private List<EmpireLevel> levels = new ArrayList<>();

    private Map<String, EmpireMission> missions = new HashMap<>();

    private Map<String, EmpirePerk> perks;
    private Map<String, EmpirePerk> perkTags;
    private Set<String> unlockables;

    private void setup() {
        loadConfig();
        for (Empire empire : DatabaseManager.getInstance().getAllEmpires()) {
            if (Config.Empire.World.ENABLED && Config.Empire.World.RENAME && empire.getWorldName() != null) {
                World world = SettlementsPlugin.getInstance().getServer().getWorld(empire.getWorldName());
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
                SettlementsPlugin.logDebug(empire.getStateTag() + " " + empire.getName() + " will be removed after " + Config.Empire.Upkeep.ABANDON_TIME + " minutes in " + delay + "!");
                Utils.runLater(() -> {
                    SettlementsPlugin.getInstance().getLogging().info(empire.getStateTag() + " " + empire.getName() + " was removed because it has been abandoned for " + Config.Empire.Upkeep.ABANDON_TIME + " minutes!");
                    deleteEmpire(empire);
                    ComponentUtils.broadcastMessage(Message.get("broadcast.empire-deletion").getReplacedComponent("name", empire.getName()));
                }, delay, TimeUnit.MILLISECONDS);
            }
        }

        if (Config.Empire.World.ENABLED) {
            // Loop through all worlds to find empire ones
            for (File world : SettlementsPlugin.getInstance().getServer().getWorldContainer().listFiles(File::isDirectory)) {
                Empire empire = getEmpireFromWorld(world);
                if (empire != null && getLevel(empire.getLevel()).map(EmpireLevel::getWorldRadius).orElse(0) > 0) {
                    empire.setWorldName(world.getName());
                }
            }
        }
        SettlementsPlugin.logDebug("Loaded EmpireManager with " + empireIds.size() + " empires, " + levels.size() + " levels, " + missions.size() + " missions and " + perks.size() + " perks");
    }

    public void postLoad() {
        for (Empire empire : getEmpires()) {
            if (!empire.isAbandoned()) {
                for (Settlement member : empire.getMembers()) {
                    if (member.getMissionData().isEmpty()) {
                        empire.distributeNewMissions(member);
                    }
                }
                for (EmpirePerk perk : getPerks()) {
                    if (perk.matchesLevelRequirement(empire) && perk.getUnlockCost().isEmpty()) {
                        empire.unlockPerk(empire.getChief(), perk);
                    }
                }
            }
        }
    }

    public void loadConfig() {
        loadLevels();
        loadPerks();
        loadMissions();
    }

    private void loadLevels() {
        levels.clear();
        EmpireLevel lastLevel = null;
        for (int i = 1; i <= Config.Empire.Leveling.MAX_LEVEL; i++) {
            EmpireLevel level = new EmpireLevel(i, lastLevel, ConfigManager.getInstance().getEmpireLevelConfig().getConfigurationSection("levels.level-" + i));
            lastLevel = level;
            levels.add(level);
            SettlementsPlugin.logDebug("Loaded " + level);
        }
    }

    private void writeIdFile(World world, Empire empire) {
        empireWorlds.put(world.getWorldFolder().getName(), empire);
        File idFile = new File(world.getWorldFolder(), "empire.id");
        if (!idFile.exists()) {
            try (FileWriter writer = new FileWriter(idFile)) {
                writer.write(String.valueOf(empire.getId().value()));
            } catch (IOException e) {
                SettlementsPlugin.getInstance().getLogging().error("Error while trying to write empire id into world folder " + world.getName());
                SettlementsPlugin.getInstance().getLogging().exception(e);
            }
        }
    }

    private void loadPerks() {
        perks = new LinkedHashMap<>();
        perkTags = new LinkedHashMap<>();
        unlockables = new HashSet<>();
        for (String perkName : ConfigManager.getInstance().getEmpirePerksConfig().getKeys(false)) {
            try {
                EmpirePerk perk = new EmpirePerk(ConfigManager.getInstance().getEmpirePerksConfig().getConfigurationSection(perkName));
                perks.put(perk.name().toLowerCase(), perk);
                perkTags.put(perk.getTag().toLowerCase(), perk);
                unlockables.addAll(perk.getUnlockables());
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.getInstance().getLogging().error("Config for Empire Perk " + perkName + " was invalid! " + e.getMessage());
            }
        }
    }

    private void loadMissions() {
        missions.clear();
        for (String missionId : ConfigManager.getInstance().getEmpireMissionsConfig().getKeys(false)) {
            try {
                missions.put(missionId.toLowerCase(Locale.ROOT), new EmpireMission(ConfigManager.getInstance().getEmpireMissionsConfig().getConfigurationSection(missionId)));
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.getInstance().getLogging().error("Config for Empire Mission " + missionId + " was invalid! " + e.getMessage());
            }
        }
    }

    public void loadEmpireWorlds() {
        for (Empire empire : getEmpires()) {
            createEmpireWorld(empire);
        }
    }

    public static EmpireManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new EmpireManager();
            instance.setup();
        }
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
        Settlement settlement = SettlementManager.getInstance().getFromPlayer(player);
        return settlement != null ? settlement.getEmpire() : null;
    }

    public Optional<EmpireLevel> getLevel(int level) {
        if (level <= levels.size()) {
            return Optional.of(levels.get(level - 1));
        }
        return Optional.empty();
    }

    public EmpireMission getMission(String id) {
        return missions.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<EmpireMission> getMissions() {
        return missions.values();
    }

    public void finishMission(EmpireMission mission, Resident resident) {
        if (!resident.hasEmpire()) {
            // Wat
            return;
        }
        Settlement settlement = resident.getSettlement();
        Empire empire = settlement.getEmpire();
        settlement.setMissionPoints(settlement.getMissionPoints() + 1);
        empire.setFinishedMissions(empire.getFinishedMissions() + 1);
        Message.get("missions.completed").tell(empire.getOnlineResidents(),
                "state", settlement.getStateTag(),
                "name", settlement.getName(),
                "mission", mission.getTag(),
                "missionpoints", String.valueOf(settlement.getMissionPoints()),
                "finishedmissions", String.valueOf(empire.getFinishedMissions()),
                "player", resident.getName()
        );
        SettlementsPlugin.logDebug(settlement.getStateTag() + " " + settlement.getName() + " finished mission " + mission.getTag() + " for empire " + empire.getName());
        settlement.checkLevelUp(resident);
        empire.checkLevelUp(resident);
        settlement.save();
        empire.save();
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

    public void registerNewEmpire(Resident resident, Empire empire) {
        if (!empire.getId().isNull()) {
            SettlementsPlugin.getInstance().getLogging().warn("Error while saving empire " + empire.getName() + " to the database! It already had an ID defined?");
            return;
        }
        empire.setBank(Config.Empire.START_BALANCE);
        empire.getCapital().setEmpire(empire);
        empire.getCapital().getChief().setEmpireRank(EmpireRank.getLeader());
        empires.put(empire.getName().replace(' ', '_').toLowerCase(), empire);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().addNewEmpire(empire);
            DatabaseManager.getInstance().saveSettlement(empire.getCapital());
            DatabaseManager.getInstance().saveResident(empire.getCapital().getChief());
            if (!empire.getId().isNull()) {
                empireIds.put(empire.getId(), empire);
                empireUuids.put(empire.getUUID(), empire);
                if (Config.Empire.World.ENABLED && empire.getLevelInfo().getWorldRadius() > 0) {
                    if (Config.Empire.World.RENAME) {
                        empire.setWorldName(Config.Empire.World.PREFIX + empire.getName() + Config.Empire.World.SUFFIX);
                    } else {
                        empire.setWorldName(Config.Empire.World.PREFIX + "id" + empire.getId().value() + Config.Empire.World.SUFFIX);
                    }
                    Utils.runSync(() -> {
                        createEmpireWorld(empire);
                        Message.get("empire-world-generated").tell(empire.getOnlineResidents());
                    });
                }
                Utils.runSync(() -> new LandownerCreatedEvent(empire, resident).callEvent());
            } else {
                SettlementsPlugin.getInstance().getLogging().warn("Error while saving empire " + empire.getName() + " to the database! No ID was returned.");
            }
        });
    }

    World createEmpireWorld(Empire empire) {
        if (!Config.Empire.World.ENABLED || !empire.hasWorld()) {
            return null;
        }
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
        // check if world spawn is valid (inside world border, above min-y and under max-y)
        if (!world.getWorldBorder().isInside(world.getSpawnLocation())
                || world.getSpawnLocation().getY() <= world.getMinHeight()
                || world.getSpawnLocation().getY() > world.getMinHeight()) {
            // if not set to highest at 0 0
            world.setSpawnLocation(new Location(world, 0, world.getHighestBlockYAt(0, 0), 0));
        }
        return world;
    }

    public void deleteEmpire(Empire empire) {
        new LandownerDisbandEvent(empire).callEvent();
        empires.remove(empire.getName().toLowerCase());
        empireIds.remove(empire.getId());
        empireUuids.remove(empire.getUUID());
        if (empire.hasWorld()) {
            empireWorlds.remove(empire.getWorldName());
        }
        SettlementsPlugin.getInstance().addNewlyDeleted(empire);
        for (Settlement member : empire.getMembers()) {
            member.setEmpireDisband(System.currentTimeMillis());
            if (empire.equals(member.getEmpire())) {
                member.setEmpire(null);
            } else {
                empire.getMemberIds().remove(member.getId());
            }
            member.getMissionData().clear();
        }
        if (Config.Empire.World.ENABLED && empire.hasWorld()) {
            World empireWorld = Bukkit.getWorld(empire.getWorldName());
            if (empireWorld != null) {
                for (Player player : empireWorld.getPlayers()) {
                    Utils.teleportToSpawn(player);
                }
                deleteWorld(empireWorld);
            }
        }
        LandManager.getInstance().unclaimLand(empire);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteEmpire(empire);
            DatabaseManager.getInstance().saveSettlementMissionData(empire.getMembers());
            DatabaseManager.getInstance().saveSettlements(empire.getMembers());
        });
    }

    private void deleteWorld(World empireWorld) {
        if (Bukkit.unloadWorld(empireWorld, true)) {
            try {
                FileUtils.deleteDirectory(empireWorld.getWorldFolder());
            } catch (IOException e) {
                SettlementsPlugin.getInstance().getLogging().error("Could delete the files of world " + empireWorld.getName());
                SettlementsPlugin.getInstance().getLogging().exception(e);
            }
        } else {
            SettlementsPlugin.getInstance().getLogging().error("Could not unload and delete the world " + empireWorld.getName());
        }
    }

    void abandonEmpire(Empire empire) {
        SettlementsPlugin.getInstance().addNewlyAbandoned(empire);

        // Remove members
        Set<Settlement> modifiedSettlements = new HashSet<>();
        for (Settlement member : empire.getMembers()) {
            member.setEmpire(null);
            member.getMissionData().clear();
            modifiedSettlements.add(member);
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
        Collection<Ally> allies = empire.getAllies();
        empire.getAllies().clear();
        for (Ally ally : allies) {
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
        if (empire.hasWorld()) {
            LandManager.getInstance().removeWorld(empire.getWorldName());
        }
        for (Perk perk : empire.getPerks()) {
            if (perk.hasEffects()) {
                PerkData perkData = empire.getPerkData(perk);
                if (perkData != null && perkData.isActive()) {
                    perkData.toggle();
                    empire.removeEffects(null, perk);
                }
            }
        }
        empire.getPerksMap().clear();

        empire.setCreated(System.currentTimeMillis());

        new LandownerAbandonedEvent(empire).callEvent();

        DatabaseManager.getInstance().saveEmpire(empire);
        DatabaseManager.getInstance().saveAllies(allies);
        DatabaseManager.getInstance().saveSettlementMissionData(modifiedSettlements);
        DatabaseManager.getInstance().saveSettlements(modifiedSettlements);
        DatabaseManager.getInstance().saveResidents(modifiedResidents);

        Utils.runLater(() -> {
            SettlementsPlugin.getInstance().getLogging().info(empire.getStateTag() + " " + empire.getName() + " was removed because it has been abandoned for " + Config.Empire.Upkeep.ABANDON_TIME + " minutes!");
            deleteEmpire(empire);
        }, Config.Empire.Upkeep.ABANDON_TIME, TimeUnit.MINUTES);
    }

    /**
     * Should not be used directly! Use {@link Empire#removeMember(Targetable)}!
     */
    void leaveEmpire(Settlement settlement) {
        Empire empire = settlement.getEmpire();
        if (empire == null) {
            return;
        }
        settlement.setEmpire(null);
        List<Resident> toSave = new ArrayList<>();
        for (Resident member : settlement.getResidents()) {
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
        settlement.getMissionData().clear();
        DatabaseManager.getInstance().saveMissionData(settlement);
        DatabaseManager.getInstance().saveSettlement(settlement);
        DatabaseManager.getInstance().saveResidents(toSave);
    }

    /**
     * Check whether or not an empire can level up
     * @param empire The Empire to check
     * @param res
     * @return Whether or not the empire was leveled up
     */
    public boolean checkLevelUp(Empire empire, Resident res) {
        if (!empire.canLevelUp()) {
            return false;
        }

        int oldRadius = empire.getWorldRadius();
        empire.setLevel(empire.getLevel() + 1);
        MessageUtils.tellResidents(empire.getOnlineResidents(), Message.get("levels.upgrade").getReplacedComponent("level", Integer.toString(empire.getLevel()), "state", empire.getStateTag()));
        removeLevelRequirements(empire, res);
        return onLevelUp(empire, res, oldRadius);
    }

    public boolean onLevelUp(Empire empire, Resident res, int oldRadius) {
        List<String> perksDesc = new ArrayList<>();
        for (EmpirePerk perk : getPerks()) {
            if (empire.getLevel() == perk.getLevel()) {
                if (perk.getUnlockCost().isEmpty()) {
                    Message.get("levels.perk-unlocked").tell(empire, "perk", perk.getTag());
                    empire.unlockPerk(res, perk);
                } else {
                    Message.get("levels.perk-available").tell(empire, "perk", perk.getTag());
                }
                perksDesc.add(perk.getTag());
            }
        }

        if (Config.Empire.World.ENABLED) {
            if (oldRadius == 0 && !empire.hasWorld() && empire.getWorldRadius() > 0) {
                if (Config.Empire.World.RENAME) {
                    empire.setWorldName(Config.Empire.World.PREFIX + empire.getName() + Config.Empire.World.SUFFIX);
                } else {
                    empire.setWorldName(Config.Empire.World.PREFIX + "id" + empire.getId().value() + Config.Empire.World.SUFFIX);
                }
                Utils.runSync(() -> {
                    createEmpireWorld(empire);
                    Message.get("empire-world-generated").tell(empire.getOnlineResidents());
                });
            } else if (empire.hasWorld()) {
                World world = Bukkit.getWorld(empire.getWorldName());
                if (world != null) {
                    world.getWorldBorder().changeSize(empire.getWorldRadius() * 2, 5);
                }
            }
        }

        new LandownerLeveledEvent(empire, res).callEvent();
        empire.save();
        return true;
    }

    public void removeLevelRequirements(Empire empire, Resident res) {
        Map<String, Long> levelReqs = getLevel(empire.getLevel())
                .map(EmpireLevel::getRequirements)
                .orElse(Collections.emptyMap());
        if (empire.remove(levelReqs)) {
            empire.save();
            DatabaseManager.getInstance().logLevelup(empire, res, levelReqs);
        }
    }

    public Empire getEmpireFromWorld(File worldFolder) {
        if (!worldFolder.exists() || !worldFolder.isDirectory() || !Config.Empire.World.ENABLED) {
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
                        if (empire != null && empire.hasWorld()) {
                            return empire;
                        } else {
                            SettlementsPlugin.getInstance().getLogging().warn("No world " + worldName + " specified empire with ID " + id + " but no empire with such an id was found?");
                        }
                    }
                } catch (IOException | NumberFormatException e) {
                    SettlementsPlugin.getInstance().getLogging().error("Error while trying to read empire id file from world folder " + worldName);
                    SettlementsPlugin.getInstance().getLogging().exception(e);
                }
            }
            return null;
        });
    }

    public Empire getEmpireFromWorld(World world) {
        if (world == null || !Config.Empire.World.ENABLED) {
            return null;
        }
        return getEmpireFromWorld(world.getWorldFolder());
    }

    public Empire getEmpireFromWorld(String worldName) {
        if (!Config.Empire.World.ENABLED) {
            return null;
        }
        if (empireWorlds.containsKey(worldName)) {
            return empireWorlds.get(worldName);
        }
        return getEmpireFromWorld(new File(SettlementsPlugin.getInstance().getServer().getWorldContainer(), worldName));
    }

    public boolean changeName(Empire empire, String name) {
        String oldName = empire.getName();
        empire.setName(name);

        if (Config.Empire.World.ENABLED && Config.Empire.World.RENAME && empire.hasWorld()) {
            World world = Bukkit.getWorld(empire.getWorldName());
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

        DatabaseManager.getInstance().saveEmpire(empire);
        return true;
    }

    private boolean renameWorld(Empire empire, World world) {
        if (world == null || !Config.Empire.World.ENABLED || !empire.hasWorld()) {
            return false;
        }

        String oldWorldName = world.getName();
        Map<Player, Location> positions = new LinkedHashMap<>();
        for (Player player : world.getPlayers()) {
            positions.put(player, player.getLocation());
            Utils.teleportToSpawn(player);
        }
        File worldFolder = world.getWorldFolder();
        File newFolder = new File(Bukkit.getWorldContainer(), empire.getWorldName());
        if (newFolder.exists()) {
            SettlementsPlugin.getInstance().getLogging().error("World with name " + empire.getWorldName() + " already exists?");
            return false;
        }
        Bukkit.unloadWorld(world, true);
        try {
            FileUtils.copyDirectory(worldFolder, newFolder);
            FileUtils.deleteDirectory(worldFolder);
        } catch (IOException e) {
            SettlementsPlugin.getInstance().getLogging().error("Error while copying world " + worldFolder.getName() + " to " + empire.getWorldName() + "!");
            SettlementsPlugin.getInstance().getLogging().exception(e);
            return false;
        }

        World newWorld = createEmpireWorld(empire);

        for (Map.Entry<Player, Location> e : positions.entrySet()) {
            if (e.getKey().isOnline()) {
                e.getValue().setWorld(newWorld);
                e.getKey().teleportAsync(e.getValue());
            }
        }
        LandManager.getInstance().renameWorld(oldWorldName, empire.getWorldName());
        return true;
    }

    public boolean regenWorld(Empire empire) {
        if (Config.Empire.World.ENABLED && empire.hasWorld()) {
            World world = Bukkit.getWorld(empire.getWorldName());
            empire.setInRegen(true);
            deleteWorld(world);
            createEmpireWorld(empire);
            empire.setInRegen(false);
            return true;
        }
        return false;
    }

    public Set<String> getUnlockables() {
        return unlockables;
    }
}

package de.themoep.settlements.utils.config;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.event.EventManager;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.npc.NpcManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.CursesFilter;
import de.themoep.settlements.utils.IconRpMapping;
import de.themoep.settlements.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigManager {
    private static ConfigManager instance;

    private Map<String, ConfigParser> parsers = new HashMap<>();
    private static final ConfigParser DEFAULT_PARSER = (TypelessConfigParser) object -> {
        if (object instanceof ConfigurationSection) {
            Map<String, List<String>> map = new HashMap<>();

            for (String key : ((ConfigurationSection) object).getKeys(false)) {
                map.put(key, ((ConfigurationSection) object).getStringList(key));
            }

            return map;
        } else {
            return object;
        }
    };
    private static final ConfigParser ENUM_PARSER = new ConfigParser() {
        @Override
        public <T> Object toJava(Class<T> type, Object object) {
            if (object instanceof String && type.isEnum()) {
                return Enum.valueOf((Class<? extends Enum>) type, ((String) object).toUpperCase(Locale.ROOT));
            }
            return object;
        }

        @Override
        public Object toYaml(Object object) {
            if (object instanceof Enum) {
                return ((Enum) object).name().toLowerCase(Locale.ROOT);
            }
            return object;
        }
    };

    private ConfigAccessor messages;
    private ConfigAccessor commands;
    private ConfigAccessor data;
    private ConfigAccessor settlementLevelConf;
    private ConfigAccessor settlementMissionsConf;
    private ConfigAccessor empireLevelConf;
    private ConfigAccessor empireMissionsConf;
    private ConfigAccessor mapConf;
    private ConfigAccessor npcConf;
    private ConfigAccessor npcLevelConf;
    private ConfigAccessor settlementPerksConf;
    private ConfigAccessor empirePerksConf;
    private ConfigAccessor eventsConf;
    private ConfigAccessor useFlagsConf;
    private IconRpMapping iconRpMapping;
    private CursesFilter toggleableCurses;
    private CursesFilter forcedCurses;
    private ConfigAccessor lastUpkeepConf;

    private ConfigAccessor settlementRanksConf;
    private ConfigAccessor empireRanksConf;

    private ConfigAccessor guisConf;
    private BossBar pvpBossBar;

    private void setup() {
        registerParser("StringSet", new TypelessConfigParser() {
            @Override
            public Object toJava(Object object) {
                if (object instanceof Collection
                        && ((Collection) object).size() > 0
                        && ((Collection) object).iterator().next() instanceof String) {
                    return new LinkedHashSet<>((Collection<String>) object);
                }
                return new LinkedHashSet<String>();
            }
            @Override
            public Object toYaml(Object object) {
                if (object instanceof Collection) {
                    return new ArrayList<>((Collection<?>) object);
                }
                return object;
            }
        });
        registerParser("MaterialSet", new TypelessConfigParser() {
            @Override
            public Object toJava(Object object) {
                EnumSet<Material> set = EnumSet.noneOf(Material.class);
                if (object instanceof Collection) {
                    for (Object o : (Collection) object) {
                        if (o instanceof Material) {
                            set.add((Material) o);
                        } else if (o instanceof String) {
                            if (((String) o).startsWith("*")) {
                                for (Material material : Material.values()) {
                                    if (material.name().endsWith(((String) o).substring(1).toUpperCase())) {
                                        set.add(material);
                                    }
                                }
                            } else if (((String) o).endsWith("*")) {
                                for (Material material : Material.values()) {
                                    if (material.name().startsWith(((String) o).substring(0, ((String) o).length() - 1).toUpperCase())) {
                                        set.add(material);
                                    }
                                }
                            } else {
                                try {
                                    set.add(Material.valueOf(((String) o).toUpperCase(Locale.ROOT)));
                                } catch (IllegalArgumentException e) {
                                    SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, o + " is not a valid Material name in the config!");
                                }
                            }
                        }
                    }
                }
                return set;
            }

            @Override
            public Object toYaml(Object object) {
                if (object instanceof Collection) {
                    return ((Collection) object).stream().map(Objects::toString).collect(Collectors.toList());
                }
                return object;
            }
        });

        reload(false);
    }

    public void reload(boolean isReload) {
        SettlementsPlugin.getInstance().saveDefaultConfig();
        SettlementsPlugin.getInstance().reloadConfig();
        int configVersion = SettlementsPlugin.getInstance().getConfig().getInt("config-version", 0);
        load(SettlementsPlugin.getInstance().getConfig(), "", Config.class, configVersion, isReload);
        messages = new ConfigAccessor(SettlementsPlugin.getInstance(), "messages.yml");
        data = new ConfigAccessor(SettlementsPlugin.getInstance(), "data.yml");
        commands = new ConfigAccessor(SettlementsPlugin.getInstance(), "commands.yml");
        settlementLevelConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "settlementLevelConf.yml");
        settlementMissionsConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "settlementMissions.yml");
        empireLevelConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "empireLevelConf.yml");
        convertEmpireLevels();
        empireMissionsConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "empireMissions.yml");
        mapConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "mapConf.yml");
        npcConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "npcConf.yml");
        npcLevelConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "npcLevelConf.yml");
        settlementPerksConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "settlementPerksConf.yml");
        empirePerksConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "empirePerksConf.yml");
        settlementRanksConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "settlementRanks.yml");
        empireRanksConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "empireRanks.yml");
        eventsConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "events.yml");
        useFlagsConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "useflags.yml");
        toggleableCurses = new CursesFilter(new ConfigAccessor(SettlementsPlugin.getInstance(), "curses.yml").getConfig());
        forcedCurses = new CursesFilter(new ConfigAccessor(SettlementsPlugin.getInstance(), "cursesForced.yml").getConfig());
        lastUpkeepConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "lastUpkeep.yml");
        guisConf = new ConfigAccessor(SettlementsPlugin.getInstance(), "guis.yml");
        iconRpMapping = new IconRpMapping(SettlementsPlugin.getInstance());
        if (Config.Chat.CHAT_ONLY) {
            Config.Lock.ENABLED = false;
            Config.Portal.ENABLED = false;
            Config.Sidebar.ENABLED = false;
        } else if (SettlementsPlugin.getInstance().isRunning()) {
            EmpireManager.getInstance().loadConfig();
            SettlementManager.getInstance().loadConfig();
            NpcManager.getInstance().loadConfig();
            UseFlagManager.getInstance().loadConfig();
            Message.clearCache();
            SettlementsPlugin.getInstance().getLandModeListener().reload();
            updateBossBar();
            if (getEventsConf().getConfig().getBoolean("enabled")) {
                EventManager.getInstance().loadConfig();
            }
        }
    }

    private void convertEmpireLevels() {
        long upkeepCost = getConfig().getLong("empire.upkeep.cost", getConfig().getLong("upkeep.empire", -1));
        long worldRadius = getConfig().getLong("empire.world.radius", getConfig().getLong("empires-world.border-radius", -1));
        if (upkeepCost > -1 || worldRadius > -1) {
            if (upkeepCost > -1) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Found that empire upkeep cost was set to " + upkeepCost + " in the config.yml. Setting that into empireLevelConf.yml. Remove the value to allow modification of the per-level value!");
            }
            if (worldRadius > -1) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Found that empire world radius was set to " + worldRadius + " in the config.yml. Setting that into empireLevelConf.yml. Remove the value to allow modification of the per-level value!");
            }
            for (int i = 1; i <= 10; i++) {
                if (upkeepCost > -1 && upkeepCost != getEmpireLevelConfig().getLong("levels.level-" + i + ".upkeep")) {
                    getEmpireLevelConfig().set("levels.level-" + i + ".upkeep", upkeepCost);
                }
                if (worldRadius > -1 && worldRadius + (i - 1) * 35 != getEmpireLevelConfig().getInt("levels.level-" + i + ".world-radius")) {
                    getEmpireLevelConfig().set("levels.level-" + i + ".world-radius", worldRadius + (i - 1) * 35);
                }
            }
        }
    }

    private void load(FileConfiguration config, String basePath, Class<?> configClass, int configVersion, boolean isReload) {
        for (Field field : configClass.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                ConfigEntry entryInfo = field.getAnnotation(ConfigEntry.class);
                field.setAccessible(true);
                String path;
                if (entryInfo != null && configVersion < entryInfo.old().length) {
                    // Legacy config loading
                    path = entryInfo.old()[configVersion];
                } else {
                    path = basePath + field.getName().toLowerCase(Locale.ROOT).replace('_', '-');
                }
                Object rawValue = config.get(path, null);
                try {
                    Object value = getParser(field).toJava(field.getType(), rawValue);
                    if (value != null) {
                        if (entryInfo == null || entryInfo.reload() || !isReload) {
                            field.set(null, value);
                        } else {
                            Object oldValue = field.get(null);
                            if (!Objects.deepEquals(oldValue, value)) {
                                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried to change a config setting which can't be reloaded: '" + path + "' Please restart.");
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Unable to load value of " + path + "! (Was `" + rawValue + "`, expected " + field.getType().getSimpleName() + ".");
                    SettlementsPlugin.logDebug(e.getMessage());
                }
            }
        }

        for (Class<?> innerClass : configClass.getClasses()) {
            load(config, basePath + getConfigKey(innerClass.getSimpleName()) + ".", innerClass, configVersion, isReload);
        }
    }

    private Map<String, String> loadComments(Class<?> configClass, int configVersion, String basePath, Map<String, String> comments) {
        for (Field field : configClass.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                ConfigComment comment = field.getAnnotation(ConfigComment.class);
                if (comment != null) {
                    ConfigEntry entryInfo = field.getAnnotation(ConfigEntry.class);
                    String path;
                    if (entryInfo != null && configVersion < entryInfo.old().length) {
                        // Legacy config loading
                        path = entryInfo.old()[configVersion];
                    } else {
                        path = basePath + field.getName().toLowerCase(Locale.ROOT).replace('_', '-');
                    }
                    comments.put(path.toLowerCase(Locale.ROOT), comment.value());
                }
            }
        }

        for (Class<?> innerClass : configClass.getClasses()) {
            ConfigComment comment = innerClass.getAnnotation(ConfigComment.class);
            String path = basePath + getConfigKey(innerClass.getSimpleName());
            if (comment != null) {
                comments.put(path.toLowerCase(Locale.ROOT), comment.value());
            }
            comments = loadComments(innerClass, configVersion, path + ".", comments);
        }
        return comments;
    }

    private String getConfigKey(String className) {
        StringBuilder configKey = new StringBuilder();
        for (char c : className.toCharArray()) {
            if (configKey.length() != 0 && Character.isUpperCase(c)) {
                configKey.append('-');
            }
            configKey.append(Character.toLowerCase(c));
        }
        return configKey.toString();
    }

    public void convertConfigTo(int version) {
        int oldVersion = Config.CONFIG_VERSION;
        Config.CONFIG_VERSION = version;
        write(getConfig(), "", version, oldVersion, Config.class);
        SettlementsPlugin.getInstance().saveConfig();
        writeComments(new File(SettlementsPlugin.getInstance().getDataFolder(), "config.yml"), Config.class, Config.CONFIG_VERSION);
    }

    private void write(FileConfiguration config, String basePath, int configVersion, int removeVersion, Class<?> configClass) {
        for (Field field : configClass.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                ConfigEntry entryInfo = field.getAnnotation(ConfigEntry.class);
                field.setAccessible(true);
                String path;
                if (entryInfo != null && configVersion < entryInfo.old().length) {
                    // Legacy config loading
                    path = entryInfo.old()[configVersion];
                } else {
                    path = basePath + field.getName().toLowerCase(Locale.ROOT).replace('_', '-');
                }
                try {
                    Object value = getParser(field).toYaml(field.get(null));
                    if (configVersion != removeVersion && entryInfo != null) {
                        if (removeVersion < entryInfo.old().length) {
                            config.set(entryInfo.old()[removeVersion], null);
                        } else if (removeVersion >= entryInfo.old().length) {
                            String oldPath = basePath + field.getName().toLowerCase(Locale.ROOT).replace('_', '-');
                            if (!config.contains(oldPath, true) && entryInfo.old().length > 0) {
                                oldPath = entryInfo.old()[entryInfo.old().length - 1];
                            }
                            if (!oldPath.equals(path)) {
                                config.set(oldPath, null);
                            }
                        }
                    }
                    if (configVersion == removeVersion || value == null || !value.equals(config.get(path, null))) {
                        config.set(path, value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        for (Class clazz : configClass.getClasses()) {
            write(config, basePath + clazz.getSimpleName().replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT) + ".",
                    configVersion, removeVersion, clazz);
        }
    }

    private void writeComments(File configFile, Class<?> configClass, int configVersion) {
        if (!configFile.exists()) {
            return;
        }
        // Load comments from the config file
        Map<String, String> comments = loadComments(configClass, configVersion, "", new HashMap<>());

        List<String> newConfigLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            ArrayDeque<String> path = new ArrayDeque<>();
            String line;
            // The pattern to be used for the YAML key matching
            Pattern keyPattern = Pattern.compile("^( *)([\\w-]+):.*$");
            int indention = 0;
            String previousComment = null;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                // Only handle and write non-comment lines
                String trimmedLine = line.trim();
                if (!trimmedLine.startsWith("#")) {
                    Matcher keyMatcher = keyPattern.matcher(line);
                    if (keyMatcher.matches() && keyMatcher.groupCount() > 1) {
                        String indent = keyMatcher.group(1);
                        if (indention == 0 && indent.length() > 0) {
                            indention = indent.length();
                        }

                        // Check if the key follows the detected indention of being at least one bigger than the previous one (or smaller)
                        if (indent.length() <= path.size() * indention) {
                            if (indention > 0) {
                                while (path.size() > indent.length() / indention) {
                                    path.pollLast();
                                }
                            } else {
                                path.pollLast();
                            }

                            // Add the key as the last part of the path
                            path.addLast(keyMatcher.group(2));

                            String comment = comments.get(String.join(".", path).toLowerCase(Locale.ROOT));
                            // If there is a comment for that path and it doesn't match the already existing, previous comment then write it
                            if (comment != null) {
                                String[] commentLines = comment.split("\n");
                                if (!commentLines[commentLines.length - 1].equalsIgnoreCase(previousComment)) {
                                    for (String commentLine : commentLines) {
                                        newConfigLines.add(indent + "# " + commentLine);
                                    }
                                }
                                previousComment = null;
                            }
                        }
                    } else {
                        previousComment = trimmedLine.substring(1).trim();
                    }

                    newConfigLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            for (String line : newConfigLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a parser
     * @param name The name of the parser
     * @param configParser The parser itself
     */
    private void registerParser(String name, ConfigParser configParser) {
        parsers.put(name.toLowerCase(Locale.ROOT), configParser);
    }

    /**
     * Get a registered parser
     * @param name The name of the parser
     * @return The parser or null if it doesn't exist
     */
    private ConfigParser getParser(String name) {
        return parsers.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Get the parser that should be used for a field
     * @param field The field
     * @return The registered parser if the field has a Parser annotation or the default one
     */
    private ConfigParser getParser(Field field) {
        ConfigParser parser = null;
        if (field.isAnnotationPresent(ConfigEntry.class)) {
            ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
            if (!configEntry.parser().isEmpty()) {
                parser = getParser(configEntry.parser());
            }
        }
        if (parser == null) {
            parser = getParser(field.getType().getSimpleName());
        }
        if (parser == null && field.getType().isEnum()) {
            parser = ENUM_PARSER;
        }
        if (parser == null) {
            parser = DEFAULT_PARSER;
        }
        return parser;
    }

    public static void save(File file, FileConfiguration fileConfiguration) {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messages.getConfig();
    }

    public FileConfiguration getCommandsConfig() {
        return commands.getConfig();
    }

    public ConfigAccessor getDataConfig() {
        return data;
    }

    public FileConfiguration getSettlementLevelConfig() {
        return settlementLevelConf.getConfig();
    }

    public FileConfiguration getEmpireLevelConfig() {
        return empireLevelConf.getConfig();
    }

    public FileConfiguration getEmpireMissionsConfig() {
        return empireMissionsConf.getConfig();
    }

    public FileConfiguration getSettlementMissionsConfig() {
        return settlementMissionsConf.getConfig();
    }

    public FileConfiguration getMapConfig() {
        return mapConf.getConfig();
    }

    public FileConfiguration getNpcConfig() {
        return npcConf.getConfig();
    }

    public FileConfiguration getNpcLevelConfig() {
        return npcLevelConf.getConfig();
    }

    public FileConfiguration getPerksConfig() {
        return settlementPerksConf.getConfig();
    }

    public FileConfiguration getEmpirePerksConfig() {
        return empirePerksConf.getConfig();
    }

    public ConfigAccessor getSettlementRanksConf() {
        return settlementRanksConf;
    }

    public ConfigAccessor getEmpireRanksConf() {
        return empireRanksConf;
    }

    public ConfigAccessor getEventsConf() {
        return eventsConf;
    }

    public ConfigAccessor getUseFlagsConf() {
        return useFlagsConf;
    }

    public FileConfiguration getGuisConfig() {
        return guisConf.getConfig();
    }

    public CursesFilter getForcedCurses() {
        return forcedCurses;
    }

    public CursesFilter getToggleableCurses() {
        return toggleableCurses;
    }

    public FileConfiguration getConfig() {
        return SettlementsPlugin.getInstance().getConfig();
    }

    public IconRpMapping getIconRpMapping() {
        return iconRpMapping;
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new ConfigManager();
            instance.setup();
        }
    }

    /**
     * Saves the main plugin config
     */
    public void savePluginConfig() {
        write(SettlementsPlugin.getInstance().getConfig(), "", Config.CONFIG_VERSION, Config.CONFIG_VERSION, Config.class);
        SettlementsPlugin.getInstance().saveConfig();
        writeComments(new File(SettlementsPlugin.getInstance().getDataFolder(), "config.yml"), Config.class, Config.CONFIG_VERSION);
    }

    public void updateLastUpkeep() {
        lastUpkeepConf.getConfig().set("last-upkeep", System.currentTimeMillis());
        lastUpkeepConf.saveConfig();
    }

    public long getLastUpkeep() {
        return lastUpkeepConf.getConfig().getLong("last-upkeep");
    }

    public BossBar getBossBar() { return pvpBossBar; }

    public void updateBossBar() { pvpBossBar = Bukkit.createBossBar(Message.get("pvp-claim.bossbar").getText(), BarColor.RED, BarStyle.SOLID); }

}

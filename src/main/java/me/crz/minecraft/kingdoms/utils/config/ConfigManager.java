package me.crz.minecraft.kingdoms.utils.config;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.event.EventManager;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.utils.CursesFilter;
import me.crz.minecraft.kingdoms.utils.IconRpMapping;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.vault.Levels;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
    private ConfigAccessor levelConf;
    private ConfigAccessor mapConf;
    private ConfigAccessor settlementPerksConf;
    private ConfigAccessor empirePerksConf;
    private ConfigAccessor eventsConf;
    private IconRpMapping iconRpMapping;
    private CursesFilter toggleableCurses;
    private CursesFilter forcedCurses;
    private ConfigAccessor lastUpkeepConf;

    private ConfigAccessor settlementRanksConf;
    private ConfigAccessor empireRanksConf;

    private ConfigManager() {
        registerParser("StringSet", new TypelessConfigParser() {
            @Override
            public Object toJava(Object object) {
                if (object instanceof Collection
                        && ((Collection) object).size() > 0
                        && ((Collection) object).iterator().next() instanceof String) {
                    return new LinkedHashSet<>((Collection<String>) object);
                }
                return object;
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
                if (object instanceof Collection) {
                    EnumSet<Material> set = EnumSet.noneOf(Material.class);
                    for (Object o : (Collection) object) {
                        if (o instanceof Material) {
                            set.add((Material) o);
                        } else if (o instanceof String) {
                            try {
                                set.add(Material.getMaterial(((String) o).toUpperCase(Locale.ROOT)));
                            } catch (IllegalArgumentException e) {
                                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, o + " is not a valid Material name in the config!");
                            }
                        }
                    }
                    return set;
                }
                return object;
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
        KingdomsPlugin.getInstance().saveDefaultConfig();
        KingdomsPlugin.getInstance().reloadConfig();
        int configVersion = KingdomsPlugin.getInstance().getConfig().getInt("config-version", 0);
        load(KingdomsPlugin.getInstance().getConfig(), "", Config.class, configVersion, isReload);
        messages = new ConfigAccessor(KingdomsPlugin.getInstance(), "messages.yml");
        data = new ConfigAccessor(KingdomsPlugin.getInstance(), "data.yml");
        commands = new ConfigAccessor(KingdomsPlugin.getInstance(), "commands.yml");
        levelConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "levelConf.yml");
        mapConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "mapConf.yml");
        settlementPerksConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "settlementPerksConf.yml");
        empirePerksConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "empirePerksConf.yml");
        settlementRanksConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "settlementRanks.yml");
        empireRanksConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "empireRanks.yml");
        eventsConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "events.yml");
        toggleableCurses = new CursesFilter(new ConfigAccessor(KingdomsPlugin.getInstance(), "curses.yml").getConfig());
        forcedCurses = new CursesFilter(new ConfigAccessor(KingdomsPlugin.getInstance(), "cursesForced.yml").getConfig());
        lastUpkeepConf = new ConfigAccessor(KingdomsPlugin.getInstance(), "lastUpkeep.yml");
        iconRpMapping = new IconRpMapping(KingdomsPlugin.getInstance());
        if (Config.Chat.CHAT_ONLY) {
            Config.Lock.ENABLED = false;
            Config.Portal.ENABLED = false;
            Config.Lock.ENABLED = false;
        } else if (KingdomsPlugin.getInstance().isRunning()) {
            VillageManager.getInstance().loadPerks();
            Message.clearCache();
            if (Config.Lock.ENABLED) {
                LockManager.getInstance().loadConfig();
            }
            KingdomsPlugin.getInstance().getLandModeListener().reload();
            Levels.clearRequirements();
            if (getEventsConf().getConfig().getBoolean("enabled")) {
                EventManager.getInstance().loadConfig();
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
                Object rawValue = config.get(path);
                try {
                    Object value = getParser(field).toJava(field.getType(), rawValue);
                    if (value != null) {
                        if (entryInfo == null || entryInfo.reload() || !isReload) {
                            field.set(null, value);
                        } else {
                            Object oldValue = field.get(null);
                            if (!Objects.deepEquals(oldValue, value)) {
                                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Tried to change a config setting which can't be reloaded: '" + path + "' Please restart.");
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Unable to load value of " + path + "! (Was `" + rawValue + "`, expected " + field.getType().getSimpleName() + ".");
                    KingdomsPlugin.logDebug(e.getMessage());
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
        KingdomsPlugin.getInstance().saveConfig();
        writeComments(new File(KingdomsPlugin.getInstance().getDataFolder(), "config.yml"), Config.class, Config.CONFIG_VERSION);
    }

    private void write(FileConfiguration config, String basePath, int configVersion, int removeVersion, Class<?> configClass) {
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
                try {
                    Object value = getParser(field).toYaml(field.get(null));
                    if (configVersion != removeVersion && entryInfo != null) {
                        if (removeVersion < entryInfo.old().length) {
                            config.set(entryInfo.old()[removeVersion], null);
                        } else if (removeVersion == entryInfo.old().length) {
                            config.set(basePath + field.getName().toLowerCase(Locale.ROOT).replace('_', '-'), null);
                        }
                    }
                    config.set(path, value);
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

    public FileConfiguration getLevelConfig() {
        return levelConf.getConfig();
    }

    public FileConfiguration getMapConfig() {
        return mapConf.getConfig();
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

    public CursesFilter getForcedCurses() {
        return forcedCurses;
    }

    public CursesFilter getToggleableCurses() {
        return toggleableCurses;
    }

    public FileConfiguration getConfig() {
        return KingdomsPlugin.getInstance().getConfig();
    }

    public IconRpMapping getIconRpMapping() {
        return iconRpMapping;
    }

    synchronized public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Saves all configs that can dynamically change
     */
    public void save() {
        write(KingdomsPlugin.getInstance().getConfig(), "", Config.CONFIG_VERSION, Config.CONFIG_VERSION, Config.class);
        KingdomsPlugin.getInstance().saveConfig();
        writeComments(new File(KingdomsPlugin.getInstance().getDataFolder(), "config.yml"), Config.class, Config.CONFIG_VERSION);
    }

    public void updateLastUpkeep() {
        lastUpkeepConf.getConfig().set("last-upkeep", System.currentTimeMillis());
        lastUpkeepConf.saveConfig();
    }

    public long getLastUpkeep() {
        return lastUpkeepConf.getConfig().getLong("last-upkeep");
    }

}

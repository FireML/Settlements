package de.themoep.settlements.land;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.utils.MaterialUtils;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.Utils.ItemUtils;

import java.util.*;
import java.util.logging.Level;

public class UseFlagManager {
    private static UseFlagManager instance;

    private final Map<String, UseGroup> groups = new LinkedHashMap<>();
    private final Map<String, UseFlag> byId = new LinkedHashMap<>();
    private final Map<Use, UseFlag> byUseType = new EnumMap<>(Use.class);

    private void setup() {
        loadConfig();
        SettlementsPlugin.logDebug("Loaded UseFlagManager with " + byId.size() + " use flags");
    }

    public void loadConfig() {
        ConfigurationSection flagsConfig = ConfigManager.getInstance().getUseFlagsConf().getConfig().getConfigurationSection("flags");
        if (flagsConfig != null) {
            byId.clear();
            byUseType.clear();
            for (String name : flagsConfig.getKeys(false)) {
                ConfigurationSection config = flagsConfig.getConfigurationSection(name);

                Set<Use> uses = EnumSet.noneOf(Use.class);
                if (config.contains("type", true)) {
                    try {
                        uses.add(Use.valueOf(config.getString("type").toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("type") + " is not a valid use type for flag " + name + " in useflags.yml!");
                    }
                } else if (config.contains("types", true) && config.isList("types")) {
                    for (String type : config.getStringList("types")) {
                        try {
                            uses.add(Use.valueOf(type.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, type + " is not a valid use type for flag " + name + " in useflags.yml!");
                        }
                    }
                }

                Material icon = null;
                if (config.contains("icon", true)) {
                    icon = MaterialUtils.getMaterial(config.getString("icon"));
                }
                if (icon != null && !icon.isItem()) {
                    SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, icon + " is not a valid icon type for flag " + name + " in useflags.yml! That type has no item which can be displayed");
                    icon = null;
                }
                if (icon == null) {
                    if (!uses.isEmpty()) {
                        Use use = uses.iterator().next();
                        for (Material material : use.getMaterials()) {
                            if (material.isItem()) {
                                icon = material;
                                break;
                            }
                        }
                    }
                    if (icon == null) {
                        icon = Material.BARRIER;
                    }
                }

                BuildState defaultState = null;
                if (config.contains("default-state", true)) {
                    try {
                        defaultState = BuildState.valueOf(config.getString("default-state", "unknown").toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("default-state") + " is not a valid BuildState for flag " + name + " in useflags.yml!");
                    }
                }

                addFlag(new UseFlag(name, defaultState, icon, uses));
            }
        }

        ConfigurationSection groupsConfig = ConfigManager.getInstance().getUseFlagsConf().getConfig().getConfigurationSection("groups");
        if (groupsConfig != null) {
            groups.clear();
            for (String name : groupsConfig.getKeys(false)) {
                ConfigurationSection config = groupsConfig.getConfigurationSection(name);

                List<UseFlag> flags = new ArrayList<>();
                if (config.contains("flag", true)) {
                    UseFlag flag = get(config.getString("flag"));
                    if (flag != null) {
                        flags.add(flag);
                    } else {
                        SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("flag") + " is not a valid use flag for group " + name + " in useflags.yml!");
                    }
                } else if (config.contains("flags", true) && config.isList("flags")) {
                    for (String type : config.getStringList("flags")) {
                        UseFlag flag = get(type);
                        if (flag != null) {
                            flags.add(flag);
                        } else {
                            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, type + " is not a valid use flag for group " + name + " in useflags.yml!");
                        }
                    }
                }

                ItemStack icon = ItemUtils.toIcon(Material.BARRIER);
                String matStr = config.getString("icon");
                if (matStr != null) {
                    Material material;
                    if (matStr.contains(",")) {
                        String[] split = matStr.split(",");
                        material = Material.matchMaterial(split[0]);
                        if (material == Material.PLAYER_HEAD && split.length > 1) {
                            if (split[1].length() > 16) {
                                icon = ItemUtils.getHead(split[1]);
                            } else if (split.length > 2 && split[2].length() == 36) {
                                icon = ItemUtils.getHead(UUID.fromString(split[2]), split[1]);
                            }
                        }
                    } else {
                        material = Material.matchMaterial(matStr);
                        if (material != null && material.isItem()) {
                            icon = ItemUtils.toIcon(material);
                        } else {
                            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, matStr + " is not a valid icon type for flag " + name + " in useflags.yml! That type has no item which can be displayed");
                            icon = ItemUtils.toIcon(Material.BARRIER);
                        }
                    }
                } else {
                    SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, matStr + " is not a valid icon type for flag " + name + " in useflags.yml! That type has no item which can be displayed");
                    icon = ItemUtils.toIcon(Material.BARRIER);
                }

                BuildState defaultState = null;
                if (config.contains("default-state", true)) {
                    try {
                        defaultState = BuildState.valueOf(config.getString("default-state", "unknown").toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("default-state") + " is not a valid BuildState for group " + name + " in useflags.yml!");
                    }
                }

                addGroup(new UseGroup(name, defaultState, icon, flags));
            }
        }
    }

    private void addGroup(UseGroup group) {
        if (group.getUseFlags().isEmpty()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Group " + group.name() + " in useflags.yml is not valid as it contains no use flags!");
            return;
        }
        groups.put(group.getKey(), group);
    }

    private void addFlag(UseFlag flag) {
        if (flag.getUseTypes().isEmpty()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Flag " + flag.name() + " in useflags.yml is not valid as it contains no use types!");
            return;
        }
        byId.put(flag.name().toLowerCase(Locale.ROOT), flag);
        for (Use useType : flag.getUseTypes()) {
            UseFlag old = byUseType.put(useType, flag);
            if (old != null) {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Use type " + useType.name() + " was defined for multiple flags (" + old.name()  + " and " + flag.name() + " in useflags.yml! Use types can only be defined for one flag! Flag " + flag.name() + " will use it.");
            }
        }
    }

    public static UseFlagManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new UseFlagManager();
            instance.setup();
        }
    }

    public UseFlag get(String id) {
        return byId.get(id.toLowerCase(Locale.ROOT));
    }

    public UseFlag get(Use useType) {
        return byUseType.get(useType);
    }

    public Collection<UseFlag> getUseFlags() {
        return byId.values();
    }

    public Collection<UseGroup> getGroups() {
        return groups.values();
    }
}

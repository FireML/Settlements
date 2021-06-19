package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class UseFlagManager {
    private static UseFlagManager instance;

    private final Map<String, UseFlag> byId = new LinkedHashMap<>();
    private final Map<Use, UseFlag> byUseType = new EnumMap<>(Use.class);

    private UseFlagManager() {
        loadConfig();
        KingdomsPlugin.logDebug("Loaded UseFlagManager with " + byId.size() + " use flags");
    }

    public void loadConfig() {
        ConfigurationSection flags = ConfigManager.getInstance().getUseFlagsConf().getConfig().getConfigurationSection("flags");
        if (flags != null) {
            byId.clear();
            byUseType.clear();
            for (String name : flags.getKeys(false)) {
                ConfigurationSection config = flags.getConfigurationSection(name);

                Set<Use> uses = EnumSet.noneOf(Use.class);
                if (config.contains("type", true)) {
                    try {
                        uses.add(Use.valueOf(config.getString("type").toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("type") + " is not a valid use type for flag " + name + " in useflags.yml!");
                    }
                } else if (config.contains("types", true) && config.isList("types")) {
                    for (String type : config.getStringList("types")) {
                        try {
                            uses.add(Use.valueOf(type.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, type + " is not a valid use type for flag " + name + " in useflags.yml!");
                        }
                    }
                }

                Material icon = null;
                if (config.contains("icon", true)) {
                    icon = MaterialUtils.getMaterial(config.getString("icon"));
                }
                if (icon != null && !icon.isItem()) {
                    KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, icon + " is not a valid icon type for flag " + name + " in useflags.yml! That type has no item which can be displayed");
                    icon = null;
                }
                if (icon == null) {
                    if (uses.size() > 0) {
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
                        KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, config.getString("default-state") + " is not a valid BuildState for flag " + name + " in useflags.yml!");
                    }
                }

                addFlag(new UseFlag(name, defaultState, icon, uses));
            }
        }
    }

    private void addFlag(UseFlag flag) {
        if (flag.getUseTypes().isEmpty()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Flag " + flag.name() + " in useflags.yml is not valid as it contains no use types!");
            return;
        }
        byId.put(flag.getKey(), flag);
        for (Use useType : flag.getUseTypes()) {
            UseFlag old = byUseType.put(useType, flag);
            if (old != null) {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Use type " + useType.name() + " was defined for multiple flags (" + old.name()  + " and " + flag.name() + " in useflags.yml! Use types can only be defined for one flag! Flag " + flag.name() + " will use it.");
            }
        }
    }

    public static UseFlagManager getInstance() {
        if (instance == null) {
            instance = new UseFlagManager();
        }
        return instance;
    }

    public UseFlag get(String id) {
        return byId.get(id.toUpperCase());
    }

    public UseFlag get(Use useType) {
        return byUseType.get(useType);
    }

    public Collection<UseFlag> getUseFlags() {
        return byId.values();
    }
}

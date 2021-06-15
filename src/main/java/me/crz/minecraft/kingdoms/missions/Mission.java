package me.crz.minecraft.kingdoms.missions;

import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public abstract class Mission implements NameTag {
    private final String id;
    private final long missionPoints;
    private final long contributionPoints;
    private final Type type;
    private final Map<String, Long> requirements = new LinkedHashMap<>();

    public Mission(ConfigurationSection config) throws IllegalArgumentException {
        id = config.getName();
        missionPoints = config.getLong("mission-points", 0);
        contributionPoints = config.getLong("contribution-points", 0);
        type = Type.valueOf(config.getString("type", "missing").toUpperCase(Locale.ROOT));

        ConfigurationSection requirementsConfig = config.getConfigurationSection("requirements");
        if (requirementsConfig != null) {
            for (String requirement : requirementsConfig.getKeys(false)) {
                requirements.put(requirement.toUpperCase(Locale.ROOT), requirementsConfig.getLong(requirement));
            }
        } else {
            throw new IllegalArgumentException("Missing requirements in config");
        }
    }

    public static Mission fromDbId(String dbId) {
        String[] split = dbId.split(":", 2);
        if (split.length != 2) {
            return null;
        }
        if ("empiremission".equalsIgnoreCase(split[0])) {
            return EmpireManager.getInstance().getMission(split[1]);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getDbId() {
        return getDbName() + ":" + getId();
    }

    protected abstract String getDbName();

    @Override
    public String name() {
        return getId();
    }

    @Override
    public String getTag() {
        String tag = NameTag.super.getTag();
        if (humanizedName().equals(tag)) {
            return getType().getTag() + " " + tag;
        }
        return tag;
    }

    public long getMissionPoints() {
        return missionPoints;
    }

    public long getContributionPoints() {
        return contributionPoints;
    }

    public Type getType() {
        return type;
    }

    public Map<String, Long> getRequirements() {
        return requirements;
    }

    public boolean isRequirement(String type) {
        return getRequirement(type) > 0;
    }

    public long getRequirement(String type) {
        return getRequirements().getOrDefault(type.toUpperCase(Locale.ROOT), 0L);
    }

    public ItemStack getIcon() {
        for (String r : getRequirements().keySet()) {
            try {
                return ItemUtils.toIcon(Material.valueOf(r));
            } catch (IllegalArgumentException ignored) {}
        }
        return ItemUtils.toIcon(Material.DIRT);
    }

    public abstract void finish(Resident resident);

    public enum Type implements NameTag {
        TURN_IN,
        PLACE,
        BREAK,
        HARVEST,
        KILL
    }
}

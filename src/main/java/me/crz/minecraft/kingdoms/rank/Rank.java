package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Rank implements Rankable, NameTag {

    private final String name;

    private int ordinal = Integer.MAX_VALUE;

    private String inherits = null;

    private final Map<String, Boolean> permissions = new LinkedHashMap<>();

    public Rank(String name, ConfigurationSection config) {
        this.name = name;
        if (config != null) {
            for (String permission : config.getStringList("permissions")) {
                if (permission.startsWith("-")) {
                    permissions.put(permission.substring(1).toLowerCase(), false);
                } else {
                    permissions.put(permission.toLowerCase(), true);
                }
            }
        }
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public Optional<Boolean> getPermissionValue(String permission) {
        return Optional.ofNullable(permissions.get(permission));
    }

    public Optional<Boolean> getEffectivePermissionValue(String permission) {
        Optional<Boolean> value = getPermissionValue(permission);
        if (value.isPresent()) {
            return value;
        } else if (getInherits() == null) {
            return Optional.empty();
        }
        return getInheritedRank().getEffectivePermissionValue(permission);
    }

    public abstract Rank getInheritedRank();

    public String getInherits() {
        return inherits;
    }

    public void setInherits(String inherits) {
        this.inherits = inherits;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int ordinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
}

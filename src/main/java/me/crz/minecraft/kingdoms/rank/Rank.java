package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Rank implements Rankable, NameTag {

    private final String name;

    private int ordinal = Integer.MAX_VALUE;

    private String inherits = null;
    private String parent = null;

    private final Map<String, Boolean> permissions = new LinkedHashMap<>();
    private Material icon = Material.WOODEN_SHOVEL;

    public Rank(String name, ConfigurationSection config) {
        this.name = name;
        if (config != null) {
            for (String permission : config.getStringList("permissions")) {
                String permName = permission.toLowerCase();
                boolean permValue = true;
                if (permission.startsWith("-")) {
                    permName = permName.substring(1);
                    permValue = false;
                }
                permissions.put(permName, permValue);
                if (Bukkit.getPluginManager().getPermission("kingdoms.command." + permName) == null) {
                    Bukkit.getPluginManager().addPermission(new Permission(
                            "kingdoms.command." + permName,
                            Message.get("command." + permName).getText(),
                            PermissionDefault.TRUE
                    ));
                }
            }
            if (config.isString("icon")) {
                icon = Material.matchMaterial(config.getString("icon"));
                if (icon == null) {
                    icon = Material.WOODEN_SHOVEL;
                }
            }
        }
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public Map<String, Boolean> getInheritedPermissions() {
        Map<String, Boolean> inheritedPerms = new LinkedHashMap<>(permissions);
        if (getInherits() != null) {
            for (Map.Entry<String, Boolean> entry : getInheritedRank().getInheritedPermissions().entrySet()) {
                inheritedPerms.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        return inheritedPerms;
    }

    public Optional<Boolean> getPermissionValue(String permission) {
        return Optional.ofNullable(permissions.get(permission));
    }

    public Optional<Boolean> getInheritedPermissionValue(String permission) {
        Optional<Boolean> value = getPermissionValue(permission);
        if (value.isPresent()) {
            return value;
        } else if (getInherits() == null) {
            return Optional.empty();
        }
        return getInheritedRank().getInheritedPermissionValue(permission);
    }

    public abstract Rank getInheritedRank();

    public String getInherits() {
        return inherits;
    }

    public void setInherits(String inherits) {
        this.inherits = inherits;
    }

    public abstract Rank getParentRank();

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
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

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }
}

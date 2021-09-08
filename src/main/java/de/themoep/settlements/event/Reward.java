package de.themoep.settlements.event;

import de.themoep.settlements.utils.MaterialUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Reward {
    private final String rewardId;

    private final String name;

    private ItemStack icon = null;
    private final long money;
    private final List<ItemStack> items = new ArrayList<>();
    private final List<String> commands;

    public Reward(String rewardId, ConfigurationSection config) throws IllegalArgumentException {
        this.rewardId = rewardId;
        this.name = config.getString("name", rewardId);
        this.money = config.getLong("money", config.getInt("money"));
        if (config.isString("icon")) {
            Material iconMat = MaterialUtils.getMaterial(config.getString("icon"));
            if (iconMat != null) {
                if (iconMat.isItem()) {
                    icon = new ItemStack(iconMat);
                } else {
                    throw new IllegalArgumentException("Icon type " + iconMat + " is not an obtainable item and cannot be used for the GUI!");
                }
            }
        }
        for (Map<?, ?> rawMap : config.getMapList("items")) {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            if (!map.containsKey("v")) {
                map.put("v", Bukkit.getUnsafe().getDataVersion());
            }
            if (map.get("type") instanceof String) {
                map.put("type", ((String) map.get("type")).toUpperCase());
            } else {
                throw new IllegalArgumentException("Item config does not contain type?");
            }
            ItemStack item = ItemStack.deserialize(map);
            items.add(item);
            if (icon == null) {
                icon = item;
            }
        }
        if (icon == null) {
            icon = new ItemStack(Material.EMERALD);
        }
        this.commands = config.getStringList("commands");
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public long getMoney() {
        return money;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public List<String> getCommands() {
        return commands;
    }
}

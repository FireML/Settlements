package me.crz.minecraft.kingdoms.event;

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

    public Reward(String rewardId, ConfigurationSection config) {
        this.rewardId = rewardId;
        this.name = config.getString("name", rewardId);
        this.money = config.getLong("money", config.getInt("money"));
        if (config.isString("icon")) {
            Material iconMat = Material.matchMaterial(config.getString("icon"));
            if (iconMat != null) {
                icon = new ItemStack(iconMat);
            }
        }
        for (Map<?, ?> rawMap : config.getMapList("items")) {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            if (!map.containsKey("v")) {
                map.put("v", Bukkit.getUnsafe().getDataVersion());
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

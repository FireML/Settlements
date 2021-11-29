package de.themoep.settlements;

import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class SettlementsGUI implements ConfigMessages {
    protected static final ItemStack BORDER_ITEM = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

    private final String name;

    public SettlementsGUI(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String type() {
        return "gui";
    }

    protected String[] getSetup() {
        return ConfigManager.getInstance().getGuisConfig().getStringList("guis." + getKey() + ".setup").toArray(new String[0]);
    }

    protected Material getType(String c) {
        String matStr = ConfigManager.getInstance().getGuisConfig().getString("guis." + getKey() + ".elements." + c);
        if (matStr != null) {
            return Material.matchMaterial(matStr);
        }
        return null;
    }

    protected ItemStack getIcon(String c) {
        return ItemUtils.toIcon(getType(c));
    }

    protected List<GuiElement> getConfiguredElements() {
        List<GuiElement> elements = new ArrayList<>();

        ConfigurationSection elementsConfig = ConfigManager.getInstance().getGuisConfig().getConfigurationSection("guis." + getKey() + ".elements");
        if (elementsConfig != null) {
            for (String key : elementsConfig.getKeys(false)) {
                if (key.length() == 1) {
                    elements.add(new StaticGuiElement(key.charAt(0), getIcon(key)));
                }
            }
        }

        return elements;
    }

    protected GuiElementGroup.Alignment getAlignment(String c) {
        try {
            return GuiElementGroup.Alignment.valueOf(ConfigManager.getInstance().getGuisConfig().getString("guis." + getKey() + ".alignments." + c, "left").toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return GuiElementGroup.Alignment.LEFT;
        }
    }
}

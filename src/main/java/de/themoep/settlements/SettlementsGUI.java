package de.themoep.settlements;

import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.npc.NpcConfig;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Next;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.Utils.ItemUtils;
import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

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

    protected ItemStack getIcon(String c) {
        return getIcon(c, null);
    }

    protected ItemStack getIcon(String c, ItemStack def) {
        String matStr = ConfigManager.getInstance().getGuisConfig().getString("guis." + getKey() + ".elements." + c);
        if (matStr != null) {
            Material material;
            if (matStr.contains(",")) {
                String[] split = matStr.split(",");
                material = Material.matchMaterial(split[0]);
                if (material == Material.PLAYER_HEAD && split.length > 1) {
                    if (split[1].length() > 16) {
                        return ItemUtils.getHead(split[1]);
                    } else if (split.length > 2 && split[2].length() == 36) {
                        return ItemUtils.getHead(UUID.fromString(split[2]), split[1]);
                    }
                }
            } else {
                material = Material.matchMaterial(matStr);
            }
            if (material == null) {
                return def;
            }
            return ItemUtils.toIcon(material);
        }
        return def;
    }

    protected ItemStack getIcon(Levelable<?> selected) {
        if (selected instanceof NpcData npcData) {
            NpcConfig config = npcData.getConfig();
            if (config.getEntityType() == EntityType.PLAYER) {
                if (!config.getSkinTextures().isEmpty()) {
                    return ItemUtils.getHead(config.getSkinTextures());
                } else if (!config.getSkinName().isEmpty()) {
                    return ItemUtils.getHead(config.getSkinUuid(), config.getSkinName());
                }
            }
            return getIcon(npcData.getType().getKey());
        } else if (selected != null) {
            return getIcon(selected.getKey());
        }
        return getIcon("selectable");
    }

    protected List<GuiElement> getConfiguredElements() {
        List<GuiElement> elements = new ArrayList<>();

        ConfigurationSection elementsConfig = ConfigManager.getInstance().getGuisConfig().getConfigurationSection("guis." + getKey() + ".elements");
        if (elementsConfig != null) {
            for (String key : elementsConfig.getKeys(false)) {
                if (key.length() == 1) {
                    Message loreMessage = getMessage(key);
                    String lore = " ";
                    if (loreMessage.exists()) {
                        lore = loreMessage.getText();
                    }
                    elements.add(new StaticGuiElement(key.charAt(0), getIcon(key), lore));
                }
            }
        }

        return elements;
    }

    protected GuiElementGroup.Alignment getAlignment(String c) {
        try {
            return GuiElementGroup.Alignment.valueOf(ConfigManager.getInstance().getGuisConfig().getString("guis." + getKey() + ".alignments." + c, "left").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return GuiElementGroup.Alignment.LEFT;
        }
    }

    protected GuiElementGroup createGroup(char c) {
        GuiElementGroup group = new GuiElementGroup(c);
        group.setAlignment(getAlignment(String.valueOf(c)));
        return group;
    }

    protected <S extends NameTag & Next<S>> GuiElement getCycleElement(char c, Supplier<S> s, GuiElement.Action click, String type) {
        ItemStack icon = getIcon(type);
        return new DynamicGuiElement(c, who -> {
            S state = s.get();
            return new StaticGuiElement(c, icon, click, getText(type,
                    "state", state.getTag(),
                    "nextstate", state.next().getTag()
            ));
        });
    }
}

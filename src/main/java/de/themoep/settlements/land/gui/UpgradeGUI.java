package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.leveling.LevelInfo;
import de.themoep.settlements.leveling.Levelable;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigKey;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.HumanReadable;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpgradeGUI extends ConfigurableGui {

    private Levelable<? extends LevelInfo> selected;

    public UpgradeGUI(Resident resident, LandOwner<?, ? extends LandownerLevel> landOwner) {
        super(resident, landOwner.getKey() + ".upgrade", "state", landOwner.getStateTag());
        selected = landOwner;

        List<Levelable<? extends LevelInfo>> levelables = new ArrayList<>();
        levelables.add(selected);

        for (NpcType npcType : NpcType.values()) {
            levelables.add(landOwner.getNpcData(npcType));
        }

        GuiElementGroup navBar = new GuiElementGroup('l');

        for (Levelable<?> levelable : levelables) {
            navBar.addElement(new DynamicGuiElement('l', who -> new StaticGuiElement('l',
                    ItemUtils.setGlowing(getIcon(levelable), levelable == selected),
                    click -> {
                        if (levelable.getLevel() > 0 && selected != levelable && !levelable.isMaxLevel()) {
                            selected = levelable;
                            click.getGui().draw();
                        }
                        return true;
                    }, getText("navigation.entry", "name", levelable instanceof LandOwner ? ((LandOwner<?, ?>) levelable).getStateTag() : "npc"),
                                getText("navigation.description." + (levelable.isMaxLevel() ? "upgraded" : levelable.getLevel() > 0 ? "upgradable" : "locked"),
                                        "action", getText("navigation.action." + (levelable == selected ? "current" : "switch"))))));
        }

        gui.addElement(navBar);

        gui.addElement(new DynamicGuiElement('r', who -> {
            GuiElementGroup reqGroup = new GuiElementGroup('r');

            if (!selected.isMaxLevel()) {
                for (Map.Entry<String, Long> entry : selected.getNextLevelRequirements().entrySet()) {
                    ItemStack icon;
                    if (entry.getKey().equalsIgnoreCase("level")) {
                        icon = getIcon(landOwner);
                    } else {
                        icon = ItemUtils.toIcon(Material.matchMaterial(entry.getKey()));
                    }

                    if (icon == null) {
                        icon = getIcon("requirement." + entry.getKey());
                    }

                    boolean hasReached = landOwner.has(entry.getKey(), entry.getValue());

                    return new StaticGuiElement('r', ItemUtils.setGlowing(icon, hasReached), getText("requirement." + (hasReached ? "left" : "reached"),
                            "name", HumanReadable.humanizedName(entry.getKey()),
                            "required", String.valueOf(entry.getValue()),
                            "progress", String.valueOf(selected.getAmount(entry.getKey())),
                            "amount", String.valueOf(entry.getValue() - selected.getAmount(entry.getKey()))
                    ));
                }
            }

            return reqGroup;
        }));

        gui.addElement(new DynamicGuiElement('i', who -> {
            LevelInfo nextLevel = selected.getNextLevel().orElse(null);
            if (nextLevel != null) {
                Collection<? extends LevelPerk> perks = selected.getPerks(nextLevel.getLevel());
                return new StaticGuiElement('i', getIcon(selected), getText("info",
                        "name", selected.getTag(),
                        "benefits", getText("benefits", "benefits", perks.stream()
                                .map(p -> getText("benefit",
                                        "name", p.getTag(),
                                        "description", Message.get("description." + p.getKey()).getText()
                                )).collect(Collectors.joining("\n")))
                ));
            }
            return null;
        }));

        gui.addElement(new DynamicGuiElement('p', who -> {
            LevelInfo nextLevel = selected.getNextLevel().orElse(null);
            if (nextLevel != null) {
                double progress = selected.getNextLevelProgress();
                return new StaticGuiElement('p', getIcon("progress"), getText("progress",
                        "progress", String.valueOf(((int) progress * 1000) / 10d),
                        "progressbar", getText("progress-bar.finished",
                                "bars", String.join("", Collections.nCopies((int) Math.floor(progress * 50), "|"))) +
                                getText("progress-bar.unfinished",
                                        "bars", String.join("", Collections.nCopies((int) Math.ceil((1 - progress) * 50), "|")))
                ));
            }
            return null;
        }));

        gui.addElement(new DynamicGuiElement('d', who -> new StaticGuiElement('d', getIcon(selected), click -> {
            if (click.getType() == ClickType.LEFT && click.getEvent().getCursor() != null) {
                Map<String, Long> requirements = selected.getNextLevelRequirements();
                ItemStack cursor = click.getEvent().getCursor();
                String type = cursor.getType().name();
                if (requirements.containsKey(type)) {
                    long required = requirements.get(type);
                    long amount = selected.getAmount(type);
                    if (amount < required) {
                        int cursorAmount = cursor.getAmount();
                        int added;
                        if (cursorAmount <= required - amount) {
                            added = cursorAmount;
                        } else {
                            added = (int) (required - amount);
                        }
                        cursor.setAmount(cursor.getAmount() - added);
                        if (cursor.getAmount() <= 0) {
                            click.getEvent().setCursor(null);
                        }
                        landOwner.add(type, added);
                        if (!selected.checkLevelUp(resident)) {
                            if (selected instanceof DatabaseSerializable) {
                                ((DatabaseSerializable) selected).save();
                            }
                        }
                        click.getGui().draw();
                    }
                }
            }
            return true;
        }, getText("deposit", "name", selected.getTag()))));
    }

    private ItemStack getIcon(Levelable<?> selected) {
        if (selected instanceof NpcData) {
            // TODO: Npc icons
        } else if (selected instanceof ConfigKey) {
            return getIcon(((ConfigKey) selected).getKey());
        }
        return getIcon("selectable");
    }
}

package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.BuildStateable;
import de.themoep.settlements.land.FlagAble;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.land.UseGroup;
import de.themoep.settlements.land.UseGroupSetter;
import de.themoep.settlements.utils.Message;
import org.bukkit.entity.HumanEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PermsGUI extends SettlementsGUI {

    private final InventoryGui gui;

    public PermsGUI(String type, FlagAble flagAble, Supplier<Boolean> permCheck, BiConsumer<BuildStateable, GuiElement.Click> buildSetter) {
        super(type + ".perms");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());
        gui.addElement(new StaticGuiElement('s', getIcon("states"), getText("states", "state", flagAble.getParent().getStateTag())));

        gui.addElement(new StaticGuiElement('a', getIcon("build"), click -> {
            if (permCheck.get()) {
                buildSetter.accept(flagAble, click);
            }
            return true;
        }, getText("build",
                "build", flagAble.getBuildState().getTag(),
                "state", flagAble.getStateTag()
        )));

        GuiElementGroup flagGroup = createGroup('u');
        for (UseGroup group : UseFlagManager.getInstance().getGroups()) {
            List<String> flagsList = new ArrayList<>();
            for (UseFlag flag : group.getUseFlags()) {
                flagsList.add(getText("flag-line", "name", flag.getTag()));
            }

            flagGroup.addElement(new DynamicGuiElement('u', h -> {
                if (permCheck.get()) {
                    BuildState groupState = flagAble.getUseGroupState(group);
                    String stateTag;
                    if (groupState == null) {
                        stateTag = Message.get("names.buildstate.varied").getText();
                    } else {
                        stateTag = groupState.getTag();
                    }

                    return new StaticGuiElement('u', group.getItem(), click -> {
                        if (click.getType().isShiftClick()) {
                            new UseGUI(type, group, flagAble, permCheck, buildSetter).open(click.getWhoClicked());
                        } else if (permCheck.get()) {
                            buildSetter.accept(new UseGroupSetter(flagAble, group), click);
                        }
                        return true;
                    }, getText("entry",
                            "flag", group.getTag(),
                            "description", getText("descriptions." + group.name().toLowerCase(Locale.ROOT), "state", flagAble.getStateTag()),
                            "state", flagAble.getStateTag(),
                            "flags", String.join("\n", flagsList),
                            "access", stateTag
                    ));
                }
                return null;
            }));
        }
        gui.addElement(flagGroup);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));

    }

    public void open(HumanEntity player) {
        gui.show(player);
    }

}

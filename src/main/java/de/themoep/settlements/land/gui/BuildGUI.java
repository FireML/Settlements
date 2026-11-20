package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.BuildStateable;
import de.themoep.settlements.utils.Message;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BuildGUI extends SettlementsGUI {
    private final InventoryGui gui;


    public BuildGUI(@NonNull String type, BuildStateable stateAble, Supplier<Boolean> permCheck) {
        super(type + ".build");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getComponent("title", "type", stateAble.getBuildTag()), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());
        gui.addElement(new StaticGuiElement('s', getIcon("states"), getComponent("states",
                "state", stateAble.getParent().getStateTag(),
                "type", stateAble.getBuildTag()
        )));

        GuiElementGroup group = new GuiElementGroup('g');
        group.setAlignment(getAlignment("g"));

        for (BuildState state : BuildState.values(stateAble.getBuildStateType())) {
            if (!stateAble.acceptsState(state)) {
                continue;
            }
            group.addElement(new DynamicGuiElement('g', (h) -> {
                if (permCheck.get()) {
                    String[] replacements = {
                            "state", state.getTag(),
                            "description", getMessage("build." + state.getKey()).getReplacedText(
                                    "state", stateAble.getStateTag(),
                                    "type", stateAble.getBuildTag()
                            ),
                            "type", stateAble.getBuildTag()
                    };
                    ItemStack icon;
                    List<Component> description = new ArrayList<>();
                    if (state == stateAble.getBuildState()) {
                        if (stateAble.acceptsState(state)) {
                            icon = getIcon("selected");
                            description.add(getComponent("selected-state", replacements));
                            description.add(getComponent("description", replacements));
                        } else {
                            icon = getIcon("individual");
                            description.add(getComponent("individual-state", replacements));
                            description.add(getComponent("description", replacements));
                        }
                    } else if (state == stateAble.getParent().getBuildState() && !stateAble.acceptsState(state) && (stateAble.getBuildState() == null || !stateAble.getBuildState().appliesTo(stateAble.getParent().getBuildStateType()))) {
                        icon = getIcon("inherited");
                        description.add(getComponent("inherited-state", replacements));
                        description.add(getComponent("description", replacements));
                    } else {
                        icon = getIcon("unset");
                        description.add(getComponent("set-state", replacements));
                        description.add(getComponent("description", replacements));
                    }
                    return new StaticGuiElement('g',
                            icon,
                            click -> {
                                if (permCheck.get()) {
                                    stateAble.setBuildState(state);
                                    Message.get("command." + type + ".build.success").tell(click.getWhoClicked(),
                                            "flagable", stateAble.getStateTag(),
                                            "state", state.getTag(),
                                            "type", stateAble.getBuildTag()
                                    );
                                    stateAble.save();
                                    gui.draw();
                                }
                                return true;
                            },
                            description.toArray(new Component[0])
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getComponent("back")));
    }

    public void open(HumanEntity player) {
        gui.show(player);
    }
}

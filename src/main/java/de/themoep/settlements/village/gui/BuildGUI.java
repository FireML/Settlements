package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.FlagAble;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.resident.Resident;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Supplier;

public class BuildGUI extends SettlementsGUI {
    private final InventoryGui gui;


    public BuildGUI(String type, FlagAble flagAble, Supplier<Boolean> permCheck, Supplier<String> stateProvider) {
        super((type == null || type.isEmpty() ? "" : type + ".") + "build");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());
        gui.addElement(new StaticGuiElement('s', getIcon("states"), getText("states")));

        GuiElementGroup group = new GuiElementGroup('g');
        group.setAlignment(getAlignment("g"));

        for (BuildState state : BuildState.values(flagAble.getBuildStateType())) {
            group.addElement(new DynamicGuiElement('g', (h) -> {
                if (permCheck.get()) {
                    Map<String, String> replacements = Utils.toMap(
                            "state", state.getTag(),
                            "description", getMessage("build." + state.getKey()).getReplacedText("state", stateProvider.get())
                    );
                    if (flagAble.getBuildState() == state) {
                        return new StaticGuiElement('g',
                                ItemUtils.toIcon(Material.LIME_WOOL),
                                getMessage("selected-state").getReplacedText(replacements),
                                getMessage("description").getReplacedText(replacements)
                        );
                    }
                    return new StaticGuiElement('g',
                            ItemUtils.toIcon(Material.LIGHT_GRAY_WOOL),
                            click -> {
                                if (permCheck.get()) {
                                    flagAble.setBuildState(state);
                                    Message.get("command." + type + ".build.success").tell(click.getWhoClicked(), "state", state.getTag());
                                    flagAble.save();
                                    gui.draw();
                                }
                                return true;
                            },
                            getMessage("set-state").getReplacedText(replacements),
                            getMessage("description").getReplacedText(replacements)
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), getText("back")));
    }

    public void open(HumanEntity player) {
        gui.show(player);
    }
}

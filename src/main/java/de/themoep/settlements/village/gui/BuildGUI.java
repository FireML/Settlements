package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

public class BuildGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };

    private final InventoryGui gui;

    public BuildGUI() {
        super("build");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        for (BuildState state : BuildState.values(BuildState.AppliesTo.SETTLEMENT)) {
            group.addElement(new DynamicGuiElement('g', (h) -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                if (resident.hasVillage()) {
                    Map<String, String> replacements = Utils.toMap(
                            "state", state.getTag(),
                            "description", getMessage("build." + state.getKey()).getReplacedText("state", resident.getVillage().getStateTag())
                    );
                    if (resident.getVillage().getBuildState() == state) {
                        return new StaticGuiElement('g',
                                ItemUtils.toIcon(Material.LIME_WOOL),
                                getMessage("selected-state").getReplacedText(replacements),
                                getMessage("description").getReplacedText(replacements)
                        );
                    }
                    return new StaticGuiElement('g',
                            ItemUtils.toIcon(Material.LIGHT_GRAY_WOOL),
                            click -> {
                                if (resident.hasVillage() && resident.hasPermission("village.build")) {
                                    resident.getVillage().setBuildState(state);
                                    Message.get("command.village.build.success").tell(resident, "state", state.getTag());
                                    DatabaseManager.getInstance().saveSettlement(resident.getVillage());
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

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new SettingsGUI((Player) click.getEvent().getWhoClicked()).open();
            return true;
        }, getText("back")));
    }

    public void open(final Player p) {
        gui.show(p);
    }
}

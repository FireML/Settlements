package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ModeGUI extends ConfigurableGui {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };

    public ModeGUI(Resident res) {
        super(res, "empire.mode");

        GuiElementGroup group = createGroup('g');

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND)) {
            group.addElement(new DynamicGuiElement('g', (h) -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                if (resident.hasEmpire()) {
                    boolean enabled = m.get(resident.getSettlement().getEmpire());
                    return new StaticGuiElement('g',
                            ItemUtils.setGlowing(m.getItem(), enabled),
                            click -> {
                                if (resident.hasEmpire() && resident.hasPermission("empire.mode")) {
                                    boolean state = !m.get(resident.getSettlement().getEmpire());
                                    if (m.set(state, resident.getSettlement().getEmpire())) {
                                        DatabaseManager.getInstance().saveEmpire(resident.getSettlement().getEmpire());
                                    }
                                    Message.get("command.empire.mode.success").tell(resident,
                                            "mode", m.getTag(),
                                            "value", getText("entry.state." + state)
                                    );
                                    click.getGui().draw();
                                }
                                return true;
                            },
                            getText("entry.title",
                                    "mode", m.getTag(),
                                    "value", getText("entry.state." + enabled)
                            ),
                            getText("entry.lore",
                                    "mode", m.getTag(),
                                    "value", getText("entry.state." + enabled)
                            )
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));

    }
}

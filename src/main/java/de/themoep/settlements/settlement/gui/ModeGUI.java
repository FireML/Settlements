package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ModeGUI extends ConfigurableGui {

    public ModeGUI(Resident res) {
        super(res, "mode");

        GuiElementGroup group = createGroup('m');

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            group.addElement(new DynamicGuiElement('m', (h) -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                if (resident.hasSettlement()) {
                    boolean enabled = m.get(resident.getSettlement());
                    return new StaticGuiElement('m',
                            ItemUtils.setGlowing(m.getItem(), enabled),
                            click -> {
                                if (resident.hasSettlement() && resident.hasPermission("settlement.mode")) {
                                    boolean state = !m.get(resident.getSettlement());
                                    if (m.set(state, resident.getSettlement())) {
                                        DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
                                    }
                                    Message.get("command.settlement.mode.success").tell(resident,
                                            "mode", m.getTag(),
                                            "value", getText("entry.state." + state)
                                    );
                                    gui.draw();
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

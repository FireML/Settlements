package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;

import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.events.LandModeChangeEvent;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;

public class ModeGUI extends ConfigurableGui {

    public ModeGUI(Resident resident, Settlement landOwner) {
        super(resident, landOwner.getKey() + ".mode");

        GuiElementGroup group = createGroup('m');

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            group.addElement(new DynamicGuiElement('m', (h) -> {
                if (landOwner.hasPermission(resident, "mode")) {
                    boolean enabled = m.get(landOwner);
                    return new StaticGuiElement('m',
                            ItemUtils.setGlowing(m.getItem(), enabled),
                            click -> {
                                if (landOwner.hasPermission(resident, "mode")) {
                                    boolean state = !m.get(landOwner);
                                    if (m.set(state, landOwner)) {
                                        landOwner.save();
                                        landOwner.getLand().forEach(land -> new LandModeChangeEvent(land).callEvent());
                                    }
                                    Message.get("command." + landOwner.getKey() + ".mode.success").tell(this.resident,
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

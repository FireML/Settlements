package de.themoep.settlements.land.gui.plot;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.events.LandModeChangeEvent;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModeGUI extends PlotGUI {

    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    public ModeGUI(Resident resident, OwnedLand editedLand) {
        super(resident, editedLand, "mode");

        GuiElementGroup group = new GuiElementGroup('m');

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND)) {
            group.addElement(new DynamicGuiElement('m', (h) -> {
                OwnedLand land = LandManager.getInstance().getLand(h.getLocation());
                if (land != null) {
                    boolean enabled = m.get(land);
                    List<Component> description = new ArrayList<>();
                    description.add(getComponent("entry.title",
                            "mode", m.getTag(),
                            "value", getText("entry.state." + enabled)
                    ));
                    description.add(getComponent("entry.lore",
                            "mode", m.getTag(),
                            "value", getText("entry.state." + enabled)
                    ));

                    if (m.appliesTo(PlotMode.AppliesTo.LAND_OWNER)) {
                        if (land.hasMode(m)) {
                            description.add(getComponent("entry.click-to-unset",
                                    "parent", land.getSettlement().getStateTag(),
                                    "value", yesOrNo(land.getSettlement().getMode(m))
                            ));
                        } else {
                            description.add(getComponent("entry.parent",
                                    "parent", land.getSettlement().getStateTag()
                            ));
                        }
                    }
                    ItemStack item = m.getItem();
                    ItemUtils.setGlowing(item, enabled);
                    return new StaticGuiElement('m',
                            item,
                            click -> {
                                OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                                if (currentLand != null) {
                                    boolean canAdmin = currentLand.canAdmin(resident);
                                    if (currentLand.isPlotOwner(resident) || canAdmin) {
                                        if (m == PlotMode.embassy) {
                                            if (currentLand.containsHome() || currentLand.isOutpost()) {
                                                getError("command.plot.mode.cant-set-embassy").tell(resident);
                                                return true;
                                            } else if (!canAdmin) {
                                                return true;
                                            }
                                        }
                                        if (click.getType() == ClickType.MIDDLE) {
                                            if (currentLand.unsetMode(m)) {
                                                Message.get("command.plot.mode.unset").tell(resident,
                                                        "mode", m.getTag(),
                                                        "value", yesOrNo(currentLand.getSettlement().getMode(m)),
                                                        "parent", currentLand.getSettlement().getStateTag()
                                                );
                                            } else {
                                                return true;
                                            }
                                        } else {
                                            boolean state = !m.get(currentLand);
                                            currentLand.setMode(m, state);
                                            Message.get("command.plot.mode.success").tell(resident,
                                                    "mode", m.getTag(),
                                                    "value", yesOrNo(state)
                                            );
                                        }
                                        LandManager.getInstance().syncAndSave(currentLand);
                                        new LandModeChangeEvent(currentLand).callEvent();
                                        gui.draw();
                                    }
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
}

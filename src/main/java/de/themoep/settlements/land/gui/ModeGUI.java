package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.world.SimpleChunkLocation;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public class ModeGUI extends PlotGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };
    private final InventoryGui gui;
    private final SimpleChunkLocation landLocation;

    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    public ModeGUI(OwnedLand editedLand) {
        super("mode");
        landLocation = editedLand.getLocation().copy();
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND)) {
            group.addElement(new DynamicGuiElement('g', (h) -> {
                OwnedLand land = LandManager.getInstance().getLand(h.getLocation());
                if (land != null) {
                    boolean enabled = m.get(land);
                    List<String> description = new ArrayList<>();
                    description.add(getText("entry.title",
                            "mode", m.getTag(),
                            "value", getText("entry.state." + enabled)
                    ));
                    description.add(getText("entry.lore",
                            "mode", m.getTag(),
                            "value", getText("entry.state." + enabled)
                    ));

                    if (m.appliesTo(PlotMode.AppliesTo.LAND_OWNER)) {
                        if (land.hasMode(m)) {
                            description.add(getText("entry.click-to-unset",
                                    "parent", land.getLandOwner().getStateTag(),
                                    "value", yesOrNo(land.getLandOwner().getMode(m))
                            ));
                        } else {
                            description.add(getText("entry.parent",
                                    "parent", land.getLandOwner().getStateTag()
                            ));
                        }
                    }
                    return new StaticGuiElement('g',
                            ItemUtils.setGlowing(m.getItem(), enabled),
                            click -> {
                                OwnedLand currentLand = LandManager.getInstance().getLand(click.getEvent().getWhoClicked().getLocation());
                                if (currentLand != null) {
                                    Resident resident = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
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
                                            if (currentLand.unsetMode(m) || currentLand.hasLink()) {
                                                Message.get("command.plot.mode.unset").tell(resident,
                                                        "mode", m.getTag(),
                                                        "value", yesOrNo(currentLand.getLandOwner().getMode(m)),
                                                        "parent", currentLand.getLandOwner().getStateTag()
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
                                        gui.draw();
                                    }
                                }
                                return true;
                            },
                            description.toArray(new String[0])
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', ItemUtils.toIcon(Material.BARRIER), getText("back")));
    }

    public void open(final Player p) {
        // One last check if we can really open this GUI
        OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
        if (currentLand != null && (currentLand.isPlotOwner(p) || currentLand.canAdmin(ResidentManager.getInstance().getResident(p)))) {
            gui.show(p);
        } else {
            getError("not-your-plot").tell(p);
        }
    }
}

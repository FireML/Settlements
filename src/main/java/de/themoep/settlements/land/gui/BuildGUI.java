package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
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
import de.themoep.settlements.land.BuildState;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BuildGUI extends PlotGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggr",
            "         ",
            "    b    "
    };
    private final InventoryGui gui;
    private final SimpleChunkLocation landLocation;

    public BuildGUI(OwnedLand l) {
        super("build");
        landLocation = l.getLocation().copy();

        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        for (BuildState state : BuildState.values(BuildState.AppliesTo.LAND)) {
            if (!l.isPlot() && !state.appliesTo(BuildState.AppliesTo.get(l.getLandOwner()))) {
                continue;
            }
            group.addElement(new DynamicGuiElement('g', (h) -> {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                String[] replacements = {
                        "state", state.getTag(),
                        "description", getText("build." + state.getKey(), "state", land.getLandOwner().getStateTag())
                };
                Material icon;
                List<String> description = new ArrayList<>();
                if (state == land.getBuildState()) {
                    if (land.isPlot() || state.appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
                        icon = Material.LIME_WOOL;
                        description.add(getText("selected-state", replacements));
                        description.add(getText("description", replacements));
                    } else {
                        icon = Material.WHITE_WOOL;
                        description.add(getText("plot-state", replacements));
                        description.add(getText("description", replacements));
                    }
                } else if (state == land.getLandOwner().getBuildState() && !land.isPlot() && (land.getBuildState() == null || !land.getBuildState().appliesTo(BuildState.AppliesTo.get(land.getLandOwner())))) {
                    icon = Material.LIGHT_BLUE_WOOL;
                    description.add(getText("inherited-state", replacements));
                    description.add(getText("description", replacements));
                } else {
                    icon = Material.LIGHT_GRAY_WOOL;
                    description.add(getText("set-state", replacements));
                    description.add(getText("description", replacements));
                }
                return new StaticGuiElement('g',
                        ItemUtils.toIcon(icon),
                        click -> {
                            OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                            if (state != currentLand.getBuildState()) {
                                Resident resident = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                                if (currentLand.isPlotOwner(resident) || currentLand.canAdmin(resident)) {
                                    currentLand.setBuildState(state);
                                    Message.get("command.plot.build.success").tell(resident,
                                            "state", state.getTag()
                                    );
                                    LandManager.getInstance().syncAndSave(currentLand);
                                    gui.draw();
                                }
                            }
                            return true;
                        },
                        description.toArray(new String[0])
                );
            }));
        }

        gui.addElement(group);

        if (!l.isPlot()) {
            gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.EGG), click -> {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                if (land != null && !land.isPlot()) {
                    land.setBuildState(null);
                    gui.draw();
                    LandManager.getInstance().saveLand(land);
                    Message.get("command.plot.build.reset").tell(click.getEvent().getWhoClicked(),
                            "parent", land.getLandOwner().getStateTag(),
                            "state", l.getLandOwner().getBuildState().getTag()
                    );
                }
                return true;
            }, getMessage("reset").getReplacedText(
                    "parent", l.getLandOwner().getStateTag(),
                    "state", l.getLandOwner().getBuildState().getTag()
            )));
        } else {
            gui.addElement(new StaticGuiElement('r', null));
        }

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new SettingsGUI((Player) click.getEvent().getWhoClicked());
            return true;
        }, getText("back")));
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

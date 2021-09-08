package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
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
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public class UseGUI extends PlotGUI {
    private static final String[] SETUP = {
            "         ",
            "uuuuuuuuu",
            "uuuuuuuuu",
            "uuuuuuuuu",
            "aaaaaaaar",
            "p   b   n",
    };
    private final InventoryGui gui;
    private final SimpleChunkLocation landLocation;

    public UseGUI(OwnedLand editedLand) {
        super("use");
        landLocation = editedLand.getLocation().copy();
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);

        gui.setFiller(BORDER_ITEM);

        GuiElementGroup useGroup = new GuiElementGroup('u');
        for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
            useGroup.addElement(new DynamicGuiElement('u', h -> {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                if (land != null) {
                    BuildState state = land.getUseState(u);
                    if (state == null || !land.isPlot() && !state.appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
                        state = land.getLandOwner().getDefaultBuildState();
                    }
                    List<String> text = new ArrayList<>();
                    text.add(getMessage("entry").getReplacedText(
                            "flag", u.getTag(),
                            "state", state.getTag()
                    ));
                    if (land.hasUseState(u)) {
                        text.addAll(getMessage("entry.click-to-unset").getReplacedTextList(
                                "parent", land.getLandOwner().getStateTag(),
                                "value", land.getLandOwner().getUseState(u).getTag()
                        ));
                    } else {
                        text.addAll(getMessage("entry.parent").getReplacedTextList(
                                "parent", land.getLandOwner().getStateTag(),
                                "value", land.getLandOwner().getUseState(u).getTag()
                        ));
                    }
                    return new StaticGuiElement('u', u.getItem(), click -> {
                        OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                        if (currentLand != null) {
                            Resident resident = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                            if (currentLand.isPlotOwner(resident) || currentLand.canAdmin(resident)) {
                                if (click.getType() == ClickType.MIDDLE) {
                                    if (currentLand.unsetUseState(u)) {
                                        Message.get("command.plot.use.unset").tell(resident,
                                                "flag", u.getTag(),
                                                "parent", currentLand.getLandOwner().getStateTag(),
                                                "state", currentLand.getLandOwner().getUseState(u).getTag()
                                        );
                                    } else {
                                        return false;
                                    }
                                } else {
                                    BuildState currentState = currentLand.getUseState(u);
                                    if (currentState == null || !currentLand.isPlot() && !currentState.appliesTo(BuildState.AppliesTo.get(currentLand.getLandOwner()))) {
                                        currentState = currentLand.getLandOwner().getDefaultBuildState();
                                    }
                                    BuildState changeTo = click.getType().isLeftClick()
                                            ? currentState.next(currentLand.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(currentLand.getLandOwner()))
                                            : currentState.previous(currentLand.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(currentLand.getLandOwner()));
                                    currentLand.setUseState(u, changeTo);
                                    Message.get("command.plot.use.success").tell(resident,
                                            "flag", u.getTag(),
                                            "state", changeTo.getTag()
                                    );
                                }
                                LandManager.getInstance().syncAndSave(currentLand);
                                gui.draw();
                                return true;
                            }
                        }
                        return true;
                    }, (String[]) text.toArray(new String[0]));
                }
                return null;
            }));
        }
        gui.addElement(useGroup);

        GuiElementGroup allGroup = new GuiElementGroup('a');

        for (BuildState state : BuildState.values(editedLand.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(editedLand.getLandOwner()))) {
            allGroup.addElement(new StaticGuiElement('a', state.getItem(), click -> {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                if (land != null) {
                    if (land.isPlotOwner((Player) click.getEvent().getWhoClicked())
                            || land.canAdmin(ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()))) {
                        for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                            land.setUseState(u, state);
                        }
                        Message.get("command.plot.use.success-all").tell(click.getEvent().getWhoClicked(),
                                "state", state.getTag()
                        );
                        LandManager.getInstance().syncAndSave(land);
                        gui.draw();
                        return true;
                    }
                }
                return true;
            }, getMessage("toggle-all").getReplacedText("state", state.getTag())));
        }

        gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.EGG), click -> {
            OwnedLand land = LandManager.getInstance().getLand(landLocation);
            if (land != null) {
                if (land.isPlotOwner((Player) click.getEvent().getWhoClicked())
                        || land.canAdmin(ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()))) {
                    for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                        land.unsetUseState(u);
                    }
                    Message.get("command.plot.use.unset-all").tell(click.getEvent().getWhoClicked(),
                            "parent", land.getLandOwner().getStateTag()
                    );
                    LandManager.getInstance().syncAndSave(land);
                    gui.draw();
                    return true;
                }
            }
            return true;
        }, getMessage("unset-all").getReplacedText("parent", editedLand.getLandOwner().getStateTag())));

        gui.addElement(allGroup);

        gui.addElement(new GuiPageElement('p', ItemUtils.toIcon(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, getText("page.previous", "page", "%prevpage%")));

        gui.addElement(new GuiBackElement('b', ItemUtils.toIcon(Material.BARRIER), getText("back")));

        gui.addElement(new GuiPageElement('n', ItemUtils.toIcon(Material.ARROW), GuiPageElement.PageAction.NEXT, getText("page.next", "page", "%nextpage%")));
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

package me.crz.minecraft.kingdoms.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UseGui extends PlotGUI {
    private static final String[] SETUP = {
            "         ",
            "uuuuuuuuu",
            "uuuuuuuuu",
            "uuuuuuuuu",
            "aaaaaaaar",
            "p   b   n",
    };
    private final SimpleChunkLocation landLocation;
    private InventoryGui gui;

    public UseGui(Player player, OwnedLand editedLand) {
        super("plot.use");
        this.landLocation = editedLand.getLocation().copy();
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);

        gui.setFiller(BORDER_ITEM);

        GuiElementGroup useGroup = new GuiElementGroup('u');
        for (Use u : Use.values()) {
            useGroup.addElement(new DynamicGuiElement('u', h -> {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                if (land != null) {
                    BuildState state = land.getUseState(u);
                    if (!land.isPlot() && !state.appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
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
                    return new StaticGuiElement('u', u.getItem().build(), click -> {
                        OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                        if (currentLand != null) {
                            Resident resident = ResidentManager.getInstance().getResident(player);
                            if (currentLand.isPlotOwner(player) || currentLand.canAdmin(resident)) {
                                if (click.getType() == ClickType.MIDDLE) {
                                    if (currentLand.unsetUseState(u)) {
                                        Message.get("command.plot.use.unset").tell(player,
                                                "flag", u.getTag(),
                                                "parent", currentLand.getLandOwner().getStateTag(),
                                                "state", currentLand.getLandOwner().getUseState(u).getTag()
                                        );
                                    } else {
                                        return false;
                                    }
                                } else {
                                    BuildState currentState = currentLand.getUseState(u);
                                    if (!currentLand.isPlot() && !currentState.appliesTo(BuildState.AppliesTo.get(currentLand.getLandOwner()))) {
                                        currentState = currentLand.getLandOwner().getDefaultBuildState();
                                    }
                                    BuildState changeTo = click.getType().isLeftClick()
                                            ? currentState.next(currentLand.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(currentLand.getLandOwner()))
                                            : currentState.previous(currentLand.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(currentLand.getLandOwner()));
                                    currentLand.setUseState(u, changeTo);
                                    Message.get("command.plot.use.success").tell(player,
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
        if (editedLand.isPlotOwner(player) || editedLand.canAdmin(ResidentManager.getInstance().getResident(player))) {
            for (BuildState state : BuildState.values(editedLand.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(editedLand.getLandOwner()))) {
                allGroup.addElement(new StaticGuiElement('a', state.getItem().build(), click -> {
                    OwnedLand land = LandManager.getInstance().getLand(landLocation);
                    if (land != null) {
                        if (land.isPlotOwner(player) || land.canAdmin(ResidentManager.getInstance().getResident(player))) {
                            for (Use u : Use.values()) {
                                land.setUseState(u, state);
                            }
                            Message.get("command.plot.use.success-all").tell(player,
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

            gui.addElement(new StaticGuiElement('r', new ItemStack(Material.EGG), click -> {
                OwnedLand land = LandManager.getInstance().getLand(landLocation);
                if (land != null) {
                    if (land.isPlotOwner(player) || land.canAdmin(ResidentManager.getInstance().getResident(player))) {
                        for (Use u : Use.values()) {
                            land.unsetUseState(u);
                        }
                        Message.get("command.plot.use.unset-all").tell(player,
                                "parent", land.getLandOwner().getStateTag()
                        );
                        LandManager.getInstance().syncAndSave(land);
                        gui.draw();
                        return true;
                    }
                }
                return true;
            }, getMessage("unset-all").getReplacedText("parent", editedLand.getLandOwner().getStateTag())));
        }
        gui.addElement(allGroup);

        gui.addElement(new GuiPageElement('p', new ItemStack(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, getText("page.previous", "page", "%prevpage%")));

        gui.addElement(new GuiBackElement('b', new ItemStack(Material.BARRIER), getText("back")));

        gui.addElement(new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, getText("page.next", "page", "%nextpage%")));

        gui.show(player);
    }
}

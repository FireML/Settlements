package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.FlagAble;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.land.UseGroup;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class UseGUI extends SettlementsGUI {
    private final InventoryGui gui;

    public UseGUI(String type, UseGroup group, FlagAble flagAble, Supplier<Boolean> permCheck) {
        super((type == null || type.isEmpty() ? "" : type + ".") + "perms");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());
        gui.addElement(new StaticGuiElement('s', getIcon("states"), getText("states")));

        GuiElementGroup useGroup = new GuiElementGroup('u');
        useGroup.setAlignment(getAlignment("u"));
        for (UseFlag u : group.getUseFlags()) {
            useGroup.addElement(new DynamicGuiElement('u', h -> {
                Village village = VillageManager.getInstance().getFromPlayer((Player) h);
                if (village != null) {
                    BuildState state = village.getUseState(u);
                    if (state == null) {
                        state = village.getDefaultBuildState();
                    }
                    return new StaticGuiElement('u', u.getItem(), click -> {
                        if (permCheck.get()) {
                            BuildState currentState = flagAble.getUseState(u);
                            if (currentState == null) {
                                currentState = flagAble.getDefaultBuildState();
                            }

                            // TODO: Use unified AccessGUI to set
                            BuildState changeTo = click.getType().isLeftClick()
                                    ? currentState.next(flagAble.getBuildStateType())
                                    : currentState.previous(flagAble.getBuildStateType());
                            flagAble.setUseState(u, changeTo);
                            Message.get("command." + type + ".use.success").tell(click.getEvent().getWhoClicked(),
                                    "flag", u.getTag(),
                                    "state", changeTo.getTag()
                            );
                            flagAble.save();
                            click.getGui().draw();
                        }
                        return true;
                    },getMessage("entry").getReplacedText(
                            "flag", u.getTag(),
                            "access", state.getTag(),
                            "nextaccess", state.next(BuildState.AppliesTo.SETTLEMENT).getTag(),
                            "previousaccess", state.previous(BuildState.AppliesTo.SETTLEMENT).getTag()
                    ));
                }
                return null;
            }));
        }
        gui.addElement(useGroup);

        GuiElementGroup allGroup = new GuiElementGroup('a');
        for (BuildState state : BuildState.values(BuildState.AppliesTo.SETTLEMENT)) {
            allGroup.addElement(new StaticGuiElement('a', state.getItem(), click -> {
                if (permCheck.get()) {
                    for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                        flagAble.setUseState(u, state);
                    }
                    Message.get("command." + type + ".use.success-all").tell(click.getEvent().getWhoClicked(),
                            "state", state.getTag()
                    );
                    flagAble.save();
                    click.getGui().draw();
                }
                return true;
            }, getMessage("toggle-all").getReplacedText("access", state.getTag())));
        }
        gui.addElement(allGroup);

        gui.addElement(new GuiPageElement('p', getIcon("previous"), GuiPageElement.PageAction.PREVIOUS, getText("page.previous", "page", "%prevpage%")));

        gui.addElement(new StaticGuiElement('b', getIcon("back"), click -> {
            if (!InventoryGui.goBack(click.getWhoClicked())) {
                click.getGui().close();
            }
            return true;
        }, getText("back")));

        gui.addElement(new GuiPageElement('n', getIcon("next"), GuiPageElement.PageAction.NEXT, getText("page.next", "page", "%nextpage%")));
    }

    public void open(HumanEntity player) {
        gui.show(player);
    }
}

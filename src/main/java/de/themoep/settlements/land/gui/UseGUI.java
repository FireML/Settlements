package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.BuildStateable;
import de.themoep.settlements.land.FlagAble;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.land.UseFlagSetter;
import de.themoep.settlements.land.UseGroup;
import de.themoep.settlements.utils.Message;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class UseGUI extends SettlementsGUI {
    private final InventoryGui gui;

    public UseGUI(@NonNull String type, UseGroup group, FlagAble flagAble, Supplier<Boolean> permCheck, BiConsumer<BuildStateable, GuiElement.Click> buildSetter) {
        super(type + ".use");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getComponent("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());
        gui.addElement(new StaticGuiElement('s', getIcon("states"), getComponent("states", "state", flagAble.getParent().getStateTag())));

        GuiElementGroup useGroup = createGroup('u');
        for (UseFlag u : group.getUseFlags()) {
            useGroup.addElement(new DynamicGuiElement('u', h -> {
                if (permCheck.get()) {
                    BuildState state = flagAble.getUseState(u);
                    if (state == null) {
                        state = flagAble.getDefaultBuildState();
                    }
                    List<Component> text = new ArrayList<>();
                    text.add(getMessage("entry.text").getReplacedComponent(
                            "flag", u.getTag(),
                            "access", state.getTag(),
                            "nextaccess", state.next(BuildState.AppliesTo.SETTLEMENT).getTag(),
                            "previousaccess", state.previous(BuildState.AppliesTo.SETTLEMENT).getTag()
                    ));
                    FlagAble parent = flagAble.getParent();
                    if (flagAble.hasUseState(u)) {
                        text.addAll(getMessage("entry.click-to-unset").getReplacedComponentList(
                                "parent", parent.getStateTag(),
                                "value", parent.getUseState(u).getTag()
                        ));
                    } else {
                        text.addAll(getMessage("entry.parent").getReplacedComponentList(
                                "parent", parent.getStateTag(),
                                "value", parent.getUseState(u).getTag()
                        ));
                    }
                    return new StaticGuiElement('u', u.getItem(), click -> {
                        if (permCheck.get()) {
                            if (click.getType() == ClickType.MIDDLE) {
                                if (flagAble.unsetUseState(u)) {
                                    Message.get("command." + type + ".use.unset").tell(click.getWhoClicked(),
                                            "flag", u.getTag(),
                                            "parent", flagAble.getParent().getStateTag(),
                                            "state", flagAble.getParent().getUseState(u).getTag()
                                    );
                                    flagAble.save();
                                    click.getGui().draw();
                                }
                            } else {
                                buildSetter.accept(new UseFlagSetter(flagAble, u), click);
                            }
                        }
                        return true;
                    },text.toArray(new Component[0]));
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
                    Message.get("command." + type + ".use.success-all").tell(click.getWhoClicked(),
                            "state", state.getTag()
                    );
                    flagAble.save();
                    click.getGui().draw();
                }
                return true;
            }, getMessage("toggle-all").getReplacedComponent("access", state.getTag())));
        }
        gui.addElement(allGroup);

        gui.addElement(new GuiPageElement('p', getIcon("previous"), GuiPageElement.PageAction.PREVIOUS, getComponent("page.previous", "page", "%prevpage%")));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getComponent("back")));

        gui.addElement(new GuiPageElement('n', getIcon("next"), GuiPageElement.PageAction.NEXT, getComponent("page.next", "page", "%nextpage%")));
    }

    public void open(HumanEntity player) {
        gui.show(player);
    }
}

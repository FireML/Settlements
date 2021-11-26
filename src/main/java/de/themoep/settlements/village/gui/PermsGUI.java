package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.land.UseGroup;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Message;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PermsGUI extends SettlementsGUI {

    private final InventoryGui gui;
    private Resident res;

    public PermsGUI(Resident resident) {
        super("perms");
        this.res = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());
        gui.addElement(new StaticGuiElement('s', getIcon("states"), getText("states")));

        gui.addElement(new StaticGuiElement('a', getIcon("build"), click -> {
            if (resident.hasPermission("village.perms")) {
                new BuildGUI().open((Player) click.getEvent().getWhoClicked());
            }
            return true;
        }, getText("build",
                "build", resident.getVillage().getBuildState().getTag(),
                "state", resident.getVillage().getStateTag()
        )));

        GuiElementGroup flagGroup = new GuiElementGroup('u');
        flagGroup.setAlignment(GuiElementGroup.Alignment.CENTER);
        for (UseGroup group : UseFlagManager.getInstance().getGroups()) {
            List<String> flagsList = new ArrayList<>();
            for (UseFlag flag : group.getUseFlags()) {
                flagsList.add(getText("flag-line", "name", flag.getTag()));
            }

            flagGroup.addElement(new DynamicGuiElement('u', h -> {
                if (res.hasVillage()) {
                    BuildState state = res.getVillage().getBuildState();

                    BuildState groupState = res.getVillage().getUseGroupState(group);
                    String stateTag;
                    if (groupState == null) {
                        stateTag = Message.get("names.buildstate.varied").getText();
                    } else {
                        stateTag = groupState.getTag();
                    }

                    return new StaticGuiElement('u', group.getItem(), click -> {
                        if (click.getType().isShiftClick()) {
                            new UseGUI(group).open((Player) click.getWhoClicked());
                        } else if (resident.hasVillage() && resident.hasPermission("village.perms")) {
                            BuildState currentState = res.getVillage().getUseGroupState(group);
                            if (currentState == null) {
                                currentState = res.getVillage().getDefaultBuildState();
                            }
                            // TODO: Use unified AccessGUI to set
                            BuildState changeTo = click.getType().isLeftClick()
                                    ? currentState.next(BuildState.AppliesTo.SETTLEMENT)
                                    : currentState.previous(BuildState.AppliesTo.SETTLEMENT);
                            res.getVillage().setUseGroupState(group, changeTo);
                            Message.get("command.village.use.success").tell(click.getEvent().getWhoClicked(),
                                    "flag", group.getTag(),
                                    "state", changeTo.getTag()
                            );
                            DatabaseManager.getInstance().saveSettlement(res.getVillage());
                            click.getGui().draw();
                        }
                        return true;
                    }, getText("entry",
                            "flag", group.getTag(),
                            "description", getText("descriptions." + group.name().toLowerCase(Locale.ROOT), "state", resident.getVillage().getStateTag()),
                            "state", resident.getVillage().getStateTag(),
                            "flags", String.join("\n", flagsList),
                            "access", stateTag,
                            "nextaccess", state.next(BuildState.AppliesTo.SETTLEMENT).getTag(),
                            "previousaccess", state.previous(BuildState.AppliesTo.SETTLEMENT).getTag()
                    ));
                }
                return null;
            }));
        }
        gui.addElement(flagGroup);

        gui.addElement(new StaticGuiElement('b', getIcon("back"), click -> {
            if (!InventoryGui.goBack(click.getWhoClicked())) {
                click.getGui().close();
            }
            return true;
        }, getText("back")));

    }

    public void open() {
        gui.show(res.getPlayer());
    }

}

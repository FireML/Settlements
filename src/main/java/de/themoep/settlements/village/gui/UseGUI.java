package de.themoep.settlements.village.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
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
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import org.bukkit.entity.Player;

public class UseGUI extends SettlementsGUI {
    private final InventoryGui gui;

    public UseGUI(UseGroup group) {
        super("use");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());
        gui.addElement(new StaticGuiElement('s', getIcon("states"), getText("states")));

        GuiElementGroup useGroup = new GuiElementGroup('u');
        useGroup.setAlignment(GuiElementGroup.Alignment.CENTER);
        for (UseFlag u : group.getUseFlags()) {
            useGroup.addElement(new DynamicGuiElement('u', h -> {
                Village village = VillageManager.getInstance().getFromPlayer((Player) h);
                if (village != null) {
                    BuildState state = village.getUseState(u);
                    if (state == null) {
                        state = village.getDefaultBuildState();
                    }
                    return new StaticGuiElement('u', u.getItem(), click -> {
                        Resident res = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                        if (res.hasVillage() && res.hasPermission("village.use")) {
                            BuildState currentState = res.getVillage().getUseState(u);
                            if (currentState == null) {
                                currentState = res.getVillage().getDefaultBuildState();
                            }

                            // TODO: Use unified AccessGUI to set
                            BuildState changeTo = click.getType().isLeftClick()
                                    ? currentState.next(BuildState.AppliesTo.SETTLEMENT)
                                    : currentState.previous(BuildState.AppliesTo.SETTLEMENT);
                            res.getVillage().setUseState(u, changeTo);
                            Message.get("command.village.use.success").tell(click.getEvent().getWhoClicked(),
                                    "flag", u.getTag(),
                                    "state", changeTo.getTag()
                            );
                            DatabaseManager.getInstance().saveSettlement(res.getVillage());
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
                Resident res = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                if (res.hasVillage() && res.hasPermission("village.use")) {
                    for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                        res.getVillage().setUseState(u, state);
                    }
                    Message.get("command.village.use.success-all").tell(click.getEvent().getWhoClicked(),
                            "state", state.getTag()
                    );
                    DatabaseManager.getInstance().saveSettlement(res.getVillage());
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

    public void open(Player p) {
        gui.show(p);
    }
}

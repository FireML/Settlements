package de.themoep.settlements.resident.gui;

import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.settlement.SettlementHomeCommand;
import de.themoep.settlements.command.settlement.SettlementRulesCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.gui.plot.SettingsGUI;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ItemUtils;

import java.util.Collection;

public class OverviewGUI extends ConfigurableGui {

    public OverviewGUI(Resident resident) {
        super(resident, "general.overview",
                "state", resident.hasSettlement() ? resident.getSettlement().getStateTag() : "",
                "name", resident.hasSettlement() ? resident.getSettlement().getName() : ""
        );
        if (!resident.hasSettlement()) {
            return;
        }

        Settlement settlement = resident.getSettlement();

        gui.addElement(new StaticGuiElement('s', getIcon("settlement"),
                getText("settlement", settlement.getReplacements(this))));

        gui.addElement(new StaticGuiElement('r', getIcon("rules"), click -> {
            ((SettlementRulesCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("rules"))
                    .run(resident, "settlement", "rules");
            return true;
        }, getText("rules", "state", settlement.getStateTag(), "name", settlement.getName())));

        gui.addElement(new StaticGuiElement('h', getIcon("home"), click -> {
            ((SettlementHomeCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("home"))
                    .run(resident, "settlement", "home");
            return true;
        }, getText("home", "state", settlement.getStateTag(), "name", settlement.getName())));

        OwnedLand land = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        gui.addElement(new StaticGuiElement('p', getIcon("plot"), click -> {
            OwnedLand currentLand = LandManager.getInstance().getLand(click.getWhoClicked().getLocation());
            if (currentLand != null && (currentLand.isPlotOwner(resident) || currentLand.canAdmin(resident))) {
                new SettingsGUI(resident).open();
            }
            return true;
        }, getText("plot",
                "action", getText(land != null
                        ? land.isPlotOwner(resident) || land.canAdmin(resident)
                                ? "unlockables.unlocked.options"
                                : "unlockables.locked.not-own-plot"
                        : "unlockables.locked.not-on-land"
                )
        )));

        Collection<FriendRequest> requests = resident.getFriendRequests();
        gui.addElement(new StaticGuiElement('f', ItemUtils.setGlowing(getIcon("friends"), !requests.isEmpty()), click -> {
            new FriendsList(resident, "general.friends").open();
            return true;
        }, getText("friends")));
    }
}

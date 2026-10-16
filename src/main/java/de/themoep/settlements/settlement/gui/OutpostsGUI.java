package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.settlement.SettlementHomeCommand;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.world.NamedLocation;

public class OutpostsGUI extends ConfigurableGui {

    public OutpostsGUI(Resident resident, Settlement settlement) {
        super(resident, "settlement.outposts");

        GuiElementGroup group = createGroup('e');

        for (NamedLocation outpost : settlement.getOutpostHomes().values()) {
            group.addElement(
                    new StaticGuiElement('e', getIcon("outpost"), click -> {
                        if (click.getType().isShiftClick()
                                && (settlement.hasPermission(resident, "outpost.rename")
                                        || settlement.hasPermission(resident, "outpost.sethome")
                                        || settlement.hasPermission(resident, "outpost.unclaim"))) {
                            new OutpostGUI(resident, settlement, outpost).open();
                        } else if (settlement.hasPermission(resident, "outpost.teleport")) {
                            OwnedLand land = LandManager.getInstance().getLand(outpost.toChunkLocation());
                            if (land != null && land.isOwnedBy(resident.getSettlement())) {
                                CommandManager.getInstance().getCommand(settlement.getKey()).matchSubCommand("outpost").matchSubCommand("teleport")
                                        .run(click.getWhoClicked(), settlement.getKey() + " outpost", "teleport", outpost.getName());
                            } else {
                                if (resident.getSettlement().removeOutpostHome(outpost) != null) {
                                    resident.save();
                                }
                                getError("outpost-not-claimed").tell(resident, "name", outpost.getName(), "state", resident.getSettlement().getStateTag());
                            }
                        }
                        return true;
                    }, getText("entry", outpost.getReplacements()),
                            settlement.hasPermission(resident, "outpost.rename")
                                    || settlement.hasPermission(resident, "outpost.sethome")
                                    || settlement.hasPermission(resident, "outpost.unclaim") ? getText("options", outpost.getReplacements()) : null)
            );
        }

        gui.addElement(group);

        gui.addElement(new StaticGuiElement('p', getIcon("public"), click -> {
            new ListGui<>(resident, "general.public", () -> SettlementManager.getInstance().getSettlements().stream()
                    .filter(s -> s.getAccess() == LandAccess.PUBLIC)
                    .toList(), (c, s) -> {
                // teleport to public settlements
                if (s.getAccess() == LandAccess.PUBLIC) {
                    click.getGui().close();
                    ((SettlementHomeCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("home"))
                            .run(resident, "settlement", "home", s.getName());
                } else {
                    click.getGui().draw();
                }
            }, s -> new String[0]).open();
            return true;
        }, getText("public")));

    }
}

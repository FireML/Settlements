package de.themoep.settlements.teleporter;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.empire.EmpireHomeCommand;
import de.themoep.settlements.command.settlement.SettlementHomeCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TeleportersGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "lllllllll",
            "         ",
            "    p    ",
            " v     e ",
            "         ",
            "lllllllll"
    };

    private final InventoryGui gui;

    public TeleportersGUI() {
        super("teleporters");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP) {};
        gui.addElement('l', BORDER_ITEM, " ");
        gui.setCloseAction(close -> false);

        gui.addElement('p', ItemUtils.toIcon(Material.PLAYER_HEAD), click -> {
            Player p = (Player) click.getEvent().getWhoClicked();
            new PrivateTeleportersGUI(p);
            return true;
        }, getText("private"));

        gui.addElement(new DynamicGuiElement('v', h -> {
            Settlement settlement = SettlementManager.getInstance().getFromPlayer((Player) h);
            if (settlement == null || !settlement.hasHome()) {
                return new StaticGuiElement('v', ItemUtils.toIcon(Material.RED_STAINED_GLASS), getText("locked"));
            }

            Resident chief = settlement.getChief();

            return new StaticGuiElement(
                    'v',
                    ItemUtils.getHead(chief.getUUID(), chief.getName()),
                    click -> {
                        Resident resident = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                        if (!resident.hasSettlement() || !resident.getSettlement().hasHome()) {
                            gui.draw();
                            return true;
                        }

                        click.getGui().close();
                        ((SettlementHomeCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("home")).run(
                                ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()),
                                "settlement",
                                "home"
                        );
                        return true;
                    },
                    getText("settlement-home", "state", settlement.getStateTag())
            );
        }));

        gui.addElement(new DynamicGuiElement('e', h -> {
            Settlement settlement = SettlementManager.getInstance().getFromPlayer((Player) h);
            if (settlement == null || !settlement.hasHome() || !settlement.hasEmpire() || !settlement.getEmpire().hasHome()) {
                return new StaticGuiElement('e', ItemUtils.toIcon(Material.RED_STAINED_GLASS), getText("locked"));
            }

            Resident leader = settlement.getEmpire().getCapital().getChief();

            return new StaticGuiElement(
                    'e',
                    ItemUtils.getHead(leader.getUUID(), leader.getName()),
                    click -> {
                        Resident resident = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                        if (!resident.hasEmpire() || !resident.getSettlement().getEmpire().hasHome()) {
                            gui.draw();
                            return true;
                        }

                        click.getGui().close();
                        ((EmpireHomeCommand) CommandManager.getInstance().getCommand("empire").getSubCommands().get("home")).run(
                                ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()),
                                "empire",
                                "home"
                        );
                        return true;
                    },
                    getText("empire-home", "state", settlement.getStateTag())
            );
        }));
    }

    public void open(Player p) {
        gui.show(p);
    }
}

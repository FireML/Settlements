package de.themoep.settlements.teleporter;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.empire.EmpireHomeCommand;
import de.themoep.settlements.command.village.VillageHomeCommand;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
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
            Village village = VillageManager.getInstance().getFromPlayer((Player) h);
            if (village == null || !village.hasHome()) {
                return new StaticGuiElement('v', ItemUtils.toIcon(Material.RED_STAINED_GLASS), getText("locked"));
            }

            Resident chief = village.getChief();

            return new StaticGuiElement(
                    'v',
                    ItemUtils.getHead(chief.getUUID(), chief.getName()),
                    click -> {
                        Resident resident = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                        if (!resident.hasVillage() || !resident.getVillage().hasHome()) {
                            gui.draw();
                            return true;
                        }

                        click.getGui().close();
                        ((VillageHomeCommand) CommandManager.getInstance().getCommand("village").getSubCommands().get("home")).run(
                                ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked()),
                                "village",
                                "home"
                        );
                        return true;
                    },
                    getText("village-home", "state", village.getStateTag())
            );
        }));

        gui.addElement(new DynamicGuiElement('e', h -> {
            Village village = VillageManager.getInstance().getFromPlayer((Player) h);
            if (village == null || !village.hasHome() || !village.hasEmpire() || !village.getEmpire().hasHome()) {
                return new StaticGuiElement('e', ItemUtils.toIcon(Material.RED_STAINED_GLASS), getText("locked"));
            }

            Resident leader = village.getEmpire().getCapital().getChief();

            return new StaticGuiElement(
                    'e',
                    ItemUtils.getHead(leader.getUUID(), leader.getName()),
                    click -> {
                        Resident resident = ResidentManager.getInstance().getResident((Player) click.getEvent().getWhoClicked());
                        if (!resident.hasEmpire() || !resident.getVillage().getEmpire().hasHome()) {
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
                    getText("village-home", "state", village.getStateTag())
            );
        }));
    }

    public void open(Player p) {
        gui.show(p);
    }
}

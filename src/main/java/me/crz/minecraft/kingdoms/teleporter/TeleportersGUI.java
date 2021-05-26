package me.crz.minecraft.kingdoms.teleporter;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.command.empire.EmpireHomeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageHomeCommand;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TeleportersGUI extends KingdomsGUI {
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
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP) {};
        gui.addElement('l', BORDER_ITEM, " ");
        gui.setCloseAction(close -> false);

        gui.addElement('p', ItemUtils.toIcon(Material.PLAYER_HEAD), click -> {
            Player p = (Player) click.getEvent().getWhoClicked();
            new PrivateTeleportersGUI(p);
            return true;
        }, getText("private"));

        gui.addElement(new DynamicGuiElement('v', h -> {
            Village village = VillageManager.getInstance().getVillageFromPlayer((Player) h);
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
            Village village = VillageManager.getInstance().getVillageFromPlayer((Player) h);
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

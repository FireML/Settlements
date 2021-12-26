package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.empire.EmpireHomeCommand;
import de.themoep.settlements.command.empire.EmpireSetHomeCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.land.gui.AccessGUI;
import de.themoep.settlements.land.gui.ModeGUI;
import de.themoep.settlements.land.gui.RankPermsGUI;
import de.themoep.settlements.land.gui.TitleGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.land.gui.BuildGUI;
import de.themoep.settlements.land.gui.PermsGUI;
import org.bukkit.Material;

import java.util.function.Supplier;

public class SettingsGUI extends ConfigurableGui {

    public SettingsGUI(Resident resident) {
        super(resident, "empire.settings");

        gui.addElement(new StaticGuiElement('h', ItemUtils.toIcon(Material.BEACON), click -> {
            if (click.getType().isShiftClick()) {
                if (resident.hasPermission("empire.sethome")) {
                    click.getGui().close();
                    ((EmpireSetHomeCommand) CommandManager.getInstance().getCommand("empire").getSubCommands().get("sethome")).run(resident, "empire", "sethome");
                }
            } else if (click.getType().isLeftClick() && resident.hasEmpire()) {
                click.getGui().close();
                ((EmpireHomeCommand) CommandManager.getInstance().getCommand("empire").getSubCommands().get("home")).run(resident, "empire", "home");
            }
            return true;
        }, getText(resident.hasPermission("empire.sethome") ? "sethome" : "home",
                "state", resident.getSettlement().getEmpire().getStateTag()
        )));

        if (resident.hasPermission("empire.setaccess")) {
            gui.addElement(new StaticGuiElement('a', ItemUtils.toIcon(Material.IRON_DOOR),click -> {
                if (resident.hasPermission("empire.setaccess")) {
                    new AccessGUI(resident, "empire", resident.getSettlement().getEmpire()).open();
                }
                return true;
            }, getText("access",
                    "access", resident.getSettlement().getEmpire().getAccess().getTag(),
                    "state", resident.getSettlement().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.build")) {
            gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.DIAMOND_PICKAXE), click -> {
                if (resident.hasPermission("empire.build")) {
                    new BuildGUI(
                            "empire",
                            resident.getSettlement().getEmpire(),
                            () -> resident.hasEmpire() && resident.hasPermission("empire.build")
                    ).open(resident.getPlayer());
                }
                return true;
            }, getText("build",
                    "build", resident.getSettlement().getEmpire().getBuildState().getTag(),
                    "state", resident.getSettlement().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.perms")) {
            gui.addElement(new StaticGuiElement('u', ItemUtils.toIcon(Material.LEVER),click -> {
                if (resident.hasPermission("empire.perms")) {
                    Supplier<Boolean> permCheck = () -> resident.hasEmpire() && resident.hasPermission("empire.perms");
                    new PermsGUI(
                            "empire",
                            resident.getSettlement().getEmpire(),
                            permCheck,
                            (flagAble, c) -> new BuildGUI("empire", flagAble, permCheck).open(c.getWhoClicked())
                    ).open(resident.getPlayer());
                }
                return true;
            }, getText("use",
                    "state", resident.getSettlement().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.mode")) {
            gui.addElement(new StaticGuiElement('m', ItemUtils.toIcon(Material.REPEATER),click -> {
                if (resident.hasPermission("empire.mode")) {
                    new ModeGUI(resident, resident.getSettlement().getEmpire()).open();
                }
                return true;
            }, getText("mode",
                    "state", resident.getSettlement().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.ranks")) {
            gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.RED_BANNER), click -> {
                if (resident.hasPermission("empire.ranks")) {
                    new RankPermsGUI<>("empire", resident, resident.getSettlement().getEmpire()).open();
                }
                return true;
            }, getText("perms",
                    "state", resident.getSettlement().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.perks")) {
            gui.addElement(new StaticGuiElement('p', ItemUtils.toIcon(Material.LINGERING_POTION), click -> {
                if (resident.hasPermission("empire.perks")) {
                    new PerksGUI(resident).open();
                }
                return true;
            }, getText("perks",
                    "state", resident.getSettlement().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.shop")) {
            gui.addElement(new StaticGuiElement('s', ItemUtils.toIcon(Material.POTION), click -> {
                if (resident.hasPermission("empire.shop")) {
                    new ShopGUI(resident).open();
                }
                return true;
            }, getText("shop",
                    "state", resident.getSettlement().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.title")) {
            gui.addElement(new StaticGuiElement('t', ItemUtils.toIcon(Material.PAPER), click -> {
                if (resident.hasPermission("empire.title")) {
                    new TitleGUI<>("empire", resident, resident, resident.getSettlement().getEmpire()).open();
                }
                return true;
            }, getText(resident.hasPermission("empire.titles") ? "titles-edit" : "titles",
                    "state", resident.getSettlement().getEmpire().getStateTag(),
                    "title", resident.getEmpireTitle()
            )));
        }

        Empire empire = resident.getSettlement().getEmpire();
        gui.addElement(new StaticGuiElement('i', ItemUtils.toIcon(Material.BLUE_BED),
                Utils.wrap(getText("entry", empire.getReplacements(SettingsGUI.this)), 48)));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

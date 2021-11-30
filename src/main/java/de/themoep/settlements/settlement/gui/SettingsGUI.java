package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.land.gui.BuildGUI;
import de.themoep.settlements.land.gui.PermsGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.settlement.SettlementHomeCommand;
import de.themoep.settlements.command.settlement.SettlementSetHomeCommand;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class SettingsGUI extends SettlementsGUI {

    private final static String[] SETUP = {
            "llllillll",
            "         ",
            " um h ts ",
            " ba   rp ",
            "         ",
            "llllcllll"
    };

    private final Resident resident;
    public InventoryGui gui;

    public SettingsGUI(Player player) {
        this(ResidentManager.getInstance().getResident(player));
    }

    public SettingsGUI(Resident resident) {
        super("settings");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);

        gui.addElement(new StaticGuiElement('l', BORDER_ITEM, " "));

        gui.addElement(new StaticGuiElement('h', ItemUtils.toIcon(Material.BEACON),click -> {
            if (click.getType().isShiftClick()) {
                if (resident.hasPermission("settlement.sethome")) {
                    click.getGui().close();
                    ((SettlementSetHomeCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("sethome")).run(resident, "settlement", "sethome");
                }
            } else if (click.getType().isLeftClick() && resident.hasSettlement()) {
                click.getGui().close();
                ((SettlementHomeCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("home")).run(resident, "settlement", "home");
            }
            return true;
        }, getText(resident.hasPermission("settlement.sethome") ? "sethome" : "home",
                "state", resident.getSettlement().getStateTag()
        )));

        if (resident.hasPermission("settlement.setaccess")) {
            gui.addElement(new StaticGuiElement('a', ItemUtils.toIcon(Material.IRON_DOOR), click -> {
                if (resident.hasPermission("settlement.setaccess")) {
                    new AccessGUI().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("access",
                    "access", resident.getSettlement().getAccess().getTag(),
                    "state", resident.getSettlement().getStateTag()
            )));
        }

        if (resident.hasPermission("settlement.perms")) {
            gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.DIAMOND_PICKAXE), click -> {
                if (resident.hasPermission("settlement.perms")) {
                    new BuildGUI(
                            "settlement",
                            resident.getSettlement(),
                            () -> resident.hasSettlement() && resident.hasPermission("settlement.perms")
                    ).open(click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("build",
                    "build", resident.getSettlement().getBuildState().getTag(),
                    "state", resident.getSettlement().getStateTag()
            )));
        }

        if (resident.hasPermission("settlement.perms")) {
            gui.addElement(new StaticGuiElement('u', ItemUtils.toIcon(Material.LEVER), click -> {
                if (resident.hasPermission("settlement.perms")) {
                    Supplier<Boolean> permCheck = () -> resident.hasSettlement() && resident.hasPermission("settlement.perms");
                    new PermsGUI(
                            "settlement",
                            resident.getSettlement(),
                            permCheck,
                            (flagAble, c) -> new BuildGUI("settlement", flagAble, permCheck).open(c.getWhoClicked())
                    ).open(resident.getPlayer());
                }
                return true;
            }, getText("perms",
                    "state", resident.getSettlement().getStateTag()
            )));
        }

        if (resident.hasPermission("settlement.mode")) {
            gui.addElement(new StaticGuiElement('m', ItemUtils.toIcon(Material.REPEATER), click -> {
                if (resident.hasPermission("settlement.mode")) {
                    new ModeGUI().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("mode",
                    "state", resident.getSettlement().getStateTag()
            )));
        }

        if (resident.hasPermission("settlement.ranks")) {
            gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.RED_BANNER), click -> {
                if (resident.hasPermission("settlement.ranks")) {
                    new RanksGUI((Player) click.getEvent().getWhoClicked()).open();
                }
                return true;
            }, getText("ranks",
                    "state", resident.getSettlement().getStateTag()
            )));
        }

        if (resident.hasPermission("settlement.perks")) {
            gui.addElement(new StaticGuiElement('p', ItemUtils.toIcon(Material.LINGERING_POTION), click -> {
                if (resident.hasPermission("settlement.perks")) {
                    new PerksGUI(resident).open();
                }
                return true;
            }, getText("perks",
                    "state", resident.getSettlement().getStateTag()
            )));
        }

        if (resident.hasPermission("settlement.shop")) {
            gui.addElement(new StaticGuiElement('s', ItemUtils.toIcon(Material.POTION), click -> {
                if (resident.hasPermission("settlement.shop")) {
                    new ShopGUI(resident).open();
                }
                return true;
            }, getText("shop",
                    "state", resident.getSettlement().getStateTag()
            )));
        }

        if (resident.hasPermission("settlement.title")) {
            gui.addElement(new StaticGuiElement('t', ItemUtils.toIcon(Material.PAPER), click -> {
                if (resident.hasPermission("settlement.title")) {
                    new TitleGUI(resident).open();
                }
                return true;
            }, getText(resident.hasPermission("settlement.titles") ? "titles-edit" : "titles",
                    "state", resident.getSettlement().getStateTag(),
                    "title", resident.getSettlementTitle()
            )));
        }

        Settlement settlement = resident.getSettlement();
        gui.addElement(new StaticGuiElement('i', ItemUtils.toIcon(settlement.getState() == SettlementState.KINGDOM ? Material.RED_BED : Material.LIME_BED),
                Utils.wrap(getText("entry", settlement.getReplacements(SettingsGUI.this)), 48)));

        gui.addElement(new StaticGuiElement('c', ItemUtils.toIcon(Material.BARRIER), click -> {
            click.getGui().close();
            return true;
        }, getText("close")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

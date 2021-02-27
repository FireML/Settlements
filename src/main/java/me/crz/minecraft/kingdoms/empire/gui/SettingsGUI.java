package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.command.empire.EmpireHomeCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireSetHomeCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SettingsGUI extends KingdomsGUI {

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
        super("empire.settings");
        this.resident = resident;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);

        gui.addElement(new StaticGuiElement('l', BORDER_ITEM, " "));

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
                "state", resident.getVillage().getEmpire().getStateTag()
        )));

        if (resident.hasPermission("empire.setaccess")) {
            gui.addElement(new StaticGuiElement('a', ItemUtils.toIcon(Material.IRON_DOOR),click -> {
                if (resident.hasPermission("empire.setaccess")) {
                    new AccessGUI().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("access",
                    "access", resident.getVillage().getEmpire().getAccess().getTag(),
                    "state", resident.getVillage().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.build")) {
            gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.DIAMOND_PICKAXE), click -> {
                if (resident.hasPermission("empire.build")) {
                    new BuildGui().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("build",
                    "build", resident.getVillage().getEmpire().getBuildState().getTag(),
                    "state", resident.getVillage().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.use")) {
            gui.addElement(new StaticGuiElement('u', ItemUtils.toIcon(Material.LEVER),click -> {
                if (resident.hasPermission("empire.use")) {
                    new UseGui().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("use",
                    "state", resident.getVillage().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.mode")) {
            gui.addElement(new StaticGuiElement('m', ItemUtils.toIcon(Material.REPEATER),click -> {
                if (resident.hasPermission("empire.mode")) {
                    new ModeGUI().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("mode",
                    "state", resident.getVillage().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.perms")) {
            gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.RED_BANNER), click -> {
                if (resident.hasPermission("empire.perms")) {
                    new PermsGUI((Player) click.getEvent().getWhoClicked()).open();
                }
                return true;
            }, getText("perms",
                    "state", resident.getVillage().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.perks")) {
            gui.addElement(new StaticGuiElement('p', ItemUtils.toIcon(Material.LINGERING_POTION), click -> {
                if (resident.hasPermission("empire.perks")) {
                    new PerksGUI(resident).open();
                }
                return true;
            }, getText("perks",
                    "state", resident.getVillage().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.shop")) {
            gui.addElement(new StaticGuiElement('s', ItemUtils.toIcon(Material.POTION), click -> {
                if (resident.hasPermission("empire.shop")) {
                    new ShopGUI(resident).open();
                }
                return true;
            }, getText("shop",
                    "state", resident.getVillage().getEmpire().getStateTag()
            )));
        }

        if (resident.hasPermission("empire.title")) {
            gui.addElement(new StaticGuiElement('t', ItemUtils.toIcon(Material.PAPER), click -> {
                if (resident.hasPermission("empire.title")) {
                    new TitleGUI(resident).open();
                }
                return true;
            }, getText(resident.hasPermission("empire.titles") ? "titles-edit" : "titles",
                    "state", resident.getVillage().getEmpire().getStateTag(),
                    "title", resident.getEmpireTitle()
            )));
        }

        Empire empire = resident.getVillage().getEmpire();
        gui.addElement(new StaticGuiElement('i', ItemUtils.toIcon(Material.BLUE_BED),
                getText("entry", empire.getReplacements(SettingsGUI.this))));

        gui.addElement(new StaticGuiElement('c', ItemUtils.toIcon(Material.BARRIER), click -> {
            click.getGui().close();
            return true;
        }, getText("close")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

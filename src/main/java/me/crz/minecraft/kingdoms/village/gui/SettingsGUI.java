package me.crz.minecraft.kingdoms.village.gui;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.command.village.VillageHomeCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSetHomeCommand;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageState;
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
        super("settings");
        this.resident = resident;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);

        gui.addElement(new StaticGuiElement('l', BORDER_ITEM, " "));

        gui.addElement(new StaticGuiElement('h', ItemUtils.toIcon(Material.BEACON),click -> {
            if (click.getType().isShiftClick()) {
                if (resident.hasPermission("village.sethome")) {
                    click.getGui().close();
                    ((VillageSetHomeCommand) CommandManager.getInstance().getCommand("village").getSubCommands().get("sethome")).run(resident, "village", "sethome");
                }
            } else if (click.getType().isLeftClick() && resident.hasVillage()) {
                click.getGui().close();
                ((VillageHomeCommand) CommandManager.getInstance().getCommand("village").getSubCommands().get("home")).run(resident, "village", "home");
            }
            return true;
        }, getText(resident.hasPermission("village.sethome") ? "sethome" : "home",
                "state", resident.getVillage().getStateTag()
        )));

        if (resident.hasPermission("village.setaccess")) {
            gui.addElement(new StaticGuiElement('a', ItemUtils.toIcon(Material.IRON_DOOR), click -> {
                if (resident.hasPermission("village.setaccess")) {
                    new AccessGUI().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("access",
                    "access", resident.getVillage().getAccess().getTag(),
                    "state", resident.getVillage().getStateTag()
            )));
        }

        if (resident.hasPermission("village.build")) {
            gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.DIAMOND_PICKAXE), click -> {
                if (resident.hasPermission("village.build")) {
                    new BuildGui().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("build",
                    "build", resident.getVillage().getBuildState().getTag(),
                    "state", resident.getVillage().getStateTag()
            )));
        }

        if (resident.hasPermission("village.use")) {
            gui.addElement(new StaticGuiElement('u', ItemUtils.toIcon(Material.LEVER), click -> {
                if (resident.hasPermission("village.use")) {
                    new UseGui().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("use",
                    "state", resident.getVillage().getStateTag()
            )));
        }

        if (resident.hasPermission("village.mode")) {
            gui.addElement(new StaticGuiElement('m', ItemUtils.toIcon(Material.REPEATER), click -> {
                if (resident.hasPermission("village.mode")) {
                    new ModeGUI().open((Player) click.getEvent().getWhoClicked());
                }
                return true;
            }, getText("mode",
                    "state", resident.getVillage().getStateTag()
            )));
        }

        if (resident.hasPermission("village.perms")) {
            gui.addElement(new StaticGuiElement('r', ItemUtils.toIcon(Material.RED_BANNER), click -> {
                if (resident.hasPermission("village.perms")) {
                    new PermsGUI((Player) click.getEvent().getWhoClicked()).open();
                }
                return true;
            }, getText("perms",
                    "state", resident.getVillage().getStateTag()
            )));
        }

        if (resident.hasPermission("village.perks")) {
            gui.addElement(new StaticGuiElement('p', ItemUtils.toIcon(Material.LINGERING_POTION), click -> {
                if (resident.hasPermission("village.perks")) {
                    new PerksGUI(resident).open();
                }
                return true;
            }, getText("perks",
                    "state", resident.getVillage().getStateTag()
            )));
        }

        if (resident.hasPermission("village.shop")) {
            gui.addElement(new StaticGuiElement('s', ItemUtils.toIcon(Material.POTION), click -> {
                if (resident.hasPermission("village.shop")) {
                    new ShopGUI(resident).open();
                }
                return true;
            }, getText("shop",
                    "state", resident.getVillage().getStateTag()
            )));
        }

        if (resident.hasPermission("village.title")) {
            gui.addElement(new StaticGuiElement('t', ItemUtils.toIcon(Material.PAPER), click -> {
                if (resident.hasPermission("village.title")) {
                    new TitleGUI(resident).open();
                }
                return true;
            }, getText(resident.hasPermission("village.titles") ? "titles-edit" : "titles",
                    "state", resident.getVillage().getStateTag(),
                    "title", resident.getVillageTitle()
            )));
        }

        Village village = resident.getVillage();
        gui.addElement(new StaticGuiElement('i', ItemUtils.toIcon(village.getState() == VillageState.KINGDOM ? Material.RED_BED : Material.LIME_BED),
                getText("entry", village.getReplacements(SettingsGUI.this))));

        gui.addElement(new StaticGuiElement('c', ItemUtils.toIcon(Material.BARRIER), click -> {
            click.getGui().close();
            return true;
        }, getText("close")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

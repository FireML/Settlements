package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AccessGUI extends SettlementsGUI {
    private final static String[] SETUP = {
            "         ",
            "         ",
            "ggggggggg",
            "ggggggggg",
            "         ",
            "    b    "
    };

    private final InventoryGui gui;

    public AccessGUI() {
        super("empire.access");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP);
        gui.setCloseAction(close -> false);
        gui.setFiller(BORDER_ITEM);

        GuiElementGroup group = new GuiElementGroup('g');

        for (LandAccess access : LandAccess.values()) {
            group.addElement(new DynamicGuiElement('g', (h) -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                if (resident.hasEmpire()) {
                    if (resident.getVillage().getEmpire().getAccess() == access) {
                        return new StaticGuiElement('g',
                                ItemUtils.toIcon(Material.LIME_WOOL),
                                getText("selected", "access", access.getTag()),
                                getText("access." + access.getKey(), "state", resident.getVillage().getEmpire().getStateTag())
                        );
                    }
                    return new StaticGuiElement('g',
                            ItemUtils.toIcon(Material.LIGHT_GRAY_WOOL),
                            click -> {
                                if (resident.hasEmpire() && resident.hasPermission("empire.setaccess")) {
                                    resident.getVillage().getEmpire().setAccess(access);
                                    Message.get("command.empire.setaccess.success").tell(resident, "access", access.getTag());
                                    DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire());
                                    gui.draw();
                                }
                                return true;
                            },
                            getText("available", "access", access.getTag()),
                            getText("access." + access.getKey(), "state", resident.getVillage().getEmpire().getStateTag())
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new StaticGuiElement('b', ItemUtils.toIcon(Material.BARRIER), click -> {
            new SettingsGUI((Player) click.getEvent().getWhoClicked()).open();
            return true;
        }, getText("back")));
    }

    public void open(final Player p) {
        gui.show(p);
    }
}

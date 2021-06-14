package me.crz.minecraft.kingdoms.empire.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AccessGUI extends KingdomsGUI {
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
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP);
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

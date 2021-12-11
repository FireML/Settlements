package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Message;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AccessGUI extends SettlementsGUI {
    private final InventoryGui gui;

    public AccessGUI(@NonNull String type, LandOwner<?, ?> landOwner) {
        super(type + ".access");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));

        GuiElementGroup group = new GuiElementGroup('a');
        group.setAlignment(getAlignment("a"));

        for (LandAccess access : LandAccess.values()) {
            group.addElement(new DynamicGuiElement('a', (h) -> {
                Resident resident = ResidentManager.getInstance().getResident((Player) h);
                if (landOwner.isMember(resident)) {
                    if (landOwner.getAccess() == access) {
                        return new StaticGuiElement('a',
                                getIcon("selected"),
                                getText("selected", "access", access.getTag()),
                                getText("access." + access.getKey(), "state", landOwner.getStateTag())
                        );
                    }
                    return new StaticGuiElement('a',
                            getIcon("available"),
                            click -> {
                                if (landOwner.hasPermission(resident, type + ".setaccess")) {
                                    landOwner.setAccess(access);
                                    Message.get("command." + type + ".setaccess.success").tell(resident, "access", access.getTag());
                                    landOwner.save();
                                    gui.draw();
                                }
                                return true;
                            },
                            getText("available", "access", access.getTag()),
                            getText("access." + access.getKey(), "state", landOwner.getStateTag())
                    );
                }
                return null;
            }));
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('n', getIcon("back"), true, getText("back")));
    }

    public void open(final Player p) {
        gui.show(p);
    }
}

package de.themoep.settlements.land.gui;

import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AccessGUI extends ConfigurableGui {

    public AccessGUI(Resident resident, @NonNull String type, LandOwner<?, ?> landOwner) {
        super(resident, type + ".access");

        GuiElementGroup group = createGroup('a');

        for (LandAccess access : LandAccess.values()) {
            group.addElement(new DynamicGuiElement('a', (h) -> {
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
                                if (landOwner.hasPermission(resident, "setaccess")) {
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
    }
}

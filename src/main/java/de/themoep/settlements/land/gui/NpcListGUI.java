package de.themoep.settlements.land.gui;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import org.bukkit.Location;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;

public class NpcListGUI extends ConfigurableGui {

    public NpcListGUI(Resident resident, LandOwner<?, ?> landOwner) {
        super(resident, landOwner.getKey() + ".npclist");

        SettlementsPlugin.getInstance().getCitizens().checkForNew(resident, landOwner);

        GuiElementGroup group = createGroup('n');

        NpcType.AppliesTo appliesTo = NpcType.AppliesTo.fromKey(landOwner.getKey());
        for (NpcType type : NpcType.values()) {
            if (!type.appliesTo(appliesTo)) {
                continue;
            }
            NpcData npcData = landOwner.getNpcData(type);
            if (!npcData.isAvailable()) {
                continue;
            }
            if (npcData != null && npcData.isUnlocked()) {
                group.addElement(new StaticGuiElement('n', getIcon(npcData), click -> {
                    if (landOwner.hasPermission(resident, "npc")) {
                        Location location = resident.getPlayer().getLocation();
                        OwnedLand l = LandManager.getInstance().getLand(location);
                        if (l != null && l.isOwnedBy(landOwner)) {
                            SettlementsPlugin.getInstance().getCitizens().createNpc(landOwner, type, location);
                        } else {
                            getError("not-a-claim").tell(resident);
                        }
                    } else {
                        getError("no-permission").tell(resident);
                    }
                    click.getGui().close();
                    return true;
                }, getText("entry.unlocked", "name", npcData.getTag())));
            } else {
                group.addElement(new StaticGuiElement('n', getIcon(npcData), getText("entry.locked", "name", npcData.getTag())));
            }
        }

        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

}

package de.themoep.settlements.missions;

import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElementGroup;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;

public class MissionGui extends ConfigurableGui {

    public MissionGui(Resident resident, MissionProvider<?> provider, MissionTaker<?> taker) {
        super(resident, provider.name() + ".missions");

        gui.addElement(new StaticGuiElement('i', getIcon("info"), getText("info",
                Mission.getReplacements(this, provider, taker))));

        GuiElementGroup group = new GuiElementGroup('m');
        group.setAlignment(getAlignment("m"));
        for (MissionData missionData : taker.getMissionData()) {
            group.addElement(new DynamicGuiElement('m', who -> {
                StaticGuiElement element = new StaticGuiElement('m', missionData.getMission().getIcon(), getText("entry", missionData.getReplacements(this, provider)));
                if (missionData.getMission().getType() == Mission.Type.TURN_IN && !missionData.isFinished()) {
                    element.setAction(click -> {
                        new TurnInGUI(resident, missionData).open();
                        return true;
                    });
                }
                return element;
            }));
        }
        gui.addElement(group);

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

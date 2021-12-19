package de.themoep.settlements.missions;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.empire.EmpireMission;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import org.apache.commons.lang.WordUtils;

import java.util.stream.Collectors;

public class MissionGui extends ConfigurableGui {

    public MissionGui(Resident resident, MissionProvider<?> provider, MissionTaker<?> taker) {
        super(resident, "missions");

        gui.addElement(new StaticGuiElement('i', getIcon("info"), getText("info",
                Mission.getReplacements(this, provider, taker))));

        Settlement settlement = (Settlement) provider;

        GuiElementGroup group = new GuiElementGroup('m');
        for (MissionData missionData : settlement.getMissionData()) {
            group.addElement(new DynamicGuiElement('m', who -> {
                StaticGuiElement element = new StaticGuiElement('m', missionData.getMission().getIcon(), getText("entry",
                        "title", missionData.getMission().getTag(),
                        "requirements", missionData.getMission().getRequirements().entrySet().stream().map(e ->
                                getText("requirement." + (missionData.getProgress(e.getKey()) < e.getValue() ? "todo" : "done"),
                                        "progress", String.valueOf(missionData.getProgress(e.getKey())),
                                        "required", String.valueOf(e.getValue()),
                                        "type", WordUtils.capitalizeFully(e.getKey().replace('_', ' '))
                        )).collect(Collectors.joining("\n")),
                        "action", missionData.isFinished()
                                ? getText("finished")
                                : (missionData.getMission() instanceof EmpireMission && ((EmpireMission) missionData.getMission()).onlyInEmpireWorld()
                                        ? getText("only-in-empire", "empire", resident.getSettlement().getEmpire().getName())
                                        : getText("types." + missionData.getMission().getType().getKey())
                                )
                ));
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

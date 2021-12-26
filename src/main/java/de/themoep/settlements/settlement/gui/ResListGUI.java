package de.themoep.settlements.settlement.gui;

import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ListGui;

public class ResListGUI extends SettlementsGUI {

    private final ListGui<Resident> gui;

    public ResListGUI(Resident resident, Settlement settlement) {
        super("settlement.reslist");
        gui = new ListGui<>(resident, name(), true, settlement::getResidents, (click, r) -> {
            new ResidentGUI(resident, r).open();
        }, r -> r.getReplacements(this));
    }

    public void open() {
        gui.open();
    }
}

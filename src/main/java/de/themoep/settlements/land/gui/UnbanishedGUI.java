package de.themoep.settlements.land.gui;

import de.themoep.settlements.SettlementsGUI;

import de.themoep.settlements.land.Unbanished;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;

public class UnbanishedGUI extends SettlementsGUI {

    private final ListGui<Unbanished> gui;

    public UnbanishedGUI(Resident resident, Settlement settlement) {
        super(settlement.getKey() + ".unbanished");

        gui = new ListGui<>(resident, name(), true,  settlement::getUnbanished, null, b -> {
            Targetable t = b.getTargetable();
            return new String[]{
                    "name", (t instanceof Settlement s ? s.getStateTag() + " ": "") + t.getName(),
                    "date", Utils.DATE_FORMAT.format(b.banishDate()),
                    "reason", b.reason(),
                    "banisher", b.banisherId() != Utils.ZERO_UUID ? ResidentManager.getInstance().getResidentName(b.banisherId()) : "[Auto]",
                    "unbanisher", ResidentManager.getInstance().getResidentName(b.unbanisherId()),
                    "unbanishdate", Utils.DATE_FORMAT.format(b.unbanishDate())
            };
        });
    }

    public void open() {
        gui.open();
    }
}

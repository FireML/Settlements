package de.themoep.settlements.land.gui;

import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.Unbanished;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;

public class UnbanishedGUI extends SettlementsGUI {

    private final ListGui<Unbanished> gui;

    public UnbanishedGUI(Resident resident, LandOwner<?, ?> landOwner) {
        super(landOwner.getKey() + ".unbanished");

        gui = new ListGui<>(resident, name(), true,  landOwner::getUnbanished, null, b -> {
            Targetable t = b.getTargetable();
            return new String[]{
                    "name", (t instanceof LandOwner ? ((LandOwner<?, ?>) t).getStateTag() + " ": "") + t.getName(),
                    "date", Utils.DATE_FORMAT.format(b.getBanishDate()),
                    "reason", b.getReason(),
                    "banisher", b.getBanisherId() != Utils.ZERO_UUID ? ResidentManager.getInstance().getResidentName(b.getBanisherId()) : "[Auto]",
                    "unbanisher", ResidentManager.getInstance().getResidentName(b.getUnbanisherId()),
                    "unbanishdate", Utils.DATE_FORMAT.format(b.getUnbanishDate())
            };
        });
    }

    public void open() {
        gui.open();
    }
}

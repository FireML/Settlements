package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.land.Banished;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;

public class BanishedGUI extends SettlementsGUI {

    private final ListGui<Banished> gui;

    public BanishedGUI(Resident resident, LandOwner<?, ?> landOwner) {
        super(landOwner.getKey() + ".banished");
        gui = new ListGui<>(resident, name(), true, landOwner::getBanished, (click, banished) -> {
            Targetable t = banished.getTargetable();
            if (landOwner.hasPermission(resident, "unbanish")
                    && landOwner.removeBanished(resident, t)) {
                landOwner.save();
                click.getGui().draw();
                Message.get("command." + landOwner.getKey() + ".unbanish.unbanished").tell(landOwner.getOnlineResidents(), "target", t.getName());
            }
        }, b -> {
            Targetable t = b.getTargetable();
            return new String[] {
                    "name", (t instanceof LandOwner<?, ?> owner ? owner.getStateTag() + " ": "") + t.getName(),
                    "date", Utils.DATE_FORMAT.format(b.getBanishDate()),
                    "reason", b.getReason(),
                    "banisher", b.getSenderId() != Utils.ZERO_UUID ? ResidentManager.getInstance().getResidentName(b.getSenderId()) : "[Auto]"
            };
        });

        gui.addElement(new StaticGuiElement('u', getIcon("unbanished"), click -> {
            new UnbanishedGUI(resident, landOwner).open();
            return true;
        }, getText("unbanished")));
    }

    public void open() {
        gui.open();
    }
}

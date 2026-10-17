package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.land.Banished;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;

public class BanishedGUI extends SettlementsGUI {

    private final ListGui<Banished> gui;

    public BanishedGUI(Resident resident, Settlement settlement) {
        super(settlement.getKey() + ".banished");
        gui = new ListGui<>(resident, name(), true, settlement::getBanished, (click, banished) -> {
            Targetable t = banished.getTargetable();
            if (settlement.hasPermission(resident, "unbanish")
                    && settlement.removeBanished(resident, t)) {
                settlement.save();
                click.getGui().draw();
                Message.get("command." + settlement.getKey() + ".unbanish.unbanished").tell(settlement.getOnlineResidents(), "target", t.getName());
            }
        }, b -> {
            Targetable t = b.getTargetable();
            return new String[] {
                    "name", (t instanceof Settlement owner ? owner.getStateTag() + " ": "") + t.getName(),
                    "date", Utils.DATE_FORMAT.format(b.banishDate()),
                    "reason", b.reason(),
                    "banisher", b.senderId() != Utils.ZERO_UUID ? ResidentManager.getInstance().getResidentName(b.senderId()) : "[Auto]"
            };
        });

        gui.addElement(new StaticGuiElement('u', getIcon("unbanished"), click -> {
            new UnbanishedGUI(resident, settlement).open();
            return true;
        }, getText("unbanished")));
    }

    public void open() {
        gui.open();
    }
}

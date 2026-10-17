package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.commandold.common.AllyCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AllyGUI extends ConfigurableGui implements AllyCommand {

    public AllyGUI(@NonNull Settlement settlement, Resident resident, Ally ally) {
        super(resident, settlement.getKey() + ".ally");

        gui.addElement(new StaticGuiElement('i', ally.getSettlement().getIcon(), getText("info." + ally.getType(), ally.getSettlement().getReplacements(this))));

        if (settlement.hasPermission(resident, "manageally")) {
            gui.addElement(new DynamicGuiElement('c', who -> new StaticGuiElement('c', getIcon("nearbyclaim"), click -> {
                if (settlement.hasPermission(resident, "allyclaim")) {
                    ally.setNearbyClaim(!ally.isNearbyClaim());
                    ally.save();

                    if (!ally.isNearbyClaim()) {
                        LandManager.getInstance().checkClaimDistance(resident.getSettlement(), ally.getSettlement());
                    }
                    Message.get("command." + settlement.getKey() + "." + (ally.isNearbyClaim() ? "enabled" : "disabled")).tell(resident,
                            "grace", String.valueOf(Config.Claiming.Close.Unclaim.GRACE_DAYS));
                }
                return true;
            }, getText("nearbyclaim",
                    "state", settlement.getStateTag(),
                    "current", getText(ally.isNearbyClaim() ? "enabled" : "disabled")
            ))));

            gui.addElement(new StaticGuiElement('r', getIcon("unally"), click -> {
                new ConfirmationGui(name() + ".unally", c -> {
                    if (!settlement.hasPermission(resident, "unally")) {
                        c.getGui().close();
                        return;
                    }
                    Settlement target = ally.getSettlement();

                    if (!settlement.isAlly(target)) {
                        c.getGui().close();
                        return;
                    }

                    Ally sourceAlly = settlement.removeAlly(target);
                    Ally targetAlly = target.removeAlly(target);
                    if (!settlement.isAllowAllyClaim() || !target.isAllowAllyClaim() || (sourceAlly != null && !sourceAlly.isNearbyClaim()) || (targetAlly != null && !targetAlly.isNearbyClaim())) {
                        LandManager.getInstance().checkClaimDistance(settlement, target);
                    }

                    if (sourceAlly != null) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().delete(settlement, sourceAlly));
                    } else {
                        settlement.save();
                        DatabaseManager.getInstance().save(settlement);
                    }
                    if (targetAlly != null) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().delete(target, targetAlly));
                    } else {
                        target.save();
                    }
                    Message.get("command." + settlement.getKey() + ".unally.success").tell(target, "name", settlement.getName(), "state", settlement.getStateTag());
                    Message.get("command." + settlement.getKey() + ".unally.success").tell(settlement, "name", target.getName(), "state", target.getStateTag());
                    c.getGui().close();
                }, c -> InventoryGui.goBack(c.getWhoClicked()), "name", ally.getSettlement().getName(), "state", ally.getSettlement().getStateTag()).open(resident.getPlayer());
                return true;
            }, getText("unally.entry")));

            // TODO: Per ally options
        }
    }
}

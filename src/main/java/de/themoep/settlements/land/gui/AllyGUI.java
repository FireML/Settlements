package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.common.AllyCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AllyGUI extends ConfigurableGui implements AllyCommand {

    public AllyGUI(@NonNull LandOwner landOwner, Resident resident, Ally ally) {
        super(resident, landOwner.getKey() + ".ally");

        gui.addElement(new StaticGuiElement('i', ally.getLandOwner().getIcon(), Utils.wrapLore(getText("info." + ally.getType(), ally.getLandOwner().getReplacements(this)))));

        if (landOwner.hasPermission(resident, "manageally")) {
            gui.addElement(new DynamicGuiElement('c', who -> new StaticGuiElement('c', getIcon("nearbyclaim"), click -> {
                if (landOwner.hasPermission(resident, "allyclaim")) {
                    ally.setNearbyClaim(!ally.isNearbyClaim());
                    ally.save();

                    if (!ally.isNearbyClaim()) {
                        LandManager.getInstance().checkClaimDistance(resident.getSettlement(), ally.getLandOwner());
                    }
                    Message.get("command." + landOwner.getKey() + "." + (ally.isNearbyClaim() ? "enabled" : "disabled")).tell(resident,
                            "grace", String.valueOf(Config.Claiming.Close.Unclaim.GRACE_DAYS));
                }
                return true;
            }, getText("nearbyclaim",
                    "state", landOwner.getStateTag(),
                    "current", getText(ally.isNearbyClaim() ? "enabled" : "disabled")
            ))));

            gui.addElement(new StaticGuiElement('r', getIcon("unally"), click -> {
                new ConfirmationGui(name() + ".unally", c -> {
                    if (!landOwner.hasPermission(resident, "unally")) {
                        c.getGui().close();
                        return;
                    }
                    LandOwner target = ally.getLandOwner();

                    if (!landOwner.isAlly(target)) {
                        c.getGui().close();
                        return;
                    }

                    Ally sourceAlly = landOwner.removeAlly(target);
                    Ally targetAlly = target.removeAlly(target);
                    if (!landOwner.isAllowAllyClaim() || !target.isAllowAllyClaim() || (sourceAlly != null && !sourceAlly.isNearbyClaim()) || (targetAlly != null && !targetAlly.isNearbyClaim())) {
                        LandManager.getInstance().checkClaimDistance(landOwner, target);
                    }

                    if (sourceAlly != null) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().delete(landOwner, sourceAlly));
                    } else {
                        DatabaseManager.getInstance().save(landOwner);
                    }
                    if (targetAlly != null) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().delete(target, targetAlly));
                    } else {
                        DatabaseManager.getInstance().save(target);
                    }
                    Message.get("command." + landOwner.getKey() + ".unally.success").tell(target, "name", landOwner.getName(), "state", landOwner.getStateTag());
                    Message.get("command." + landOwner.getKey() + ".unally.success").tell(landOwner, "name", target.getName(), "state", target.getStateTag());
                    c.getGui().close();
                }, c -> {
                    InventoryGui.goBack(c.getWhoClicked());
                }, "name", ally.getLandOwner().getName(), "state", ally.getLandOwner().getStateTag()).open(resident.getPlayer());
                return true;
            }, getText("unally.entry")));

            // TODO: Per ally options
        }
    }
}

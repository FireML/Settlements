package de.themoep.settlements.settlement.gui;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.world.NamedLocation;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.Utils.ItemUtils;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import uk.firedev.poleislib.libs.wesjd.anvilgui.AnvilGUI;

import java.util.Collection;

public class OutpostGUI extends ConfigurableGui {

    public OutpostGUI(Resident resident, Settlement settlement, NamedLocation outpost) {
        super(resident, "settlement.outpost", outpost.getReplacements());

        gui.addElement(new StaticGuiElement('i', getIcon("info"), getText("info", outpost.getReplacements())));

        gui.addElement(new StaticGuiElement('r', getIcon("rename"), click -> {
            if (settlement.hasPermission(resident, "outpost.rename")) {
                new AnvilGUI.Builder()
                        .itemLeft(ItemUtils.getHead(resident.getUUID(), resident.getName()))
                        .title(getText("rename.anvil.title", "name", outpost.getName()))
                        .text(getText("rename.anvil.placeholder", "name", outpost.getName()))
                        .onClose(player -> Utils.runLater(() -> new OutpostGUI(resident, settlement, outpost).open(), 1))
                        .onClick((slot, stateSnapshot) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT) {
                                return null;
                            }
                            Player player = stateSnapshot.getPlayer();
                            String text = stateSnapshot.getText();
                            if (settlement.hasPermission(resident, "outpost.rename")) {
                                String name = text.trim();

                                OwnedLand land = LandManager.getInstance().getLand(outpost.toChunkLocation());
                                if (land == null || !land.isOwnedBy(resident.getSettlement())) {
                                    if (resident.getSettlement().removeOutpostHome(outpost) != null) {
                                        resident.getSettlement().save();
                                    }
                                    getError("outpost-not-claimed").tell(resident, "name", outpost.getName(), "state", resident.getSettlement().getStateTag());
                                    Utils.runLater(() -> new OutpostGUI(resident, settlement, outpost), 1);
                                    return AnvilGUI.Response.close();
                                }

                                String oldName = outpost.getName();
                                resident.getSettlement().removeOutpostHome(outpost);
                                outpost.setName(name);
                                resident.getSettlement().setOutpostHome(outpost);
                                land.setOutpost(outpost.getName());
                                getMessage("success").tell(resident.getSettlement(),
                                        "player", resident.getName(),
                                        "oldname", oldName,
                                        "newname", outpost.getName(),
                                        "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                                        "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ()));
                                DatabaseManager.getInstance().saveSettlement(resident.getSettlement());

                                Utils.runLater(() -> new OutpostGUI(resident, settlement, outpost), 1);
                            } else {
                                getError("no-permission").tell(player);
                            }
                            return AnvilGUI.Response.close();
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("rename.button",
                "name", outpost.getName(),
                "action", getText(settlement.hasPermission(resident, "outpost.rename") ? "unlockables.unlocked.edit" : "unlockables.locked.permission")
        )));

        gui.addElement(new StaticGuiElement('h', getIcon("sethome"), click -> {
            if (!settlement.hasPermission(resident, "outpost.sethome")) {
                return true;
            }
            OwnedLand currentLand = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
            if (currentLand == null || !currentLand.isOwnedBy(resident.getSettlement())) {
                getError("not-an-outpost").tell(resident);
                return true;
            }
            OwnedLand outpostLand = null;
            if (currentLand.isOutpost()) {
                outpostLand = currentLand;
            } else {
                Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(currentLand, search -> {
                    if (search.isOutpost()) {
                        return LandManager.FilterResponse.RETURN;
                    } else if (search.containsHome()) {
                        return LandManager.FilterResponse.SKIP_ENTRY;
                    }
                    return LandManager.FilterResponse.CONTINUE;
                });
                for (OwnedLand l : connected) {
                    if (l.isOutpost()) {
                        outpostLand = l;
                        break;
                    }
                }
            }
            if (outpostLand == null) {
                getError("not-an-outpost").tell(resident);
                return true;
            }
            resident.getSettlement().setOutpostHome(outpostLand.getOutpostName(), resident.getPlayer().getLocation());
            if (outpostLand != currentLand) {
                currentLand.setOutpost(outpostLand.getOutpostName());
                outpostLand.setOutpost(null);
            }
            OwnedLand finalOutpostLand = outpostLand;
            resident.getSettlement().save();
            DatabaseManager.getInstance().saveLand(currentLand);
            if (finalOutpostLand != currentLand) {
                DatabaseManager.getInstance().saveLand(finalOutpostLand);
            }
            Message.get("command." + settlement.getKey() + ".outpost.sethome.success").tell(resident, "name", currentLand.getOutpostName());
            return true;
        }, getText("sethome",
                "name", outpost.getName(),
                "action", getText(settlement.hasPermission(resident, "outpost.sethome") ? "unlockables.unlocked.set" : "unlockables.locked.permission")
        )));

        gui.addElement(new StaticGuiElement('u', getIcon("unclaim"), click -> {
            if (settlement.hasPermission(resident, "outpost.unclaim")) {
                CommandManager.getInstance().getCommand(settlement.getKey()).matchSubCommand("outpost").matchSubCommand("unclaim")
                        .run(click.getWhoClicked(), settlement.getKey() + " outpost", "unclaim", outpost.getName());
            }
            return true;
        }, getText("unclaim",
                "name", outpost.getName(),
                "action", getText(settlement.hasPermission(resident, "outpost.unclaim") ? "unlockables.unlocked.unclaim" : "unlockables.locked.permission")
        )));

    }
}

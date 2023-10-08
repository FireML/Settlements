package de.themoep.settlements.land.gui;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.libs.themoep.inventorygui.InventoryGui;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import uk.firedev.poleislib.libs.wesjd.anvilgui.AnvilGUI;

public class ManageFinancesGUI extends ConfigurableGui {

    public ManageFinancesGUI(Resident resident, LandOwner<?, ?> landOwner) {
        super(resident, landOwner.getDbName() + ".manage-finances");

        gui.addElement(new StaticGuiElement('t', getIcon("tax"), click -> {
            if (landOwner.hasPermission(resident, "tax.set")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("tax"))
                        .title(getText("tax.anvil.title", "tax", Utils.CURRENCY_FORMAT.format(landOwner.getTax())))
                        .text(getText("tax.anvil.placeholder", "tax", String.valueOf(landOwner.getTax())))
                        .onClose(snapshot -> Utils.runLater(() -> InventoryGui.goBack(snapshot.getPlayer()), 1))
                        .onClick((slot, stateSnapshot) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT) {
                                return null;
                            }
                            String text = stateSnapshot.getText();
                            if (!landOwner.hasPermission(resident, "tax.set")) {
                                getError("no-permission").actionbar(resident);
                                Utils.runLater(() -> new ManageFinancesGUI(resident, landOwner).open(), 1);
                                return AnvilGUI.Response.close();
                            }
                            try {
                                long amount = Long.parseLong(text);
                                if (amount < 0) {
                                    return AnvilGUI.Response.text(Message.get("command." + landOwner.getKey() + ".tax.set.below-zero").getReplacedText(
                                            "amount", String.valueOf(amount)
                                    ));
                                }
                                if (Config.Settlement.MAX_TAX > -1 && amount > landOwner.getMaxTax()) {
                                    return AnvilGUI.Response.text(Message.get("command." + landOwner.getKey() + ".tax.set.above-max").getReplacedText(
                                            "amount", String.valueOf(amount),
                                            "max", String.valueOf(landOwner.getMaxTax())
                                    ));
                                }
                                landOwner.setTax(amount);
                                landOwner.save();
                                MessageUtils.notify(landOwner, landOwner.getResidents(), Message.get("command." + landOwner.getKey() + ".tax.set.success"),
                                        "state", resident.getSettlement().getStateTag(),
                                        "amount", String.valueOf(amount)
                                );
                                Utils.runLater(() -> new ManageFinancesGUI(resident, landOwner).open(), 1);
                                return AnvilGUI.Response.close();
                            } catch (NumberFormatException e) {
                                return AnvilGUI.Response.text(getError("not-a-number").getReplacedText("input", text));
                            }
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("tax.button",
                "action", getText(landOwner.hasPermission(resident, "tax.set") ? "unlockables.unlocked.edit" : "unlockables.locked.nopermission"),
                "tax", Utils.CURRENCY_FORMAT.format(landOwner.getTax()),
                "hour", Utils.HOUR_FORMAT.format(Config.Upkeep.HOUR * 60 * 60 * 1000),
                "cooldown", "" // TODO: Cooldown
        )));

        gui.addElement(new StaticGuiElement('a', getIcon("auctiontax"), click -> {
            if (landOwner.hasPermission(resident, "auctiontax.set")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("auctiontax"))
                        .title(getText("auctiontax.anvil.title", "auctiontax", Utils.CURRENCY_FORMAT.format(landOwner.getAuctionTax())))
                        .text(getText("auctiontax.anvil.placeholder", "auctiontax", String.valueOf(landOwner.getAuctionTax())))
                        .onClose(snapshot -> Utils.runLater(() -> InventoryGui.goBack(snapshot.getPlayer()), 1))
                        .onClick((slot, stateSnapshot) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT) {
                                return null;
                            }
                            String text = stateSnapshot.getText();
                            if (!landOwner.hasPermission(resident, "auctiontax.set")) {
                                getError("no-permission").actionbar(resident);
                                Utils.runLater(() -> new ManageFinancesGUI(resident, landOwner).open(), 1);
                                return AnvilGUI.Response.close();
                            }
                            try {
                                long amount = Long.parseLong(text);
                                if (amount < 0) {
                                    return AnvilGUI.Response.text(Message.get("command." + landOwner.getKey() + ".auctiontax.set.below-zero").getReplacedText(
                                            "amount", String.valueOf(amount)
                                    ));
                                }
                                if (Config.Settlement.MAX_TAX > -1 && amount > landOwner.getMaxTax()) {
                                    return AnvilGUI.Response.text(Message.get("command." + landOwner.getKey() + ".auctiontax.set.above-max").getReplacedText(
                                            "amount", String.valueOf(amount),
                                            "max", String.valueOf(landOwner.getMaxTax())
                                    ));
                                }
                                landOwner.setAuctionTax(amount);
                                landOwner.save();
                                MessageUtils.notify(landOwner, landOwner.getResidents(), Message.get("command." + landOwner.getKey() + ".auctiontax.set.success"),
                                        "state", resident.getSettlement().getStateTag(),
                                        "amount", Utils.toPercentage(resident.getSettlement().getAuctionTax())
                                );
                                Utils.runLater(() -> new ManageFinancesGUI(resident, landOwner).open(), 1);
                                return AnvilGUI.Response.close();
                            } catch (NumberFormatException e) {
                                return AnvilGUI.Response.text(getError("not-a-number").getReplacedText("input", text));
                            }
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("auctiontax.button",
                "action", getText(landOwner.hasPermission(resident, "auctiontax.set") ? "unlockables.unlocked.edit" : "unlockables.locked.nopermission"),
                "auctiontax", Utils.toPercentage(landOwner.getAuctionTax()),
                "cooldown", "" // TODO: Cooldown
        )));

        gui.addElement(new StaticGuiElement('d', getIcon("deposit"), click -> {
            if (landOwner.hasPermission(resident, "bank.deposit")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("deposit"))
                        .title(getText("deposit.anvil.title"))
                        .text(getText("deposit.anvil.placeholder"))
                        .onClose(snapshot -> Utils.runLater(() -> InventoryGui.goBack(snapshot.getPlayer()), 1))
                        .onClick((slot, stateSnapshot) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT) {
                                return null;
                            }
                            Player player = stateSnapshot.getPlayer();
                            String text = stateSnapshot.getText();
                            if (!landOwner.hasPermission(resident, "bank.deposit")) {
                                getError("no-permission").actionbar(resident);
                                Utils.runLater(() -> new ManageFinancesGUI(resident, landOwner).open(), 1);
                                return AnvilGUI.Response.close();
                            }
                            try {
                                long amount = Long.parseLong(text);
                                if (amount < 0) {
                                    return AnvilGUI.Response.text(getError("not-a-number").getReplacedText("input", text));
                                }
                                if (!SettlementsPlugin.getInstance().getEconomy().has(player, amount)) {
                                    return AnvilGUI.Response.text(getError("not-enough-money").getReplacedText(
                                            "amount", String.valueOf(amount)
                                    ));
                                }
                                EconomyResponse r2 = SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(player, (double) amount);
                                if (!r2.transactionSuccess()) {
                                    return AnvilGUI.Response.text("&cAn error occurred, please tell an admin!");
                                }
                                landOwner.addToBank(amount);
                                landOwner.save();
                                DatabaseManager.getInstance().logDeposit(landOwner, resident, amount);
                                Message.get("command." + landOwner.getKey() + ".bank.deposit.deposited").tell(landOwner,
                                        "state", landOwner.getStateTag(),
                                        "rank", landOwner.getRank(resident).getTag(),
                                        "title", resident.getDisplayTitle(),
                                        "displaytitle", resident.getDisplayTitle(),
                                        "name", resident.getName(),
                                        "amount", Utils.CURRENCY_FORMAT.format(amount)
                                );
                                Utils.runLater(() -> new ManageFinancesGUI(resident, landOwner).open(), 1);
                                return AnvilGUI.Response.close();
                            } catch (NumberFormatException e) {
                                return AnvilGUI.Response.text(getError("not-a-number").getReplacedText("input", text));
                            }
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("deposit.button",
                "action", getText(landOwner.hasPermission(resident, "bank.deposit") ? "unlockables.unlocked.deposit" : "unlockables.locked.nopermission")
        )));

        gui.addElement(new StaticGuiElement('w', getIcon("withdraw"), click -> {
            if (landOwner.hasPermission(resident, "bank.withdraw")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("withdraw"))
                        .title(getText("withdraw.anvil.title"))
                        .text(getText("withdraw.anvil.placeholder"))
                        .onClose(snapshot -> Utils.runLater(() -> InventoryGui.goBack(snapshot.getPlayer()), 1))
                        .onClick((slot, stateSnapshot) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT) {
                                return null;
                            }
                            Player player = stateSnapshot.getPlayer();
                            String text = stateSnapshot.getText();
                            if (!landOwner.hasPermission(resident, "bank.withdraw")) {
                                getError("no-permission").actionbar(resident);
                                Utils.runLater(() -> new ManageFinancesGUI(resident, landOwner).open(), 1);
                                return AnvilGUI.Response.close();
                            }
                            try {
                                long amount = Long.parseLong(text);
                                if (amount < 0) {
                                    return AnvilGUI.Response.text(getError("not-a-number").getReplacedText("input", text));
                                }
                                if (landOwner.removeFromBank(amount)) {
                                    EconomyResponse r2 = SettlementsPlugin.getInstance().getEconomy().depositPlayer(player, (double) amount);
                                    if (!r2.transactionSuccess()) {
                                        landOwner.addToBank(amount);
                                        return AnvilGUI.Response.text("&cAn error occurred, please tell an admin!");
                                    }
                                } else {
                                    return AnvilGUI.Response.text(getError("not-enough-money").getReplacedText(
                                            "amount", String.valueOf(amount)
                                    ));
                                }
                                Message.get("command." + landOwner.getKey() + ".bank.withdraw.withdrawn").tell(resident.getSettlement(),
                                        "state", landOwner.getStateTag(),
                                        "rank", landOwner.getRank(resident).getTag(),
                                        "title", resident.getDisplayTitle(),
                                        "displaytitle", resident.getDisplayTitle(),
                                        "name", resident.getName(),
                                        "amount", Utils.CURRENCY_FORMAT.format(amount)
                                );
                                DatabaseManager.getInstance().logWithdraw(landOwner, resident, amount);
                                landOwner.save();
                                Utils.runLater(() -> new ManageFinancesGUI(resident, landOwner).open(), 1);
                                return AnvilGUI.Response.close();
                            } catch (NumberFormatException e) {
                                return AnvilGUI.Response.text(getError("not-a-number").getReplacedText("input", text));
                            }
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        }, getText("withdraw.button",
                "action", getText(landOwner.hasPermission(resident, "bank.withdraw") ? "unlockables.unlocked.withdraw" : "unlockables.locked.nopermission")
        )));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

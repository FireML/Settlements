package de.themoep.settlements.land.gui;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.libs.themoep.inventorygui.InventoryGui;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import uk.firedev.poleislib.libs.wesjd.anvilgui.AnvilGUI;

import java.util.List;

public class BankGUI extends ConfigurableGui {

    public BankGUI(Resident resident, LandOwner<?, ?> landOwner) {
        super(resident, landOwner.getKey() + ".bank");

        gui.addElement(new StaticGuiElement('d', getIcon("donations"), click -> {
            if (landOwner.hasUnlocked("donation") || landOwner.hasPermission(resident, "bank.deposit")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("donations"))
                        .title(getText("donations-anvil.title"))
                        .text(getText("donations-anvil.placeholder"))
                        .onClose(snapshot -> Utils.runLater(() -> InventoryGui.goBack(snapshot.getPlayer()), 1))
                        .onClick((slot, stateSnapshot) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT) {
                                return null;
                            }
                            Player player = stateSnapshot.getPlayer();
                            String text = stateSnapshot.getText();
                            if (!landOwner.hasUnlocked("donation") && !landOwner.hasPermission(resident, "bank.deposit")) {
                                getError("no-permission").actionbar(resident);
                                Utils.runLater(() -> new BankGUI(resident, landOwner).open(), 1);
                                return List.of(
                                        AnvilGUI.ResponseAction.close()
                                );
                            }
                            try {
                                long amount = Long.parseLong(text);
                                if (amount < 0) {
                                    return List.of(
                                            AnvilGUI.ResponseAction.replaceInputText(getError("not-a-number").getReplacedText("input", text))
                                    );
                                }
                                if (!SettlementsPlugin.getInstance().getEconomy().has(player, amount)) {
                                    return List.of(
                                            AnvilGUI.ResponseAction.replaceInputText(getError("not-enough-money").getReplacedText("amount", String.valueOf(amount)))
                                    );
                                }
                                EconomyResponse r2 = SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(player, (double) amount);
                                if (!r2.transactionSuccess()) {
                                    return List.of(
                                            AnvilGUI.ResponseAction.replaceInputText("&cAn error occurred, please tell an admin!")
                                    );
                                }
                                landOwner.addToBank(amount);
                                landOwner.save();
                                Message.get("command." + landOwner.getKey() + (landOwner.isMember(resident) ? ".bank.deposit.deposited" : ".donate.donated")).tell(landOwner,
                                        "state", landOwner.getStateTag(),
                                        "rank", landOwner.getRank(resident).getTag(),
                                        "title", resident.getDisplayTitle(),
                                        "displaytitle", resident.getDisplayTitle(),
                                        "name", resident.getName(),
                                        "amount", Utils.CURRENCY_FORMAT.format(amount)
                                );
                                if (landOwner.isMember(resident)) {
                                    DatabaseManager.getInstance().logDeposit(landOwner, resident, amount);
                                    Utils.runLater(() -> new BankGUI(resident, landOwner).open(), 1);
                                } else {
                                    Utils.runAsync(() -> DatabaseManager.getInstance().log(landOwner, resident, "donate", "cash", amount));
                                    Message.get("command." + landOwner.getKey() + ".donate.success").tell(resident,
                                            "amount", Utils.CURRENCY_FORMAT.format(amount),
                                            "state", landOwner.getStateTag(),
                                            "name", landOwner.getName()
                                    );
                                }
                                return List.of(
                                        AnvilGUI.ResponseAction.close()
                                );
                            } catch (NumberFormatException e) {
                                return List.of(
                                        AnvilGUI.ResponseAction.replaceInputText(getError("not-a-number").getReplacedText("input", text))
                                );
                            }
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open(resident.getPlayer());
            }
            return true;
        },getText(landOwner.hasUnlocked("command.donate") ? "donations" : "donations-locked",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasUnlocked("command.donate")
                        || landOwner.hasPermission(resident, "bank.deposit")
                        ? getText("unlockables.unlocked.deposit")
                        : getText("unlockables.locked.permission"))));

        // Only donations are allowed to be used by everyone
        if (landOwner.isMember(resident)) {
            gui.addElement(new StaticGuiElement('a', getIcon("balance"), getText("balance",
                    "balance", Utils.CURRENCY_FORMAT.format(landOwner.getBank()),
                    "state", landOwner.getStateTag())));

            /* Remove for now while not working
            gui.addElement(new StaticGuiElement('s', getIcon("storage"), click -> {
                // TODO: Storage GUI
                return true;
            }, getText("storage",
                    "state", landOwner.getStateTag(),
                    "action", landOwner.hasUnlocked("storage") ? getText("unlockables.unlocked.open") : getText("unlockables.locked.npc"))));
            */

            gui.addElement(new StaticGuiElement('h', getIcon("history"), click -> {
                // TODO: GUI?
                CommandManager.getInstance().getCommand(landOwner.getKey())
                        .execute(click.getWhoClicked(), null, landOwner.getKey(), "money", "history");
                click.getGui().close();
                return true;
            }, getText("history",
                    "state", landOwner.getStateTag(),
                    "action", landOwner.hasUnlocked("donation") ? getText("unlockables.unlocked.view") : getText("unlockables.locked.npc"))));

            long upkeep = landOwner.getUpkeep();
            long taxIncome = landOwner.getTaxIncome();

            long payPerDay = upkeep - taxIncome;

            int days = -1;
            if (payPerDay > 0) {
                days = (int) (landOwner.getBank() / payPerDay);
            }

            gui.addElement(new StaticGuiElement('u', getIcon("upkeep"), getText("upkeep",
                    "upkeepperclaim", Utils.CURRENCY_FORMAT.format(landOwner.getClaimUpkeep(resident.getPlayer().getWorld().getName())),
                    "upkeep", Utils.CURRENCY_FORMAT.format(upkeep),
                    "afford", days > -1 ? String.valueOf(days) : Message.get("command." + landOwner.getKey() + ".upkeep.afford-infinite").getText(),
                    "time", UtilTime.format(Utils.timeTillNextUpkeep()),
                    "state", landOwner.getStateTag())));

            gui.addElement(new StaticGuiElement('t', getIcon("taxes"), getText("taxes",
                    "tax", Utils.CURRENCY_FORMAT.format(landOwner.getTax()),
                    "auctiontax", Utils.toPercentage(landOwner.getAuctionTax()),
                    "state", landOwner.getStateTag())));

            gui.addElement(new StaticGuiElement('m', getIcon("manage-finances"), click -> {
                if (landOwner.hasPermission(resident, "tax.set")
                        || landOwner.hasPermission(resident, "auctiontax.set")
                        || landOwner.hasPermission(resident, "bank.withdraw")
                        || landOwner.hasPermission(resident, "bank.deposit")) {
                    new ManageFinancesGUI(resident, landOwner).open();
                } else {
                    getError("no-permission").tell(resident);
                }
                return true;
            }, getText("manage-finances",
                    "tax", Utils.CURRENCY_FORMAT.format(landOwner.getTax()),
                    "acutiontax", Utils.CURRENCY_FORMAT.format(landOwner.getAuctionTax()),
                    "action", getText(landOwner.hasPermission(resident, "tax.set")
                            || landOwner.hasPermission(resident, "auctiontax.set")
                            || landOwner.hasPermission(resident, "bank.withdraw")
                            || landOwner.hasPermission(resident, "bank.deposit")
                            ? "unlockables.unlocked.options" : "unlockables.locked.nopermission"),
                    "state", landOwner.getStateTag())));
        }
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ManageTaxesGUI extends SettlementsGUI {
    private final Resident resident;
    private final InventoryGui gui;

    public ManageTaxesGUI(Resident resident, LandOwner<?, ?> landOwner) {
        super(landOwner.getDbName() + ".manage-taxes");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        gui.addElement(new StaticGuiElement('t', getIcon("tax"), click -> {
            if (landOwner.hasPermission(resident, "tax.set")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("tax"))
                        .title(getText("tax.anvil.title", "tax", Utils.CURRENCY_FORMAT.format(landOwner.getTax())))
                        .text(getText("tax.anvil.placeholder", "tax", String.valueOf(landOwner.getTax())))
                        .onClose(player -> Utils.runLater(() -> InventoryGui.goBack(player), 1))
                        .onComplete((player, text) -> {
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
                                Message.get("command." + landOwner.getKey() + ".tax.set.success").tell(resident.getSettlement(),
                                        "state", resident.getSettlement().getStateTag(),
                                        "amount", String.valueOf(amount)
                                );
                                landOwner.setTax(amount);
                                landOwner.save();
                                Utils.runLater(() -> InventoryGui.goBack(player), 1);
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
                        .onClose(player -> Utils.runLater(() -> InventoryGui.goBack(player), 1))
                        .onComplete((player, text) -> {
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
                                Message.get("command." + landOwner.getKey() + ".auctiontax.set.success").tell(resident.getSettlement(),
                                        "state", resident.getSettlement().getStateTag(),
                                        "amount", String.valueOf(amount)
                                );
                                landOwner.setAuctionTax(amount);
                                landOwner.save();
                                Utils.runLater(() -> InventoryGui.goBack(player), 1);
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
                "auctiontax", Utils.CURRENCY_FORMAT.format(landOwner.getAuctionTax())),
                "cooldown", "" // TODO: Cooldown
        ));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

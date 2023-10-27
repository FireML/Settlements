package de.themoep.settlements.settlement.gui;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.land.gui.RankSelectGUI;
import de.themoep.settlements.land.gui.TitleGUI;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.*;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.Utils.ItemUtils;
import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiStateElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.InventoryGui;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import uk.firedev.poleislib.libs.wesjd.anvilgui.AnvilGUI;

import java.util.List;

public class ResidentGUI extends ConfigurableGui {

    public ResidentGUI(Resident sender, Resident resident) {
        super(sender, "settlement.resident", resident.getReplacements());

        gui.addElement(new DynamicGuiElement('p', who -> new StaticGuiElement('p',
                ItemUtils.getHead(resident.getUUID(), resident.getName()),
                getText("info", resident.getReplacements()))));

        gui.addElement(new DynamicGuiElement('a', who -> new StaticGuiElement('a', getIcon("accepted"), getText("accepted.entry",
                "state", resident.getSettlement().getTag(),
                "accepted", resident.getSettlementRank() == resident.getSettlement().getDefaultRank()
                        ? getText("accepted.not-accepted") : getText("accepted.accepted")
        ))));

        gui.addElement(new GuiStateElement('e', () -> resident.getTaxDebtAction().getKey(),
                new GuiStateElement.State(click -> {
                    if (resident.getSettlement().hasPermission(sender, "settaxevasion")) {
                        resident.setTaxDebtAction(TaxDebtAction.NONE);
                        resident.save();
                    }
                }, TaxDebtAction.NONE.getKey(), getIcon("evasion"),
                        getText("tax-evasion",
                                "taxevasion", TaxDebtAction.NONE.getTag(),
                                "action", resident.getSettlement().hasPermission(sender, "settaxevasion") ? getText("unlockables.unlocked.change") : getText("no-permission")
                        )),
                new GuiStateElement.State(click -> {
                    if (resident.getSettlement().hasPermission(sender, "settaxevasion")) {
                        resident.setTaxDebtAction(TaxDebtAction.KICK);
                        resident.save();
                    }
                }, TaxDebtAction.KICK.getKey(), getIcon("evasion"),
                        getText("tax-evasion",
                                "taxevasion", TaxDebtAction.KICK.getTag(),
                                "action", resident.getSettlement().hasPermission(sender, "settaxevasion") ? getText("unlockables.unlocked.change") : getText("no-permission")
                        ))));

        gui.addElement(new GuiStateElement('x', () -> String.valueOf(resident.isTaxExempted()),
                new GuiStateElement.State(click -> {
                    if (resident.getSettlement().hasPermission(sender, "settaxexempt")) {
                        resident.setTaxExempted(true);
                        resident.save();
                    }
                }, Boolean.TRUE.toString(), getIcon("taxes"), getText("taxes.entry",
                        "donated", Utils.CURRENCY_FORMAT.format(resident.getDonated()),
                        "taxpaid", Utils.CURRENCY_FORMAT.format(resident.getTaxPaid()),
                        "exempted", getText("taxes.exempted"),
                        "action", resident.getSettlement().hasPermission(sender,"settaxexempt") ? getText("unlockables.unlocked.change") : getText("no-permission")
                )),
                new GuiStateElement.State(click -> {
                    if (resident.getSettlement().hasPermission(sender, "settaxexempt")) {
                        resident.setTaxExempted(false);
                        resident.save();
                    }
                }, Boolean.FALSE.toString(), getIcon("taxes"), getText("taxes.entry",
                        "donated", Utils.CURRENCY_FORMAT.format(resident.getDonated()),
                        "taxpaid", Utils.CURRENCY_FORMAT.format(resident.getTaxPaid()),
                        "exempted", getText("taxes.paying"),
                        "action", resident.getSettlement().hasPermission(sender,"settaxexempt") ? getText("unlockables.unlocked.change") : getText("no-permission")
                ))));

        gui.addElement(new DynamicGuiElement('t', who -> new StaticGuiElement('t', getIcon("settitle"), click -> {
            if (resident.getSettlement().hasPermission(sender, "settitle") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                new TitleGUI<>("settlement", sender, resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("settitle",
                "title", resident.getSettlementTitle(),
                "action", resident.getSettlement().hasPermission(sender, "settitle") ? getText("unlockables.unlocked.change") : getText("no-permission")
        ))));

        gui.addElement(new DynamicGuiElement('r', who -> new StaticGuiElement('r', getIcon("rank"), click -> {
            if (resident.getSettlement().hasPermission(sender, "setrank") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                new RankSelectGUI<>("settlement.setrank", sender, resident, resident.getSettlement(), rank -> {
                    resident.setSettlementRank(rank);
                    resident.save();
                    InventoryGui.goBack(click.getWhoClicked());
                }, rank -> {
                    if (resident.getSettlement().hasPermission(sender, "transfer")) {
                        new ConfirmationGui("settlement.setrank.transfer", c -> {
                            if (resident.getSettlement().hasPermission(sender, "transfer")) {
                                resident.getSettlement().transfer(resident);
                                MessageUtils.notify(resident.getSettlement(), resident.getSettlement().getResidents(), Message.get("command.settlement.tranfer.success"),
                                        "rank", SettlementRank.getChief().getTag(resident.getSettlement()),
                                        "setter", sender.getName(),
                                        "target", resident.getName()
                                );
                            } else {
                                getError("no-permission").tell(sender);
                            }
                            c.getGui().close();
                        }, c -> InventoryGui.goBack(c.getWhoClicked()),
                                "target", resident.getName(),
                                "state", resident.getSettlement().getStateTag(),
                                "name", resident.getSettlement().getName()).open((Player) click.getWhoClicked());
                    }
                }).open();
            }
            return true;
        }, getText("rank",
                "rank", resident.getSettlementRank().getTag(),
                "action", getText(resident.getSettlement().hasPermission(sender, "setrank")
                        ? sender.getSettlementRank().above(resident.getSettlementRank())
                        ? "unlockables.unlocked.change"
                        : "unlockables.locked.rank"
                        : "no-permission")
        ))));

        gui.addElement(new StaticGuiElement('l', getIcon("eviction"), click -> {
            if (resident.getSettlement().hasPermission(sender, "kicknotice") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                for (MailProvider mailProvider : SettlementsPlugin.getInstance().getMailProviders()) {
                    mailProvider.sendMail(Message.get("eviction.letter.sender").getReplacedText(
                                    "state", resident.getSettlement().getStateTag(),
                                    "name", resident.getSettlement().getName(),
                                    "chieftitle", resident.getSettlement().getChief().getSettlementTitle(),
                                    "chiefname", resident.getSettlement().getChief().getName()
                            ), resident,
                            Message.get("eviction.letter.text").getReplacedText(
                                    "state", resident.getSettlement().getStateTag(),
                                    "name", resident.getSettlement().getName()
                            ));
                }
                Message.get("command.settlement.kicknotice.success").tell(sender, "name", resident.getName());
                Message.get("command.settlement.kicknotice.success").actionbar(sender, "name", resident.getName());
            }
            return true;
        }, getText("eviction",
                "action", getText(sender.hasPermission("settlement.kicknotice")
                        ? sender.getSettlementRank().above(resident.getSettlementRank())
                        ? "unlockables.unlocked.send"
                        : "unlockables.locked.rank"
                        : "unlockables.locked.nopermission")
        )));

        gui.addElement(new StaticGuiElement('k', getIcon("kick"), click -> {
            new ConfirmationGui(name() + ".kick", c -> {
                if (resident.getSettlement().hasPermission(sender, "settlement.kick") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                    Message.get("command.settlement.kick.kicked-alert").tell(resident.getSettlement().getOnlineResidents(), "player", resident.getName());
                    InviteManager.getInstance().removeInvite(resident, resident.getSettlement());
                    resident.leaveSettlement();
                }
                new ResListGUI(sender, sender.getSettlement()).open();
            }, c -> InventoryGui.goBack(c.getWhoClicked()), "player", resident.getName(), "state", resident.getSettlement().getStateTag())
                    .open((Player) click.getWhoClicked());
            return true;
        }, getText("kick.entry",
                "player", resident.getName(),
                "state", resident.getSettlement().getStateTag(),
                "action", getText(resident.getSettlement().hasPermission(sender, "settlement.kick")
                        ? sender.getSettlementRank().above(resident.getSettlementRank())
                        ? "unlockables.unlocked.kick"
                        : "unlockables.locked.rank"
                        : "unlockables.locked.nopermission")
        )));

        gui.addElement(new StaticGuiElement('s', getIcon("banish"), click -> {
            Settlement settlement = resident.getSettlement();
            new AnvilGUI.Builder()
                    .itemLeft(ItemUtils.getHead(resident.getUUID(), resident.getName()))
                    .title(getText("banish.anvil.title", "player", resident.getName()))
                    .text(getText("banish.anvil.placeholder", "player", resident.getName()))
                    .onClose(player -> Utils.runLater(() -> new ResidentGUI(sender, resident).open(), 1))
                    .onClick((slot, stateSnapshot) -> {
                        if (slot != AnvilGUI.Slot.OUTPUT) {
                            return null;
                        }
                        Player player = stateSnapshot.getPlayer();
                        String text = stateSnapshot.getText();
                        if (settlement.hasPermission(sender, "banish") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                            String reason = text.trim();
                            getMessage("command.settlement.banish.banished").tell(resident.getSettlement().getOnlineResidents(), "name", resident.getName(), "reason", reason);
                            InviteManager.getInstance().removeInvite(resident, resident.getSettlement());
                            resident.getSettlement().addBanished(sender, resident, reason);
                            resident.leaveSettlement();

                            Utils.runLater(() -> new ResListGUI(sender, settlement).open(), 1);
                        } else {
                            getError("no-permission").tell(player);
                        }
                        return List.of(
                                AnvilGUI.ResponseAction.close()
                        );
                    })
                    .plugin(SettlementsPlugin.getInstance())
                    .open(sender.getPlayer());
            return true;
        }, getText("banish.entry",
                "action", resident.getSettlement().hasPermission(sender, "banish")
                        ? sender.getSettlementRank().above(resident.getSettlementRank())
                                ? getText("unlockables.unlocked.banish")
                                : getText("unlockables.locked.rank")
                        : getText("unlockables.locked.permission"),
                "player", resident.getName(),
                "state", resident.getSettlement().getStateTag()
        )));
    }
}

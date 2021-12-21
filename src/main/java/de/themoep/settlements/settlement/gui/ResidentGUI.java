package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.land.gui.RankSelectGUI;
import de.themoep.settlements.land.gui.TitleGUI;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;

public class ResidentGUI extends ConfigurableGui {

    public ResidentGUI(Resident sender, Resident resident) {
        super(sender, "settlement.resident", resident.getReplacements());
        String[] replacements = resident.getReplacements();

        gui.addElement(new StaticGuiElement('p',
                ItemUtils.getHead(resident.getUUID(), resident.getName()),
                getText("info", replacements)));

        gui.addElement(new StaticGuiElement('a', getIcon("accepted"), getText("accepted.entry",
                "state", resident.getSettlement().getTag(),
                "accepted", resident.getSettlementRank() == resident.getSettlement().getDefaultRank()
                        ? getText("accepted.not-accepted") : getText("accepted.accepted")
        )));

        gui.addElement(new GuiStateElement('e', () -> resident.getTaxDebtAction().getKey(),
                new GuiStateElement.State(click -> {
                    if (sender.hasPermission("settlement.settaxevasion")) {
                        resident.setTaxDebtAction(TaxDebtAction.KICK);
                        resident.save();
                    }
                }, TaxDebtAction.NONE.getKey(), getIcon("evasion"),
                        getText("tax-evasion",
                                "taxevasion", resident.getTaxDebtAction().getTag(),
                                "action", resident.hasPermission("settlement.settaxevasion") ? getText("unlockables.unlocked.change") : getText("no-permission")
                        )),
                new GuiStateElement.State(click -> {
                    if (sender.hasPermission("settlement.settaxevasion")) {
                        resident.setTaxDebtAction(TaxDebtAction.NONE);
                        resident.save();
                    }
                }, TaxDebtAction.KICK.getKey(), getIcon("evasion"),
                        getText("tax-evasion",
                                "taxevasion", resident.getTaxDebtAction().getTag(),
                                "action", resident.hasPermission("settlement.settaxevasion") ? getText("unlockables.unlocked.change") : getText("no-permission")
                        ))));

        gui.addElement(new GuiStateElement('x', () -> String.valueOf(resident.isTaxExempted()),
                new GuiStateElement.State(click -> {
                    if (sender.hasPermission("settlement.settaxexempt")) {
                        resident.setTaxExempted(true);
                        resident.save();
                    }
                }, Boolean.TRUE.toString(), getIcon("taxes"), getText("taxes.entry",
                        "donated", Utils.CURRENCY_FORMAT.format(resident.getDonated()),
                        "taxpaid", Utils.CURRENCY_FORMAT.format(resident.getTaxPaid()),
                        "exempted", getText("taxes.exempted"),
                        "action", resident.hasPermission("settlement.settaxexempt") ? getText("unlockables.unlocked.change") : getText("no-permission")
                )),
                new GuiStateElement.State(click -> {
                    if (sender.hasPermission("settlement.settaxexempt")) {
                        resident.setTaxExempted(false);
                        resident.save();
                    }
                }, Boolean.FALSE.toString(), getIcon("taxes"), getText("taxes.entry",
                        "donated", Utils.CURRENCY_FORMAT.format(resident.getDonated()),
                        "taxpaid", Utils.CURRENCY_FORMAT.format(resident.getTaxPaid()),
                        "exempted", getText("taxes.paying"),
                        "action", resident.hasPermission("settlement.settaxexempt") ? getText("unlockables.unlocked.change") : getText("no-permission")
                ))));

        gui.addElement(new StaticGuiElement('t', getIcon("settitle"), click -> {
            if (sender.hasPermission("settlement.settitle") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                new TitleGUI<>("settlement", sender, resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("settitle",
                "title", resident.getSettlementTitle(),
                "action", resident.hasPermission("settlement.settitle") ? getText("unlockables.unlocked.change") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('r', getIcon("rank"), click -> {
            if (sender.hasPermission("settlement.setrank") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                new RankSelectGUI<>("settlement.setrank", sender, resident.getSettlement(), rank -> {
                    resident.setSettlementRank(rank);
                    resident.save();
                    InventoryGui.goBack(click.getWhoClicked());
                }, rank -> {
                    new ConfirmationGui(name() + "settlement.setrank.transfer", c -> {
                        resident.getSettlement().transfer(resident);
                        Message.get("command.settlement.tranfer.success").tell(resident.getSettlement().getOnlineResidents(),
                                "rank", SettlementRank.getChief().getTag(resident.getSettlement()),
                                "setter", sender.getName(),
                                "target", resident.getName()
                        );
                        c.getGui().close();
                    }, c -> InventoryGui.goBack(c.getWhoClicked()),
                            "player", resident.getName(),
                            "state", resident.getSettlement().getStateTag(),
                            "settlement", resident.getSettlement().getName());
                }).open();
            }
            return true;
        }, getText("rank",
                "rank", resident.getSettlementRank().getTag(),
                "action", resident.hasPermission("settlement.setrank") ? getText("unlockables.unlocked.change") : getText("no-permission")
        )));

        if (sender.hasPermission("settlement.kicknotice")) {
            gui.addElement(new StaticGuiElement('l', getIcon("eviction"), click -> {
                if (resident.getSettlement().hasPermission(sender, "kicknotice")) {
                    for (MailProvider mailProvider : SettlementsPlugin.getInstance().getMailProviders()) {
                        mailProvider.sendMail(Message.get("eviction.sender").getReplacedText(
                                        "state", resident.getSettlement().getStateTag(),
                                        "name", resident.getSettlement().getName(),
                                        "chieftitle", resident.getSettlement().getChief().getSettlementTitle(),
                                        "chiefname", resident.getSettlement().getChief().getName()
                                ), resident,
                                Message.get("eviction.letter").getReplacedText(
                                        "state", resident.getSettlement().getStateTag(),
                                        "name", resident.getSettlement().getName()
                                ));
                    }
                    Message.get("command.settlement.kicknotice.success").tell(sender, "name", resident.getName());
                    Message.get("command.settlement.kicknotice.success").actionbar(sender, "name", resident.getName());
                }
                return true;
            }, getText("eviction")));
        }

        if (sender.hasPermission("settlement.kick")) {
            gui.addElement(new StaticGuiElement('k', getIcon("kick"), click -> {
                new ConfirmationGui(name() + ".kick", c -> {
                    if (resident.getSettlement().hasPermission(sender, "settlement.kick")) {
                        Message.get("command.settlement.kick.kicked-alert").tell(resident.getSettlement().getOnlineResidents(), "player", resident.getName());
                        InviteManager.getInstance().removeInvite(resident, resident.getSettlement());
                        resident.leaveSettlement();
                    }
                    c.getGui().close();
                }, c -> InventoryGui.goBack(c.getWhoClicked()), "player", resident.getName(), "state", resident.getSettlement().getStateTag());
                return true;
            }, getText("kick.entry", "player", resident.getName(), "state", resident.getSettlement().getStateTag())));
        }

        if (sender.hasPermission("settlement.banish")) {
            gui.addElement(new StaticGuiElement('k', getIcon("banish"), click -> {
                Settlement settlement = resident.getSettlement();
                new AnvilGUI.Builder()
                        .itemLeft(ItemUtils.getHead(resident.getUUID(), resident.getName()))
                        .title(getText("banish.anvil.title", "player", resident.getName()))
                        .text(getText("banish.anvil.placeholder", "player", resident.getName()))
                        .onClose(player -> Utils.runLater(() -> new ResidentGUI(sender, resident).open(), 1))
                        .onComplete((player, text) -> {
                            if (settlement.hasPermission(sender, "banish") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                                String reason = text.trim();
                                getMessage("command.settlement.banish.banished").tell(resident.getSettlement().getOnlineResidents(), "name", resident.getName(), "reason", reason);
                                InviteManager.getInstance().removeInvite(resident, resident.getSettlement());
                                resident.getSettlement().addBanished(sender, resident, reason);
                                resident.leaveSettlement();

                                Utils.runLater(() -> InventoryGui.goBack(player), 1);
                            } else {
                                getError("no-permission").tell(player);
                            }
                            return AnvilGUI.Response.close();
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

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }
}

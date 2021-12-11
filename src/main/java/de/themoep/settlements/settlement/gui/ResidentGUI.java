package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.land.gui.RankSelectGUI;
import de.themoep.settlements.land.gui.TitleGUI;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;

public class ResidentGUI extends SettlementsGUI {

    private final InventoryGui gui;
    private final Resident resident;

    public ResidentGUI(Resident sender, Resident resident) {
        super("settlement.resident");
        this.resident = sender;
        String[] replacements = resident.getReplacements();
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", replacements), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        gui.addElement(new StaticGuiElement('p',
                ItemUtils.getHead(resident.getUUID(), resident.getName()),
                getText("info", replacements)));

        gui.addElement(new StaticGuiElement('a', getIcon("a"), getText("a"))); // TODO What is this element?

        if (sender.hasPermission("settlement.settaxevasion")) {
            gui.addElement(new GuiStateElement('e', () -> resident.getTaxDebtAction().getKey(),
                    new GuiStateElement.State(click -> {
                        if (sender.hasPermission("settlement.settaxevasion")) {
                            resident.setTaxDebtAction(TaxDebtAction.NONE);
                            resident.save();
                        }
                    }, TaxDebtAction.NONE.getKey(), getIcon("evasion"), getText("tax-evasion", "action", resident.getTaxDebtAction().getTag())),
                    new GuiStateElement.State(click -> {
                        if (sender.hasPermission("settlement.settaxevasion")) {
                            resident.setTaxDebtAction(TaxDebtAction.KICK);
                            resident.save();
                        }
                    }, TaxDebtAction.KICK.getKey(), getIcon("evasion"), getText("tax-evasion", "action", resident.getTaxDebtAction().getTag()))));
        }

        if (sender.hasPermission("settlement.settaxexempt")) {
            gui.addElement(new GuiStateElement('x', () -> String.valueOf(resident.isTaxExempted()),
                    new GuiStateElement.State(click -> {
                        if (sender.hasPermission("settlement.settaxexempt")) {
                            resident.setTaxExempted(true);
                            resident.save();
                        }
                    }, Boolean.TRUE.toString(), getIcon("taxes"), getText("taxes.entry",
                            "donated", Utils.CURRENCY_FORMAT.format(resident.getDonated()),
                            "taxpaid", Utils.CURRENCY_FORMAT.format(resident.getTaxPaid()),
                            "exempted", getText("taxes.exempted")
                    )),
                    new GuiStateElement.State(click -> {
                        if (sender.hasPermission("settlement.settaxexempt")) {
                            resident.setTaxExempted(false);
                            resident.save();
                        }
                    }, Boolean.FALSE.toString(), getIcon("taxes"), getText("taxes.entry",
                            "donated", Utils.CURRENCY_FORMAT.format(resident.getDonated()),
                            "taxpaid", Utils.CURRENCY_FORMAT.format(resident.getTaxPaid()),
                            "exempted", getText("taxes.paying")
                    ))));
        }

        gui.addElement(new StaticGuiElement('t', getIcon("settitle"), click -> {
            if (sender.hasPermission("settlement.settitle") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                new TitleGUI<>("settlement", sender, resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("settitle", "title", resident.getSettlementTitle())));

        gui.addElement(new StaticGuiElement('r', getIcon("rank"), click -> {
            if (sender.hasPermission("settlement.setrank") && sender.getSettlementRank().above(resident.getSettlementRank())) {
                new RankSelectGUI<>("settlement.setrank", sender, resident.getSettlement(), rank -> {
                    resident.setSettlementRank(rank);
                    resident.save();
                    InventoryGui.goBack(click.getWhoClicked());
                }, rank -> {
                    new ConfirmationGui(this, "transfer", c -> {
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
        }, getText("rank", "rank", resident.getSettlementRank().getTag())));

        if (sender.hasPermission("settlement.kick")) {
            gui.addElement(new StaticGuiElement('l', getIcon("eviction"), click -> {
                if (sender.hasPermission("settlement.kick")) {
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
                }
                return true;
            }, getText("eviction")));

            gui.addElement(new StaticGuiElement('k', getIcon("kick"), click -> {
                new ConfirmationGui(this, "kick", c -> {
                    if (sender.hasPermission("settlement.kick")) {
                        Message.get("command.settlement.kick.kicked-alert").tell(resident.getSettlement().getOnlineResidents(), "player", resident.getName());
                        InviteCache.getPendingSettlementInvites().remove(resident.getUUID(), resident.getSettlement().getId());
                        resident.leaveSettlement();
                    }
                    c.getGui().close();
                }, c -> InventoryGui.goBack(c.getWhoClicked()), "player", resident.getName(), "state", resident.getSettlement().getStateTag());
                return true;
            }, getText("kick.entry", "player", resident.getName(), "state", resident.getSettlement().getStateTag())));

        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

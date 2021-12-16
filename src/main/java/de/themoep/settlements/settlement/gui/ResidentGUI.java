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
import de.themoep.settlements.utils.ConfirmationGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;

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
                if (resident.getSettlement().hasPermission(sender, "kick")) {
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
                    if (resident.getSettlement().hasPermission(sender, "kick")) {
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
                        .onClose(player -> Utils.runLater(this::open, 1))
                        .onComplete((player, text) -> {
                            if (settlement.hasPermission(sender, "banish")) {
                                String reason = text.trim();getMessage("command.settlement.banish.banished").tell(resident.getSettlement().getOnlineResidents(), "name", resident.getName(), "reason", reason);
                                InviteManager.getInstance().removeInvite(resident, resident.getSettlement());
                                resident.leaveSettlement();
                                resident.getSettlement().addBanished(sender, resident, reason);

                                Utils.runLater(() -> InventoryGui.goBack(player), 1);
                            } else {
                                getError("no-permission").tell(player);
                            }
                            return AnvilGUI.Response.close();
                        });
                return true;
            }, getText("banish.entry", "player", resident.getName(), "state", resident.getSettlement().getStateTag())));
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}

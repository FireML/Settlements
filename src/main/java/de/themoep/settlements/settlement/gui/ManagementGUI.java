package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.JoinRequest;
import de.themoep.settlements.land.gui.AlliesGUI;
import de.themoep.settlements.land.gui.BanishedGUI;
import de.themoep.settlements.land.gui.BuildGUI;
import de.themoep.settlements.land.gui.NpcListGUI;
import de.themoep.settlements.land.gui.PermsGUI;
import de.themoep.settlements.land.gui.RankPermsGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ManagementGUI extends SettlementsGUI {
    private final Resident resident;
    public InventoryGui gui;

    public ManagementGUI(Resident resident) {
        super("settlement.management");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "name", resident.getSettlement().getName(),
                "state", resident.getSettlement().getStateTag()
        ), getSetup());
        gui.setCloseAction(close -> false);
        gui.addElements(getConfiguredElements());
        gui.setFiller(getIcon("filler"));

        Resident chief = resident.getSettlement().getChief();
        gui.addElement(new StaticGuiElement('r', ItemUtils.getHead(chief.getUUID(), chief.getName()), click -> {
            if (resident.hasPermission("settlement.reslist")) {
                new ResListGUI(resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("reslist",
                "state", resident.getSettlement().getStateTag(),
                "action", resident.hasPermission("settlement.reslist") ? getText("unlockables.unlocked.browse") : getText("no-permission"))));


        Collection<Invite<?, ?>> requests = getRequests(resident);
        gui.addElement(new StaticGuiElement('e', ItemUtils.setGlowing(getIcon("recruitment"), !requests.isEmpty()), click -> {
            if (resident.getSettlement().hasUnlocked("recruitment") && resident.hasPermission("settlement.recruitment")) {
                new ListGui<>(resident, "settlement.requests", () -> getRequests(resident), (c, request) -> {
                    // accept invite
                    if (request instanceof JoinRequest) {
                        Resident source = (Resident) request.getSource();
                        if (c.getEvent().isLeftClick()) {
                            if (handleJoin(resident, source, resident.getSettlement())) {
                                c.getGui().close();
                            } else {
                                c.getGui().draw();
                            }
                        } else if (c.getEvent().isRightClick()) {
                            resident.getSettlement().removeJoinRequest((JoinRequest) request);
                            Utils.runAsync(() -> DatabaseManager.getInstance().delete(request));
                            c.getGui().draw();
                        }
                    }
                }, request -> request.getReplacements(this)).open();
            }
            return true;
        }, getText("recruitment",
                "amount", String.valueOf(requests),
                "state", resident.getSettlement().getStateTag(),
                "action", resident.getSettlement().hasUnlocked("recruitment")
                        ? resident.hasPermission("settlement.recruitment") ? getText("unlockables.unlocked.browse") : getText("no-permission")
                        : getText("unlockables.locked.level")
        )));

        gui.addElement(new StaticGuiElement('d', getIcon("banished"), click -> {
            if (resident.hasPermission("settlement.banished")) {
                new BanishedGUI("settlement", resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("banished",
                "state", resident.getSettlement().getStateTag(),
                "action", resident.hasPermission("settlement.banished") ? getText("unlockables.unlocked.browse") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('a', getIcon("allies"), click -> {
            if (resident.getSettlement().hasUnlocked("allies") && resident.hasPermission("settlement.allies")) {
                new AlliesGUI("settlement", resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("allies",
                "state", resident.getSettlement().getStateTag(),
                "action", resident.getSettlement().hasUnlocked("allies") ? resident.hasPermission("settlement.allies") ? getText("unlockables.unlocked.options") : getText("no-permission") : getText("unlockables.locked.npc")
        )));

        if (SettlementsPlugin.getInstance().getCitizens() != null && resident.hasPermission("command.npc")) {
            gui.addElement(new StaticGuiElement('n', getIcon("npc"), click -> {
                if (resident.hasPermission("command.npc")) {
                    new NpcListGUI(resident, resident.getSettlement()).open();
                }
                return true;
            }, getText("npc")));
        }

        gui.addElement(new StaticGuiElement('t', getIcon("ranks"), click -> {
            if (resident.hasPermission("settlement.ranks")) {
                new RankPermsGUI<>("settlement", resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("ranks",
                "state", resident.getSettlement().getStateTag(),
                "action", resident.hasPermission("settlement.ranks") ? getText("unlockables.unlocked.options") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('s', getIcon("settings"), click -> {
            if (resident.hasPermission("settlement.settings")) {
                new SettingsGUI(resident).open();
            }
            return true;
        }, getText("settings",
                "state", resident.getSettlement().getStateTag(),
                "action", resident.hasPermission("settlement.settings") ? getText("unlockables.unlocked.options") : getText("no-permission")
        )));

        gui.addElement(new StaticGuiElement('l', getIcon("land"), click -> {
            if (resident.hasPermission("settlement.perms")) {
                Supplier<Boolean> permCheck = () -> resident.getSettlement().hasPermission(resident, "perms");
                new PermsGUI("settlement",
                        resident.getSettlement(),
                        permCheck,
                        (flagAble, c) -> new BuildGUI("empire", flagAble, permCheck).open(c.getWhoClicked())
                ).open(resident.getPlayer());
            }
            return true;
        }, getText("land",
                "state", resident.getSettlement().getStateTag(),
                "action", resident.hasPermission("settlement.perms") ? getText("unlockables.unlocked.options") : getText("no-permission")
        )));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    private Collection<Invite<?, ?>> getRequests(Resident resident) {
        return resident.getSettlement().getJoinRequests().stream().map(r -> (Invite<?, ?>) r).collect(Collectors.toList());
    }

    public void open() {
        gui.show(resident.getPlayer());
    }

    private boolean handleJoin(Resident sender, Resident resident, Settlement settlement) {
        boolean hasInvite = InviteManager.getInstance().getInvite(resident, settlement) != null;
        if (settlement.getJoinType() == JoinType.ANYONE || hasInvite) {
            String[] replacements = {
                    "name", resident.getName(),
                    "state", settlement.getState().getTag()
            };
            if (settlement.isAbandoned()) {
                getError("settlement-doesnt-exist").actionbar(sender, "name", settlement.getName());
            } else if (settlement.getId().isNull()) {
                getError("missing-settlement-id").actionbar(sender, "name", settlement.getName());
            } else if (settlement.isFull()) {
                Message.get("command.settlement.recruitment.is-full").actionbar(sender, replacements);
            } else if (settlement.isBanished(resident)) {
                Message.get("command.settlement.recruitment.banished").actionbar(sender, replacements);
            } else if (settlement.getJoinFee() > 0 && !SettlementsPlugin.getInstance().getEconomy().has(resident.getName(), settlement.getJoinFee())) {
                Message.get("command.settlement.recruitment.not-enough-money").actionbar(sender, replacements);
            } else {
                if (settlement.getJoinFee() > 0) {
                    if (SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), settlement.getJoinFee()).transactionSuccess()) {
                        settlement.addToBank(settlement.getJoinFee());
                        MessageUtils.notify(settlement, resident, Message.get("command.settlement.join.money-paid"), "amount", String.valueOf(settlement.getJoinFee()));
                        settlement.save();
                        DatabaseManager.getInstance().log(settlement, resident, "joinfee", "cash", settlement.getJoinFee());
                    } else {
                        sender.tell("&cAn internal error occured!");
                        return false;
                    }
                }
                settlement.addMember(resident);
                MessageUtils.notify(settlement, resident, Message.get("command.settlement.join.success"),
                        "name", settlement.getName(), "state", settlement.getState().getTag());
                Message.get("command.settlement.join.new-resident").tell(settlement, "player", resident.getName());
                return true;
            }
        }
        return false;
    }
}

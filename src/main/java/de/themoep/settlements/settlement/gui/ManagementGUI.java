package de.themoep.settlements.settlement.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.land.JoinRequest;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.gui.*;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.*;
import de.themoep.settlements.utils.config.Config;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ManagementGUI extends SettlementsGUI {
    private final Resident resident;
    public final InventoryGui gui;

    public ManagementGUI(Resident resident) {
        super("settlement.management");
        this.resident = resident;
        LandOwner<?, ?> landOwner = resident.getSettlement();
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "name", landOwner.getName(),
                "state", landOwner.getStateTag()
        ), getSetup());
        gui.setCloseAction(close -> false);
        gui.addElements(getConfiguredElements());
        gui.setFiller(getIcon("filler"));

        Resident chief = landOwner.getChief();
        gui.addElement(new StaticGuiElement('r', ItemUtils.getHead(chief.getUUID(), chief.getName()), click -> {
            if (landOwner.hasPermission(resident, "reslist")) {
                new ResListGUI(resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("reslist",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "reslist") ? getText("unlockables.unlocked.browse") : getText("no-permission"))));


        Collection<Invite<?, ?>> requests = getRequests(resident);
        gui.addElement(new StaticGuiElement('e', ItemUtils.setGlowing(getIcon("recruitment"), !requests.isEmpty()), click -> {
            if ((landOwner.hasUnlocked("recruitment") || !Config.Citizens.UNLOCK_DEPENDENCY) && landOwner.hasPermission(resident, "recruitment")) {
                new ListGui<>(resident, landOwner.getKey() + ".requests", true, () -> getRequests(resident), (c, request) -> {
                    // accept invite
                    if (request instanceof JoinRequest) {
                        Resident source = (Resident) request.getSource();
                        if (c.getType().isLeftClick()) {
                            if (handleJoin(resident, source, resident.getSettlement())) {
                                c.getGui().close();
                            } else {
                                c.getGui().draw();
                            }
                        } else if (c.getType().isRightClick()) {
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
                "action", (landOwner.hasUnlocked("recruitment") || !Config.Citizens.UNLOCK_DEPENDENCY)
                        ? landOwner.hasPermission(resident, "recruitment")
                                ? getText("unlockables.unlocked.browse")
                                : getText("unlockables.locked.permission")
                        : getUnlockText(landOwner, "recruitment"),
                "state", landOwner.getStateTag()
        )));

        gui.addElement(new StaticGuiElement('d', getIcon("banished"), click -> {
            if (landOwner.hasPermission(resident, "banished")) {
                new BanishedGUI(resident, landOwner).open();
            }
            return true;
        }, getText("banished",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "banished") ? getText("unlockables.unlocked.browse") : getText("unlockables.locked.permission")
        )));

        gui.addElement(new StaticGuiElement('a', getIcon("allies"), click -> {
            if ((landOwner.hasUnlocked("allies") || !Config.Citizens.UNLOCK_DEPENDENCY) && landOwner.hasPermission(resident, "allies")) {
                new AlliesGUI(resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("allies",
                "state", landOwner.getStateTag(),
                "action", (landOwner.hasUnlocked("allies") || !Config.Citizens.UNLOCK_DEPENDENCY) ? landOwner.hasPermission(resident, "allies") ? getText("unlockables.unlocked.options") : getText("unlockables.locked.permission") : getUnlockText(landOwner, "allies")
        )));

        if (SettlementsPlugin.getInstance().getCitizens() != null && landOwner.hasPermission(resident, "npc")) {
            gui.addElement(new StaticGuiElement('n', getIcon("npc"), click -> {
                if (landOwner.hasPermission(resident, "npc")) {
                    new NpcListGUI(resident, resident.getSettlement()).open();
                }
                return true;
            }, getText("npc")));
        }

        gui.addElement(new StaticGuiElement('t', getIcon("ranks"), click -> {
            if (landOwner.hasPermission(resident, "ranks")) {
                new RankPermsGUI<>(landOwner.getKey(), resident, resident.getSettlement()).open();
            }
            return true;
        }, getText("ranks",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "ranks") ? getText("unlockables.unlocked.options") : getText("unlockables.locked.permission")
        )));

        gui.addElement(new StaticGuiElement('s', getIcon("settings"), click -> {
            if (landOwner.hasPermission(resident, "settings")) {
                new SettingsGUI(resident, landOwner).open();
            }
            return true;
        }, getText("settings",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "settings") ? getText("unlockables.unlocked.options") : getText("unlockables.locked.permission")
        )));

        gui.addElement(new StaticGuiElement('l', getIcon("land"), click -> {
            if (landOwner.hasPermission(resident, "perms")) {
                Supplier<Boolean> permCheck = () -> landOwner.hasPermission(resident, "perms");
                new PermsGUI(landOwner.getKey(),
                        resident.getSettlement(),
                        permCheck,
                        (flagAble, c) -> new BuildGUI(landOwner.getKey(), flagAble, permCheck).open(c.getWhoClicked()),
                        resident
                ).open(resident.getPlayer());
            }
            return true;
        }, getText("land",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "perms") ? getText("unlockables.unlocked.options") : getText("unlockables.locked.permission")
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
        String[] replacements = {
                "name", resident.getName(),
                "state", settlement.getState().getTag()
        };
        if (settlement.isMember(resident)) {
            getError("free-required").actionbar(sender);
        } else if (settlement.isAbandoned()) {
            getError("settlement-doesnt-exist").actionbar(sender, "name", settlement.getName());
        } else if (settlement.getId().isNull()) {
            getError("missing-settlement-id").actionbar(sender, "name", settlement.getName());
        } else if (settlement.isFull()) {
            Message.get("command.settlement.recruitment.is-full").actionbar(sender, replacements);
        } else if (settlement.isBanished(resident)) {
            Message.get("command.settlement.recruitment.banished").actionbar(sender, replacements);
        } else if (settlement.getJoinFee() > 0 && !SettlementsPlugin.getInstance().getEconomy().has(resident.getPlayer(), settlement.getJoinFee())) {
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
        return false;
    }
}

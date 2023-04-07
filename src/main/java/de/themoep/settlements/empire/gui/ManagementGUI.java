package de.themoep.settlements.empire.gui;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.land.JoinRequest;
import de.themoep.settlements.land.gui.*;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.*;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ManagementGUI extends SettlementsGUI {
    private final Resident resident;
    public InventoryGui gui;

    public ManagementGUI(Resident resident) {
        super("empire.management");
        this.resident = resident;
        Empire landOwner = resident.getSettlement().getEmpire();
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title",
                "name", landOwner.getName(),
                "state", landOwner.getStateTag()
        ), getSetup());
        gui.setCloseAction(close -> false);
        gui.addElements(getConfiguredElements());
        gui.setFiller(getIcon("filler"));

        Resident chief = landOwner.getChief();
        gui.addElement(new StaticGuiElement('r', ItemUtils.getHead(chief.getUUID(), chief.getName()), click -> {
            if (landOwner.hasPermission(resident, "memberlist")) {
                new ListGui<>(resident, "empire.memberlist", true, landOwner::getMembers, (c, s) -> {
                    // TODO: Member GUI new MemberGUI(resident, s).open();
                }, r -> r.getReplacements(this)).open();
            }
            return true;
        }, getText("memberlist",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "memberlist") ? getText("unlockables.unlocked.browse") : getText("no-permission"))));


        Collection<Invite<?, ?>> requests = getRequests(landOwner);
        gui.addElement(new StaticGuiElement('e', ItemUtils.setGlowing(getIcon("recruitment"), !requests.isEmpty()), click -> {
            if (landOwner.hasUnlocked("recruitment") && landOwner.hasPermission(resident, "recruitment")) {
                new ListGui<>(resident, landOwner.getKey() + ".requests", true, () -> getRequests(landOwner), (c, request) -> {
                    // accept invite
                    if (request instanceof JoinRequest) {
                        Resident source = (Resident) request.getSource();
                        if (c.getType().isLeftClick()) {
                            if (handleJoin(resident, source, landOwner)) {
                                c.getGui().close();
                            } else {
                                c.getGui().draw();
                            }
                        } else if (c.getType().isRightClick()) {
                            landOwner.removeJoinRequest((JoinRequest) request);
                            Utils.runAsync(() -> DatabaseManager.getInstance().delete(request));
                            c.getGui().draw();
                        }
                    }
                }, request -> request.getReplacements(this)).open();
            }
            return true;
        }, getText("recruitment",
                "amount", String.valueOf(requests),
                "action", landOwner.hasUnlocked("recruitment")
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
            if (landOwner.hasUnlocked("allies") && landOwner.hasPermission(resident, "allies")) {
                new AlliesGUI(resident, landOwner).open();
            }
            return true;
        }, getText("allies",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasUnlocked("allies") ? landOwner.hasPermission(resident, "allies") ? getText("unlockables.unlocked.options") : getText("unlockables.locked.permission") : getUnlockText(landOwner, "allies")
        )));

        if (SettlementsPlugin.getInstance().getCitizens() != null && landOwner.hasPermission(resident, "npc")) {
            gui.addElement(new StaticGuiElement('n', getIcon("npc"), click -> {
                if (landOwner.hasPermission(resident, "npc")) {
                    new NpcListGUI(resident, landOwner).open();
                }
                return true;
            }, getText("npc")));
        }

        gui.addElement(new StaticGuiElement('t', getIcon("ranks"), click -> {
            if (landOwner.hasPermission(resident, "ranks")) {
                new RankPermsGUI<>(landOwner.getKey(), resident, landOwner).open();
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
                        landOwner,
                        permCheck,
                        (flagAble, c) -> new BuildGUI(landOwner.getKey(), flagAble, permCheck).open(c.getWhoClicked())
                ).open(resident.getPlayer());
            }
            return true;
        }, getText("land",
                "state", landOwner.getStateTag(),
                "action", landOwner.hasPermission(resident, "perms") ? getText("unlockables.unlocked.options") : getText("unlockables.locked.permission")
        )));

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
    }

    private Collection<Invite<?, ?>> getRequests(Empire empire) {
        return empire.getJoinRequests().stream().map(r -> (Invite<?, ?>) r).collect(Collectors.toList());
    }

    public void open() {
        gui.show(resident.getPlayer());
    }

    private boolean handleJoin(Resident sender, Resident resident, Empire empire) {
        String[] replacements = {
                "name", resident.getName(),
                "state", empire.getStateTag()
        };
        Settlement settlement = resident.getSettlement();
        if (settlement == null) {
            getError("not-in-settlement").actionbar(sender, "player", resident.getName());
        } else if (settlement.hasEmpire()) {
            getError("free-required").actionbar(sender);
        } else if (settlement.hasPermission(resident, "empire.join")) {
            getError("no-permission").actionbar(sender);
        } else if (empire.isAbandoned()) {
            getError("empire-doesnt-exist").actionbar(sender, "name", empire.getName());
        } else if (empire.getId().isNull()) {
            getError("missing-empire-id").actionbar(sender, "name", empire.getName());
        } else if (empire.isFull()) {
            Message.get("command.empire.recruitment.is-full").actionbar(sender, replacements);
        } else if (empire.isBanished(settlement)) {
            Message.get("command.empire.recruitment.banished").actionbar(sender, replacements);
        } else if (empire.getJoinFee() > 0 && !settlement.has(empire.getJoinFee())) {
            Message.get("command.empire.recruitment.not-enough-money").actionbar(sender, replacements);
        } else {
            if (empire.getJoinFee() > 0) {
                if (settlement.removeFromBank(empire.getJoinFee())) {
                    empire.addToBank(empire.getJoinFee());
                    MessageUtils.notify(empire, resident, Message.get("command.empire.join.money-paid"), "amount", String.valueOf(empire.getJoinFee()));
                    empire.save();
                    DatabaseManager.getInstance().log(empire, resident, "joinfee", "cash", empire.getJoinFee());
                } else {
                    sender.tell("&cAn internal error occured!");
                    return false;
                }
            }
            empire.addMember(resident.getSettlement());
            MessageUtils.notify(empire, resident, Message.get("command.empire.join.success"),
                    "name", empire.getName(), "state", settlement.getStateTag());
            Message.get("command.empire.join.new-member").tell(empire, "state", settlement.getStateTag(), "name", resident.getName());
            return true;
        }
        return false;
    }
}

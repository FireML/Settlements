package de.themoep.settlements.resident.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.settlement.SettlementHomeCommand;
import de.themoep.settlements.command.settlement.SettlementRulesCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.gui.NpcListGUI;
import de.themoep.settlements.land.gui.plot.SettingsGUI;
import de.themoep.settlements.lock.LockManager;
import de.themoep.settlements.resident.Friend;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;

public class OverviewGUI extends ConfigurableGui {

    public OverviewGUI(Resident resident) {
        super(resident, "general.overview",
                "state", resident.hasSettlement() ? resident.getSettlement().getStateTag() : "",
                "name", resident.hasSettlement() ? resident.getSettlement().getName() : ""
        );
        if (!resident.hasSettlement()) {
            return;
        }

        Settlement settlement = resident.getSettlement();

        gui.addElement(new StaticGuiElement('s', getIcon("settlement"),
                getText("settlement." + (settlement.hasEmpire() ? "empire" : "no-empire"), settlement.getReplacements(this))));

        gui.addElement(new StaticGuiElement('r', getIcon("rules"), click -> {
            ((SettlementRulesCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("rules"))
                    .run(resident, "settlement", "rules");
            return true;
        }, getText("rules", "state", settlement.getStateTag(), "name", settlement.getName())));

        gui.addElement(new DynamicGuiElement('l', who -> {
            boolean isLock = "lock".equals(LockManager.getInstance().getToggledAction(who.getUniqueId()));
            return new StaticGuiElement('l', getIcon("lock"), click -> {
                LockManager.getInstance().toggleAction(click.getWhoClicked().getUniqueId(), "lock");
                click.getGui().draw();
                return true;
            }, getText("lock", "status", getText("toggle-status." + (isLock ? "enabled" : "disabled"))));
        }));

        gui.addElement(new DynamicGuiElement('u', who -> {
            boolean isUnlock = "unlock".equals(LockManager.getInstance().getToggledAction(who.getUniqueId()));
            return new StaticGuiElement('u', getIcon("unlock"), click -> {
                LockManager.getInstance().toggleAction(click.getWhoClicked().getUniqueId(), "unlock");
                click.getGui().draw();
                return true;
            }, getText("unlock", "status", getText("toggle-status." + (isUnlock ? "enabled" : "disabled"))));
        }));

        if (SettlementsPlugin.getInstance().getCitizens() != null && resident.getSettlement().hasPermission(resident, "npc")) {
            gui.addElement(new StaticGuiElement('n', getIcon("npc"), click -> {
                if (resident.getSettlement().hasPermission(resident, "npc")) {
                    new NpcListGUI(resident, resident.getSettlement()).open();
                }
                return true;
            }, getText("npc")));
        }

        gui.addElement(new StaticGuiElement('h', getIcon("home"), click -> {
            ((SettlementHomeCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("home"))
                    .run(resident, "settlement", "home");
            return true;
        }, getText("home", "state", settlement.getStateTag(), "name", settlement.getName())));

        OwnedLand land = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        gui.addElement(new StaticGuiElement('p', getIcon("plot"), click -> {
            OwnedLand currentLand = LandManager.getInstance().getLand(click.getWhoClicked().getLocation());
            if (currentLand != null && (currentLand.isPlotOwner(resident) || currentLand.canAdmin(resident))) {
                new SettingsGUI(resident).open();
            }
            return true;
        }, getText("plot",
                "action", getText(land != null
                        ? land.isPlotOwner(resident) || land.canAdmin(resident)
                                ? "unlockables.unlocked.options"
                                : "unlockables.locked.not-own-plot"
                        : "unlockables.locked.not-on-land"
                )
        )));

        Collection<FriendRequest> requests = resident.getFriendRequests();
        gui.addElement(new StaticGuiElement('f', ItemUtils.setGlowing(getIcon("friends"), !requests.isEmpty()), click -> {
            new FriendsList(resident, "general.friends").open();
            return true;
        }, getText("friends", "amount", String.valueOf(requests.size()))));
    }

    private boolean handleJoin(Resident resident, Settlement settlement) {
        boolean hasInvite = InviteManager.getInstance().getInvite(resident, settlement) != null;
        if (settlement.getJoinType() == JoinType.ANYONE || hasInvite) {
            if (settlement.isAbandoned()) {
                getError("settlement-doesnt-exist").actionbar(resident, "name", settlement.getName());
            } else if (settlement.getId().isNull()) {
                getError("missing-settlement-id").actionbar(resident, "name", settlement.getName());
            } else if (settlement.getJoinType() != JoinType.ANYONE && !hasInvite) {
                Message.get("command.settlement.join.no-invite").actionbar(resident, "name", settlement.getName(), "state", settlement.getStateTag());
            } else if (settlement.isFull()) {
                Message.get("command.settlement.join.is-full").actionbar(resident, "state", settlement.getState().getTag(), "name", settlement.getName());
            } else if (settlement.isBanished(resident)) {
                Message.get("command.settlement.join.banished").actionbar(resident, "state", settlement.getState().getTag(), "name", settlement.getName());
            } else if (settlement.getJoinFee() > 0 && !SettlementsPlugin.getInstance().getEconomy().has(resident.getPlayer(), settlement.getJoinFee())) {
                getError("not-enough-money").actionbar(resident, "state", settlement.getState().getTag(), "name", settlement.getName(), "amount", String.valueOf(settlement.getJoinFee()));
            } else {
                if (settlement.getJoinFee() > 0) {
                    if (SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), settlement.getJoinFee()).transactionSuccess()) {
                        settlement.addToBank(settlement.getJoinFee());
                        Message.get("command.settlement.join.money-paid").tell(resident, "amount", String.valueOf(settlement.getJoinFee()));
                        settlement.save();
                        DatabaseManager.getInstance().log(settlement, resident, "joinfee", "cash", settlement.getJoinFee());
                    } else {
                        resident.tell("&cAn internal error occured!");
                        return false;
                    }
                }
                settlement.addMember(resident);
                Message.get("command.settlement.join.success").tell(resident, "name", settlement.getName(), "state", settlement.getState().getTag());
                Message.get("command.settlement.join.new-resident").tell(settlement, "player", resident.getName());
                return true;
            }
        }
        return false;
    }
}

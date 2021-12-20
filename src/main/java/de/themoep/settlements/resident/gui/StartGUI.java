package de.themoep.settlements.resident.gui;

import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.command.settlement.SettlementHomeCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.invites.SettlementInvite;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StartGUI extends ConfigurableGui {

    public StartGUI(Resident resident) {
        super(resident, "general.start");
        if (resident.hasSettlement()) {
            return;
        }

        gui.addElement(new StaticGuiElement('s', getIcon("start"), click -> {
            List<BaseComponent[]> pages = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Message page = Message.get("start-guide.book.page" + i);
                if (page.exists()) {
                    pages.add(page.getReplacedComponents("playername", resident.getName(), "playerid", resident.getUUID().toString()));
                } else {
                    break;
                }
            }

            resident.getPlayer().openBook(SettlementsPlugin.getInstance().getServerAdapter().createBook(
                    "Start Guide",
                    SettlementsPlugin.getInstance().getName(),
                    pages
            ));
            return true;
        }, getText("start", "playername", resident.getName())));

        Collection<Invite<?, ?>> requests = getRequests(resident);
        gui.addElement(new StaticGuiElement('i', ItemUtils.setGlowing(getIcon("requests"), !requests.isEmpty()), click -> {
            new ListGui<>(resident, "general.requests", () -> getRequests(resident), (c, request) -> {
                // accept invite
                if (request instanceof SettlementInvite) {
                    if (handleJoin(resident, (Settlement) request.getSource())) {
                        c.getGui().close();
                    } else {
                        c.getGui().draw();
                    }
                }
            }, request -> request.getReplacements(this)).open();
            return true;
        }, getText("requests", "amount", String.valueOf(requests.size()))));

        gui.addElement(new StaticGuiElement('c', getIcon("create"), click -> {
            if (resident.hasPermission("settlements.command.settlement.create")) {
                new AnvilGUI.Builder()
                        .itemLeft(getIcon("create"))
                        .title(getText("create.anvil.title"))
                        .text("")
                        .onClose(player -> Utils.runLater(this::open, 1))
                        .onComplete((player, name) -> {
                            name = Utils.filterAllCurses(name.trim());
                            if (name.toLowerCase().contains("claims")) {
                                return AnvilGUI.Response.text(getErrorText("invalid-characters", "input", name));
                            } else if (SettlementManager.getInstance().getFromName(name) != null) {
                                return AnvilGUI.Response.text(getErrorText("settlement-exists", "name", name));
                            } else if (name.length() > Config.Settlement.NAME_LENGTH) {
                                return AnvilGUI.Response.text(getErrorText("too-long", "input", name, "amount", String.valueOf(Config.Settlement.NAME_LENGTH)));
                            } else if (!Pattern.compile(Config.Settlement.NAME_VALIDATION).matcher(name).matches()) {
                                return AnvilGUI.Response.text(getErrorText("invalid-characters", "input", name));
                            }

                            if (Config.Settlement.CREATION_COST != 0) {
                                if (!SettlementsPlugin.getInstance().getEconomy().has(resident.getPlayer(), Config.Settlement.CREATION_COST)) {
                                    return AnvilGUI.Response.text(getErrorText("not-enough-money", "amount", String.valueOf(Config.Settlement.CREATION_COST)));
                                }
                                EconomyResponse r = SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), (double) Config.Settlement.CREATION_COST);
                                if (!r.transactionSuccess()) {
                                    return AnvilGUI.Response.text("An error occurred, please tell an admin!");
                                }
                            }

                            Settlement settlement = new Settlement(name, resident.getUUID(), 1, SettlementState.VILLAGE);
                            SettlementManager.getInstance().registerNew(resident, settlement);

                            Message.get("command.settlement.create.broadcast").broadcast(
                                    "name", settlement.getName(),
                                    "state", settlement.getState().getTag(),
                                    "player", resident.getName());
                            Message.get("command.settlement.create.success").tell(resident, "name", settlement.getName());
                            return AnvilGUI.Response.close();
                        })
                        .plugin(SettlementsPlugin.getInstance())
                        .open((Player) click.getEvent().getWhoClicked());
            }
            return true;
        }, getText("create.entry",
                "amount", Utils.CURRENCY_FORMAT.format(Config.Settlement.CREATION_COST),
                "action", getText(resident.hasPermission("settlements.command.settlement.create")
                        ? resident.getBalance() < Config.Settlement.CREATION_COST ? "unlockables.locked.money" : "unlockables.unlocked.create"
                        : "no-permission")
        )));

        gui.addElement(new StaticGuiElement('j', getIcon("join"), click -> {
            new ListGui<>(resident, "general.settlements", () -> SettlementManager.getInstance().getSettlements(), (c, settlement) -> {
                // Join if open to anyone or Invite is available
                if (handleJoin(resident, settlement)) {
                    click.getGui().close();
                } else {
                    click.getGui().draw();
                }
            }, settlement -> {
                String action;
                switch (settlement.getJoinType()) {
                    case ANYONE:
                        action = "unlocked.join";
                        break;
                    case INVITE_ONLY:
                        action = "locked.invite-only";
                        break;
                    case APPLY:
                    default:
                        action = "locked.application-required";
                        break;
                }

                if (InviteManager.getInstance().getInvite(resident, settlement) != null) {
                    action = "unlocked.invited";
                }

                return new String[] { "action", getText("unlockables." + action) };
            }).open();
            return true;
        }, getText("join")));

        gui.addElement(new StaticGuiElement('p', getIcon("public"), click -> {
            new ListGui<>(resident, "general.public", () -> SettlementManager.getInstance().getSettlements().stream()
                    .filter(s -> s.getAccess() == LandAccess.PUBLIC)
                    .collect(Collectors.toList()), (c, settlement) -> {
                // teleport to public settlements
                if (settlement.getAccess() == LandAccess.PUBLIC) {
                    click.getGui().close();
                    ((SettlementHomeCommand) CommandManager.getInstance().getCommand("settlement").getSubCommands().get("home"))
                            .run(resident, "settlement", "home", settlement.getName());
                } else {
                    click.getGui().draw();
                }
            }, settlement -> new String[0]).open();
            return true;
        }, getText("public")));
    }

    private Collection<Invite<?, ?>> getRequests(Resident resident) {
        return new ArrayList<>(InviteManager.getInstance().getInvites(resident));
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

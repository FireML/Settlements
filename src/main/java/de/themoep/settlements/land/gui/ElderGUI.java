package de.themoep.settlements.land.gui;

import uk.firedev.poleislib.libs.themoep.inventorygui.DynamicGuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.common.MissionCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.JoinRequest;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.missions.MissionGui;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.npc.NpcType;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigurableGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

import java.util.stream.Collectors;

public class ElderGUI extends ConfigurableGui {

    public ElderGUI(Resident resident, LandOwner<?, ?> landOwner) {
        super(resident, landOwner.getKey() + ".elder", "type", NpcType.ELDER.getTag());

        if (landOwner.isMember(resident)) {
            if (landOwner instanceof MissionProvider provider) {
                if (!landOwner.hasUnlocked("missions") || landOwner.getLevelInfo().getMissions() <= 0) {
                    gui.addElement(new StaticGuiElement('e', getIcon("missions"), landOwner.getLevelInfo().getMissions() <= 0
                            ? getText("unlockables.locked.level", "state", landOwner.getStateTag())
                            : getUnlockText(landOwner, "missions")));
                } else {
                    if (!provider.getMissionTaker(resident).getMissionData().isEmpty()) {
                        gui.addElement(new DynamicGuiElement('e', who -> new StaticGuiElement('e', getIcon("missions"), click -> {
                            new MissionGui(resident, (MissionProvider<?>) landOwner, ((MissionProvider<?>) landOwner).getMissionTaker(resident)).open();
                            return true;
                        }, getText("missions",
                                Mission.getReplacements(this, (MissionProvider<?>) landOwner, resident)
                        ))));
                    } else {
                        gui.addElement(new DynamicGuiElement('e', who -> new StaticGuiElement('e', getIcon("missions"), getText("wait-for-missions",
                                "state", landOwner.getStateTag(),
                                "name", landOwner.getName()
                        ))));
                    }
                    if (provider.getMissionRedistributePrice() > -1 && landOwner.hasPermission(resident,"missions.redistribute")) {
                        gui.addElement(new StaticGuiElement('r', getIcon("redistribute"), click -> {
                            if (landOwner.hasPermission(resident,"missions.redistribute")) {
                                new MissionCommand() {

                                    @Override
                                    public String type() {
                                        return "command";
                                    }

                                    @Override
                                    public String name() {
                                        return landOwner.getKey() + ".missions.redistribute";
                                    }
                                }.distributeNewMissions(resident, (MissionProvider<?>) landOwner);
                                click.getGui().draw();
                            }
                            return true;
                        }, getText("redistribute-missions",
                                "state", landOwner.getStateTag(),
                                "name", landOwner.getName(),
                                "price", String.valueOf(provider.getMissionRedistributePrice()),
                                "action", landOwner.has(provider.getMissionRedistributePrice())
                                        ? getText("unlockables.unlocked.purchase")
                                        : getText("unlockables.locked.money")
                        )));
                    }
                }
            }
        } else if (landOwner.isBanished(resident)) {
            gui.addElement(new StaticGuiElement('e', getIcon("recruit"), getText("recruit.banished",
                    "state", landOwner.getStateTag(),
                    "name", landOwner.getName()
            )));
        } else if (landOwner.getJoinType() == JoinType.APPLY) {
            if (!landOwner.hasUnlocked("recruitment")) {
                gui.addElement(new StaticGuiElement('e', getIcon("recruit"), getErrorText("not-unlocked",
                        "function", "recruitment",
                        "name", landOwner.getName(),
                        "state", landOwner.getStateTag()
                )));
            } else if (landOwner.hasJoinRequest(resident)) {
                gui.addElement(new StaticGuiElement('e', getIcon("recruit"), click -> {
                    JoinRequest request = landOwner.removeJoinRequest(resident);
                    if (request != null) {
                        Utils.runAsync(() -> DatabaseManager.getInstance().delete(request));
                    }
                    Message.get("command.settlement.join.apply-cancelled").tell(resident, "name", landOwner.getName(), "state", landOwner.getStateTag());
                    click.getGui().close();
                    return true;
                }, Utils.wrapLore(getText("recruit.cancel-apply", landOwner.getReplacements(this)))));
            } else {
                gui.addElement(new StaticGuiElement('e', getIcon("recruit"), click -> {
                    Message.get("command.settlement.join.applied").tell(resident, "name", landOwner.getName(), "state", landOwner.getStateTag());
                    JoinRequest request = new JoinRequest(landOwner, resident);
                    landOwner.addJoinRequest(request);
                    request.save();
                    click.getGui().close();
                    MessageUtils.notify(
                            landOwner,
                            landOwner.getResidents().stream().filter(r -> r.hasPermission("settlement.recruitment")).collect(Collectors.toList()),
                            Message.get("command.settlement.join.someone-applied"),
                            "name", resident.getName(),
                            "state", landOwner.getStateTag()
                    );
                    return true;
                }, Utils.wrapLore(getText("recruit.apply", landOwner.getReplacements(this)))));
            }
        } else if (landOwner.getJoinType() == JoinType.ANYONE || (landOwner.getJoinType() == JoinType.INVITE_ONLY && isInvited(resident, landOwner))) {
            gui.addElement(new StaticGuiElement('e', getIcon("recruit"), click -> {
                if (landOwner instanceof Settlement settlement) {
                    if (settlement.isBanished(resident)) {
                        Message.get("command.settlement.join.banished").tell(resident, "state", settlement.getState().getTag(), "name", settlement.getName());
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
                                click.getGui().close();
                                return true;
                            }
                        }
                        settlement.addMember(resident);
                        Message.get("command.settlement.join.success").tell(resident, "name", settlement.getName(), "state", settlement.getState().getTag());
                        Message.get("command.settlement.join.new-resident").tell(settlement, "player", resident.getName());
                    }
                }
                click.getGui().close();
                return true;
            }, Utils.wrapLore(getText("recruit." + landOwner.getJoinType().getKey(), landOwner.getReplacements(this)))));
        } else {
            gui.addElement(new StaticGuiElement('e', getIcon("recruit"), Utils.wrapLore(getText("recruit." + landOwner.getJoinType().getKey(), landOwner.getReplacements(this)))));
        }

        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("close")));
    }

    private boolean isInvited(Resident resident, LandOwner<?, ?> landOwner) {
        if (landOwner instanceof Settlement) {
            return InviteManager.getInstance().getInvite(resident, (Settlement) landOwner) != null;
        }
        return false;
    }

}

package de.themoep.settlements.resident.gui;

import uk.firedev.poleislib.Utils.ItemUtils;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.resident.Friend;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ListGui;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class FriendsList extends SettlementsGUI {
    private final ListGui<Friend> gui;

    public FriendsList(Resident resident, String name) {
        super(name);
        gui = new ListGui<>(resident, "general.friends", true, resident::getFriends,
                (c, friend) -> {
                    // Unfriend
                    if (c.getType().isRightClick()) {
                        Resident fRes = friend.getResident();
                        fRes.removeFriend(resident.getUUID());
                        resident.removeFriend(fRes.getUUID());
                        resident.save();
                        fRes.save();
                        Message.get("command.friend.remove.success").tell(resident, "name", fRes.getName());
                        c.getGui().draw();
                    }
                }, friend -> {
                    String[] repl = friend.getReplacements(this);
                    for (int i = 0; i + 1 < repl.length; i += 2) {
                        if ("online".equals(repl[i])) {
                            Player player = SettlementsPlugin.getInstance().getServer().getPlayer(friend.getPlayerId());
                            if (player != null && player.isOnline() && !resident.getPlayer().canSee(player)) {
                                repl[i + 1] = getText("offline");
                            }
                        }
                    }
                    return repl;
                });

        Collection<FriendRequest> currentRequests = resident.getFriendRequests();
        gui.addElement(new StaticGuiElement('r', ItemUtils.setGlowing(getIcon("requests"), !currentRequests.isEmpty()), rClick -> {
            new ListGui<>(resident, "general.friend-requests", () -> new ArrayList<>(resident.getFriendRequests()),
                    (c, request) -> {
                        // accept request
                        if (request instanceof FriendRequest) {
                            FriendRequest currentRequest = resident.removeFriendRequest(((FriendRequest) request).getSenderName());
                            if (currentRequest == null) {
                                // No request sent?
                                c.getGui().draw();
                                return;
                            }
                            c.getGui().close();
                            Utils.runAsync(() -> {
                                Resident sender = (Resident) request.getSource();
                                DatabaseManager.getInstance().delete(request);
                                FriendRequest reverseRequest = sender.removeFriendRequest(resident.getName());
                                if (reverseRequest != null) {
                                    DatabaseManager.getInstance().delete(reverseRequest);
                                }
                                Utils.runSync(() -> {
                                    sender.addFriend(resident.getUUID(), resident.getName());
                                    resident.addFriend(sender.getUUID(), sender.getName());
                                    Message.get("command.friend.accept.success.target").tell(resident, "name", sender.getName());
                                    if (sender.getPlayer() != null) {
                                        Message.get("command.friend.accept.success.sender").tell(sender, "name", resident.getName());
                                    } else {
                                        for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                                            provider.sendMail(resident.getSettlementTitle() + " " + resident.getName(), sender,
                                                    Message.get("friend.add.mail").getReplacedText("name", resident.getName())
                                            );
                                        }
                                    }
                                    sender.save();
                                    resident.save();
                                });
                            });
                        }
                    }, request -> request.getReplacements(this)).open();
            return true;
        }, getText("requests", "amount", String.valueOf(currentRequests.size()))));
    }

    public void open() {
        gui.open();
    }
}

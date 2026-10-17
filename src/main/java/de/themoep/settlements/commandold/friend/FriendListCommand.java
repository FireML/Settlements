package de.themoep.settlements.commandold.friend;

import de.themoep.settlements.commandold.PlayerSubCommand;
import de.themoep.settlements.resident.Friend;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FriendListCommand extends PlayerSubCommand {
    public FriendListCommand(PlayerSubCommand parent) {
        super(parent, "list");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            Resident resident = ResidentManager.getInstance().getResident(player);
            if (resident.getFriends().isEmpty()) {
                getError("no-friends").tell(resident);
            } else {
                getMessage("head").tell(resident);
                TextComponent.Builder builder = Component.text();
                for (Friend friend : resident.getFriends()) {
                    Player p = friend.getPlayer();
                    Component append;
                    if (p != null && p.isOnline()) {
                        append = ComponentUtils.parseComponent(getMessage("entry").getReplacedText("online", getText("online"), "name", p.getName()));
                        append = append.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("/res show " + p.getName())));
                        append = append.clickEvent(ClickEvent.runCommand("/res show " + p.getName()));
                    } else {
                        append = ComponentUtils.parseComponent(getMessage("entry").getReplacedText("online", getText("offline"), "name", friend.getPlayerName()));
                        append = append.hoverEvent(HoverEvent.showText(ComponentUtils.parseComponent("/res show " + friend.getPlayerName())));
                        append = append.clickEvent(ClickEvent.runCommand("/res show " + friend.getPlayerName()));
                    }
                    builder.append(append).append(ComponentUtils.parseComponent(getText("separator")));
                }
                MessageUtils.tell(player, builder.build());
            }
            if (!resident.getFriendRequests().isEmpty()) {
                getMessage("head-requests").tell(resident);
                Component builder = Component.text("");
                List<FriendRequest> requests = new ArrayList<>(resident.getFriendRequests());
                for (int i = 0; i < requests.size(); i++) {
                    if (i > 0) {
                        builder = builder.append(getMessage("separator").getComponent());
                    }
                    FriendRequest request = requests.get(i);
                    builder = builder.append(getMessage("request-entry").getReplacedComponent("name", request.getSenderName()));
                }
                MessageUtils.tell(player, builder);
            }
        });
        return true;
    }
}

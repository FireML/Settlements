package de.themoep.settlements.command.friend;

import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.utils.ComponentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                String tell = resident.getFriends().stream().map(friend -> {
                    Player p = friend.getPlayer();
                    if (p != null && p.isOnline()) {
                        return getMessage("entry").getReplacedText("online", getText("online"), "name", p.getName());
                    }
                    return getMessage("entry").getReplacedText("online", getText("offline"), "name", friend.getPlayerName());
                }).collect(Collectors.joining(getText("separator")));
                MessageUtils.tell(player, ComponentUtils.parseComponent(tell));
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

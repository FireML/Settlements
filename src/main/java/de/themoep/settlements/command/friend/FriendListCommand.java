package de.themoep.settlements.command.friend;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Bukkit;

import java.util.stream.Collectors;

import de.themoep.settlements.utils.MessageUtils;
import org.bukkit.entity.Player;
import de.themoep.settlements.command.PlayerSubCommand;

public class FriendListCommand extends PlayerSubCommand {
    public FriendListCommand(PlayerSubCommand parent) {
        super(parent, "list");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            MessageUtils.tell(player, getMessage("head"));
            MessageUtils.tell(player, ResidentManager.getInstance().getResident(player).getFriends().stream().map(id -> {
                Player p = Bukkit.getPlayer(id);
                if (p != null && p.isOnline()) {
                    return getMessage("entry").getReplacedText("online", getText("online"), "name", p.getName());
                }
                Resident resident = ResidentManager.getInstance().getResident(id);
                if (resident != null) {
                    return getMessage("entry").getReplacedText("online", getText("offline"), "name", resident.getName());
                }
                return getMessage("entry").getReplacedText("online", getText("offline"), "name", id.toString());
            }).collect(Collectors.joining(getText("separator"))));
        });
        return true;
    }
}

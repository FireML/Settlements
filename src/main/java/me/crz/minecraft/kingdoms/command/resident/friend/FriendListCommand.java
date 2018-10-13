package me.crz.minecraft.kingdoms.command.resident.friend;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Bukkit;

import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class FriendListCommand extends SubCommand {
    public FriendListCommand(SubCommand parent) {
        super(parent, "list");
        setPermission("kingdoms.user");
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

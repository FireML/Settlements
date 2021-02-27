package me.crz.minecraft.kingdoms.command.friend;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import org.bukkit.Bukkit;

import java.util.stream.Collectors;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.PlayerSubCommand;

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

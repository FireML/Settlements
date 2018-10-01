package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

import java.util.stream.Collectors;

public class VillageAlliesCommand extends SubCommand {
    public VillageAlliesCommand() {
        super("lists all your allies", "allies");
        this.setPermission("village.user");
    }

    @Override
    public void run(Player p, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (village == null) {
            MessageUtils.tell(p, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        MessageUtils.tell(p, "&8&l--------&r&c&lAllies&r&8&l--------\n&a"
                + village.getAllies().stream()
                .map(id -> VillageManager.getInstance().getVillageFromId(id).getName())
                .collect(Collectors.joining("&7, &a")));
    }
}

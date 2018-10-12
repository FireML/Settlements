package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ExecuteMoveUtil;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageHomeCommand extends SubCommand {
    public VillageHomeCommand() {
        super("Teleport to a settlement's home", "home");
        this.setPermission("kingdoms.user");
        this.setUsage("home [<name>]");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village;
        Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(player);
        if (args.length < 1) {
            if (playerVillage == null) {
                MessageUtils.tell(player, Message.get("not-in-current"));
                return;
            } else if (playerVillage.getVillageHome() == null) {
                MessageUtils.tell(player, "&c&lYour village doesn't have a home ask your " + VillageRank.CHIEF.getTag(playerVillage.getState()) + " to set one!");
                return;
            } else {
                village = playerVillage;
            }
        } else {
            village = VillageManager.getInstance().getVillageFromName(args[0]);
            if (village == null) {
                MessageUtils.tell(player, Message.get("error-village-doesnt-exist"));
                return;
            }
        }

        if ((village.getAccess() == VillageAccess.PRIVATE && !village.equals(playerVillage))
                || (village.getAccess() == VillageAccess.ALLY && (playerVillage == null || !village.getAllies().contains(playerVillage.getId())))) {
            MessageUtils.tell(player, "&c&lYou don't have access to teleport to the home of that " + village.getState().humanizedName() + "!");
            return;
        }

        if (village.getVillageHome() != null) {
            Location loc = village.getVillageHome().toBukkitLocation();
            MessageUtils.tell(player, "&e&lTeleporting in 5 seconds...");
            ExecuteMoveUtil.getInstance().execute(player, () -> player.teleport(loc), 3, "Teleport");
        } else {
            MessageUtils.tell(player, "&c&lThat " + village.getState().humanizedName() + " doesn't have a home!");
        }
    }
}

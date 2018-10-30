package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class VillageHomeCommand extends SubCommand {
    public VillageHomeCommand(SubCommand parent) {
        super(parent, "home [<name>]");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Village village;
        Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(player);
        if (args.length < 1) {
            if (playerVillage == null) {
                MessageUtils.tell(player, getError("village-required"));
                return false;
            } else if (playerVillage.getVillageHome() == null) {
                MessageUtils.tell(player, getMessage("own-no-home"), "chief", VillageRank.CHIEF.getTag(playerVillage));
                return true;
            } else {
                village = playerVillage;
            }
        } else {
            village = VillageManager.getInstance().getVillageFromName(args[0]);
            if (village == null) {
                MessageUtils.tell(player, getError("village-doesnt-exist"));
                return true;
            }
        }

        if (!village.equals(playerVillage)
                && (village.getAccess() == VillageAccess.PRIVATE
                || village.getAccess() == VillageAccess.ALLY && (playerVillage == null || !village.getAllies().contains(playerVillage.getId())))) {
            MessageUtils.tell(player, getMessage("no-access"), "state", village.getState().getTag());
            return true;
        }

        if (village.getVillageHome() != null) {
            Utils.teleport(player, village.getVillageHome().toBukkitLocation(), village.getState().getKey(), getConfigPath(),
                    "state", village.getState().getTag(), "name", village.getName());
        } else {
            MessageUtils.tell(player, getMessage("no-home"), "state", village.getState().getTag());
        }
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class VillageHereCommand extends AbstractVillageInfoCommand {

    public VillageHereCommand(SubCommand parent) {
        super(parent, "here");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageAt(player.getLocation());
        if (village == null) {
            MessageUtils.tell(player, getError("no-village"));
            return false;
        }
        Utils.runAsync(() -> MessageUtils.tell(player, getMessage("general"), getReplacements(village)));
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class VillageShowCommand extends AbstractVillageInfoCommand {

    public VillageShowCommand(SubCommand parent) {
        super(parent, "show [<name>]");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length == 0) {
            Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            if (village == null) {
                MessageUtils.tell(player, getError("not-in-village"));
                return false;
            }
            Utils.runAsync(() -> MessageUtils.tell(player, getMessage("general"), getReplacements(village)));
        } else if (args.length == 1) {
            String name = args[0];
            Utils.runAsync(() -> {
                Village village2 = VillageManager.getInstance().getVillageFromName(name);
                Resident target = ResidentManager.getInstance().getResident(name);
                if (target != null) {
                    Village playerVillage = VillageManager.getInstance().getVillageFromResident(target);
                    MessageUtils.tell(player, getMessage("general"), getReplacements(playerVillage));
                } else if (village2 != null) {
                    MessageUtils.tell(player, getMessage("general"), getReplacements(village2));
                } else {
                    MessageUtils.tell(player, getError("village-doesnt-exist"), "name", args[0]);
                }
            });
        } else {
            return false;
        }
        return true;
    }
}

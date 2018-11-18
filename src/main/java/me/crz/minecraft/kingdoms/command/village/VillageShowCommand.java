package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class VillageShowCommand extends SubCommand {

    public VillageShowCommand(SubCommand parent) {
        super(parent, "show [<name>]", "info");
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
            Utils.runAsync(() -> MessageUtils.tell(player, getMessage("general"), village.getReplacements(VillageShowCommand.this)));
        } else if (args.length == 1) {
            String name = String.join(" ", args);
            Utils.runAsync(() -> {
                Village village = VillageManager.getInstance().getVillageFromName(name);
                Resident target = ResidentManager.getInstance().getResident(name);
                if (target != null && target.hasVillage()) {
                    MessageUtils.tell(player, getMessage("general"), target.getVillage().getReplacements(VillageShowCommand.this));
                } else if (village != null) {
                    MessageUtils.tell(player, getMessage("general"), village.getReplacements(VillageShowCommand.this));
                } else {
                    MessageUtils.tell(player, getError("village-doesnt-exist"), "name", name);
                }
            });
        } else {
            return false;
        }
        return true;
    }
}

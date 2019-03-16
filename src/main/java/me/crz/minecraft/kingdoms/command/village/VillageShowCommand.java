package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VillageShowCommand extends SubCommand {

    public VillageShowCommand(SubCommand parent) {
        super(parent, "show [<name>]", "info");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length == 0 && sender instanceof Player) {
            Village village = VillageManager.getInstance().getVillageFromPlayer((Player) sender);
            if (village == null) {
                MessageUtils.tell(sender, getError("not-in-village"));
                return false;
            }
            Utils.runAsync(() -> MessageUtils.tell(sender, getMessage("general"), village.getReplacements(VillageShowCommand.this)));
        } else if (args.length == 1) {
            String name = String.join(" ", args);
            Village village = VillageManager.getInstance().getVillageFromName(name);
            if (village != null) {
                getMessage("general").tell(sender, village.getReplacements(VillageShowCommand.this));
                return true;
            }
            Utils.runAsync(() -> {
                Resident target = ResidentManager.getInstance().getResident(name);
                if (target != null && target.hasVillage()) {
                    MessageUtils.tell(sender, getMessage("general"), target.getVillage().getReplacements(VillageShowCommand.this));
                } else {
                    MessageUtils.tell(sender, getError("village-doesnt-exist"), "name", name);
                }
            });
        } else {
            return false;
        }
        return true;
    }
}

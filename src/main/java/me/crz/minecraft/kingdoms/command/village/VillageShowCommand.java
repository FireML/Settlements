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
            getMessage(village.hasEmpire() ? "empire" : village.isAbandoned() ? "abandoned" : "general")
                    .tell(sender, village.getReplacements(this));
        } else if (args.length == 1) {
            String name = String.join(" ", args);
            Village village = VillageManager.getInstance().getVillageFromName(name);
            if (village != null) {
                getMessage(village.hasEmpire() ? "empire" : village.isAbandoned() ? "abandoned" : "general")
                        .tell(sender, village.getReplacements(this));
                return true;
            }
            Utils.runAsync(() -> {
                Resident target = ResidentManager.getInstance().getResident(name);
                if (target != null && target.hasVillage()) {
                    getMessage(target.hasEmpire() ? "empire" : target.getVillage().isAbandoned() ? "abandoned" : "general")
                            .tell(sender, target.getVillage().getReplacements(this));
                } else {
                    getError("village-doesnt-exist").tell(sender, "name", name);
                }
            });
        } else {
            return false;
        }
        return true;
    }
}

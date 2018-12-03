package me.crz.minecraft.kingdoms.chat.commands;

import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class IgnoreCommand extends PlayerSubCommand {

    public IgnoreCommand() {
        super(null, "ignore list|<player>");
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Resident res = ResidentManager.getInstance().getResident(p);
        Utils.runAsync(() -> {
            if (args[0].equalsIgnoreCase("list")) {
                MessageUtils.tell(p, getMessage("list.head"));
                MessageUtils.tell(p, res.getIgnore().stream()
                        .map(id -> {
                            Resident resident = ResidentManager.getInstance().getResident(id);
                            if (resident != null) {
                                return getMessage("list.entry").getReplacedText("name", resident.getName());
                            } else {
                                return getMessage("list.entry").getReplacedText("name", id.toString());
                            }
                        }).collect(Collectors.joining(getText("list.separator")))
                );
                return;
            }
            Resident target = ResidentManager.getInstance().getResident(args[0]);
            if (target != null) {
                if (res.getIgnore().contains(target.getUUID())) {
                    res.removeIgnore(target.getUUID());
                    MessageUtils.tell(p, getMessage("unignored"), "name", target.getName());
                } else {
                    res.addIgnore(target.getUUID());
                    MessageUtils.tell(p, getMessage("ignored"), "name", target.getName());
                }
                DatabaseManager.getInstance().saveResident(res);
                return;
            }
            MessageUtils.tell(p, getError("player-not-found"), "name", args[0]);
        });
        return true;
    }
}

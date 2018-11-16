package me.crz.minecraft.kingdoms.command.lock;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.TargetParser;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LockRemoveCommand extends SubCommand implements TargetParser {
    public LockRemoveCommand(LockCommand parent) {
        super(parent, "remove <player>|<settlement:name>|<empire:name> [toggle]", "revoke");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (args.length > 0) {
            Utils.runAsync(() -> {
                boolean toggle = args.length > 1 && "toggle".equalsIgnoreCase(args[args.length - 1]);
                String[] actionArgs = Arrays.copyOfRange(args, 0, args.length - (toggle ? 1 : 0));
                Targetable target = getTarget(player, actionArgs);
                if (target != null) {
                    if (toggle) {
                        if (LockManager.getInstance().toggleAction(player.getUniqueId(), "remove " + String.join(" ", actionArgs))) {
                            getMessage("toggled").tell(player, "name", target.getName(), "target", String.join(" ", actionArgs));
                        } else {
                            getMessage("untoggled").tell(player, "name", target.getName());
                        }
                    } else {
                        LockManager.getInstance().addAction(player.getUniqueId(), "remove " + String.join(" ", actionArgs));
                        getMessage("click-to-remove").tell(player, "name", target.getName());
                    }
                }
            });
        }
        return false;
    }
}

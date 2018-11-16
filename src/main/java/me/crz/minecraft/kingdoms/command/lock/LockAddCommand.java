package me.crz.minecraft.kingdoms.command.lock;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.TargetParser;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LockAddCommand extends SubCommand implements TargetParser {
    public LockAddCommand(LockCommand parent) {
        super(parent, "add <player>|<settlement:name>|<empire:name> [toggle]", "grant");
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
                        if (LockManager.getInstance().toggleAction(player.getUniqueId(), "add " + String.join(" ", actionArgs))) {
                            getMessage("toggled").tell(player, "name", target.getName(), "target", String.join(" ", actionArgs));
                        } else {
                            getMessage("untoggled").tell(player, "name", target.getName());
                        }
                    } else {
                        LockManager.getInstance().addAction(player.getUniqueId(), "add " + String.join(" ", actionArgs));
                        getMessage("click-to-add").tell(player, "name", target.getName());
                    }
                }
            });
        }
        return true;
    }
}

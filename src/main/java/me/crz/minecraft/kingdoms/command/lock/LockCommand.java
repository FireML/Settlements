package me.crz.minecraft.kingdoms.command.lock;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;

public class LockCommand extends ResidentSubCommand {
    public LockCommand() {
        super(null, "lock", "protection", "secure");
        setPermission("kingdoms.user");
        getHelpCommand();
        registerCommand(new SubCommand(this, "toggle") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                if (LockManager.getInstance().toggleAction(player.getUniqueId(), "lock")) {
                    getMessage("toggled").tell(player);
                } else {
                    getMessage("untoggled").tell(player);
                }
                return true;
            }
        });
        registerCommand(new SubCommand(this, "info") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                if (args.length > 0 && "toggle".equalsIgnoreCase(args[0])) {
                    if (LockManager.getInstance().toggleAction(player.getUniqueId(), "info")) {
                        getMessage("toggled").tell(player);
                    } else {
                        getMessage("untoggled").tell(player);
                    }
                } else {
                    LockManager.getInstance().addAction(player.getUniqueId(), "info");
                    getMessage("click-for-info").tell(player);
                }
                return true;
            }
        });
        registerCommand(new SubCommand(this, "hoppers", "hopperallow", "allowhoppers") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                if (args.length > 0 && "toggle".equalsIgnoreCase(args[0])) {
                    if (LockManager.getInstance().toggleAction(player.getUniqueId(), "hoppers")) {
                        getMessage("toggled").tell(player);
                    } else {
                        getMessage("untoggled").tell(player);
                    }
                } else {
                    LockManager.getInstance().addAction(player.getUniqueId(), "hoppers");
                    getMessage("click-to-toggle").tell(player);
                }
                return true;
            }
        });
        registerCommand(new LockAddCommand(this));
        registerCommand(new LockRemoveCommand(this));
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        LockManager.getInstance().addAction(res.getUUID(), "lock");
        getMessage("click-to-lock").tell(res);
        return true;
    }
}

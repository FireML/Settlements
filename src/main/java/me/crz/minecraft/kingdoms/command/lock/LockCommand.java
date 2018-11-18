package me.crz.minecraft.kingdoms.command.lock;

import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.lock.LockManager;
import org.bukkit.entity.Player;

public class LockCommand extends PlayerSubCommand {
    public LockCommand() {
        super(null, "lock", "protection", "secure");
        setPermission("kingdoms.user");
        getHelpCommand();
        registerCommand(new PlayerSubCommand(this, "toggle") {
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
        registerCommand(new PlayerSubCommand(this, "info") {
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
        registerCommand(new PlayerSubCommand(this, "hoppers", "hopperallow", "allowhoppers") {
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
    public boolean run(Player player, String base, String alias, String... args) {
        LockManager.getInstance().addAction(player.getUniqueId(), "lock");
        getMessage("click-to-lock").tell(player);
        return true;
    }
}

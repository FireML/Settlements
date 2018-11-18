package me.crz.minecraft.kingdoms.command.lock;

import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.lock.LockManager;
import org.bukkit.entity.Player;

public class UnlockCommand extends PlayerSubCommand {
    public UnlockCommand() {
        super(null, "unlock", "unprotection", "unsecure");
        setPermission("kingdoms.user");
        registerCommand(new PlayerSubCommand(this, "toggle") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                if (LockManager.getInstance().toggleAction(player.getUniqueId(), "unlock")) {
                    getMessage("toggled").tell(player);
                } else {
                    getMessage("untoggled").tell(player);
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        LockManager.getInstance().addAction(player.getUniqueId(), "unlock");
        getMessage("click-to-unlock").tell(player);
        return true;
    }
}

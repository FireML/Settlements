package me.crz.minecraft.kingdoms.command.lock;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.lock.LockManager;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;

public class UnlockCommand extends ResidentSubCommand {
    public UnlockCommand() {
        super(null, "unlock", "unprotection", "unsecure");
        setPermission("kingdoms.user");
        registerCommand(new SubCommand(this, "toggle") {
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
    public boolean run(Resident res, String base, String alias, String... args) {
        LockManager.getInstance().addAction(res.getUUID(), "unlock");
        getMessage("click-to-unlock").tell(res);
        return true;
    }
}

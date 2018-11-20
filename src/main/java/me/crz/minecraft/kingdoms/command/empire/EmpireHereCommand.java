package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.entity.Player;

public class EmpireHereCommand extends PlayerSubCommand {

    public EmpireHereCommand(SubCommand parent) {
        super(parent, "here");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
        if (empire == null) {
            getError("not-in-empire").tell(player);
            return true;
        }
        Utils.runAsync(() -> getMessage("general").tell(player, empire.getReplacements(EmpireHereCommand.this)));
        return true;
    }
}

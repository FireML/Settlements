package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.entity.Player;

public class EmpireSetHomeCommand extends EmpireSubCommand {
    public EmpireSetHomeCommand(SubCommand parent) {
        super(parent, "sethome");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Empire empire = res.getVillage().getEmpire();
        Player player = res.getPlayer();
        if (!player.getWorld().getName().equals(empire.getWorldName())) {
            getError("not-in-empire-world").tell(res);
            return true;
        }

        empire.setHome(player.getLocation());
        MessageUtils.tellResidents(empire.getOnlineResidents(), getMessage("success"),
                "rank", res.getEmpireRank().getTag(),
                "title", res.getVillageTitle(),
                "displaytitle", res.getDisplayTitle(),
                "name", player.getName(),
                "state", res.getVillage().getState().getTag(),
                "world", player.getLocation().getWorld().getName(),
                "x", String.valueOf(player.getLocation().getBlockX()),
                "y", String.valueOf(player.getLocation().getBlockY()),
                "z", String.valueOf(player.getLocation().getBlockZ()));
        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(empire));
        return true;
    }
}

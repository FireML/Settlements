package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.utils.MessageUtils;
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
        DatabaseManager.getInstance().saveEmpire(empire);
        return true;
    }
}

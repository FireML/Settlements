package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.world.NamedLocation;
import org.bukkit.entity.Player;

public class SettlementSetHomeCommand extends SettlementSubCommand {
    public SettlementSetHomeCommand(SubCommand parent) {
        super(parent, "sethome");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Settlement settlement = res.getSettlement();
        Player player = res.getPlayer();
        OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
        if (land == null || !land.getSettlement().equals(settlement)) {
            getError("not-your-settlement").tell(res, "state", res.getSettlement().getState().getTag());
            return true;
        }

        if (settlement.getHome() != null && !land.containsHome()) {
            OwnedLand home = LandManager.getInstance().getLand(settlement.getHome().toChunkLocation());
            if (home != null) {
                if (land.isOutpost()) {
                    NamedLocation outpost = settlement.getOutpostHomes().get(land.getOutpostName().toLowerCase());
                    if (outpost != null) {
                        land.setOutpost(null);
                        outpost.copy(settlement.getHome());
                        home.setOutpost(outpost.getName());
                        getMessage("moved-outpost").tell(settlement.getOnlineResidents(),
                                "rank", res.getSettlementRank().getTag(settlement),
                                "title", res.getSettlementTitle(),
                                "displaytitle", res.getDisplayTitle(),
                                "name", player.getName(),
                                "outpost", outpost.getName(),
                                "state", settlement.getState().getTag(),
                                "world", outpost.getWorld(),
                                "x", String.valueOf(outpost.getBlockX()),
                                "y", String.valueOf(outpost.getBlockY()),
                                "z", String.valueOf(outpost.getBlockZ()));
                    }
                }

                if (!home.isOutpost() && LandManager.getInstance().getAllConnectedLand(home,
                        l -> l.equals(land) || l.isOutpost() ? LandManager.FilterResponse.RETURN : LandManager.FilterResponse.CONTINUE)
                        .isEmpty()) {
                    getError("abandoned-land").tell(res, "state", res.getSettlement().getState().getTag());
                    return true;
                }
            }
        }

        settlement.setHome(player.getLocation());
        getMessage("success").tell(settlement.getOnlineResidents(),
                "rank", res.getSettlementRank().getTag(settlement),
                "title", res.getSettlementTitle(),
                "name", player.getName(),
                "state", settlement.getState().getTag(),
                "world", player.getLocation().getWorld().getName(),
                "x", String.valueOf(player.getLocation().getBlockX()),
                "y", String.valueOf(player.getLocation().getBlockY()),
                "z", String.valueOf(player.getLocation().getBlockZ()));
        DatabaseManager.getInstance().saveSettlement(settlement);
        return true;
    }
}

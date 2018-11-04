package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlotLinkCommand extends ResidentSubCommand {
    public PlotLinkCommand(VillagePlotCommand parent) {
        super(parent, "link", "connect");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            getError("not-a-plot").tell(res);
        } else if (!(land.isPlotOwner(res.getUUID()) || (land.isOwnedBy(res.getVillage()) && res.getVillageRank().atLeast(VillageRank.ELDER)))) {
            getError("not-your-plot").tell(res);
        } else {
            Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(land, search -> {
                if (!search.isPlotOwner(res)) {
                    return LandManager.FilterResponse.SKIP_ENTRY;
                }
                return LandManager.FilterResponse.ACCEPT;
            });

            Optional<OwnedLand> syncWith = connected.stream().filter(OwnedLand::hasLink).findAny();
            getMessage("success").tell(res, "amount", String.valueOf(connected.size()));
            Set<OwnedLand> toSave = new LinkedHashSet<>();
            if (syncWith.isPresent()) {
                connected.add(land);
            } else {
                land.setLink(UUID.randomUUID());
                toSave.add(land);
            }
            for (OwnedLand c : connected) {
                if (LandManager.getInstance().addLink(c, syncWith.orElse(land).getLink())) {
                    toSave.add(c);
                    c.syncLinked(syncWith.orElse(land));
                }
            }
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
        }
        return true;
    }
}

package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlotLinkCommand extends PlotSubCommand {
    public PlotLinkCommand(PlotCommand parent) {
        super(parent, "link", "connect");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(land, search -> {
            if (!search.isPlotOwner(res)) {
                return LandManager.FilterResponse.SKIP_ENTRY;
            }
            return LandManager.FilterResponse.ACCEPT;
        });

        if (connected.isEmpty()) {
            getError("none-connected").tell(res);
            return false;
        }

        Optional<OwnedLand> syncWith = connected.stream().filter(OwnedLand::hasLink).findAny();
        getMessage("success").tell(res, "amount", String.valueOf(connected.size()));
        Set<OwnedLand> toSave = new LinkedHashSet<>();
        if (syncWith.isPresent()) {
            connected.add(land);
        } else {
            LandManager.getInstance().addLink(land, UUID.randomUUID());
            toSave.add(land);
        }
        for (OwnedLand c : connected) {
            if (LandManager.getInstance().addLink(c, syncWith.orElse(land).getLink())) {
                toSave.add(c);
                c.syncLinked(syncWith.orElse(land));
            }
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
        return true;
    }
}

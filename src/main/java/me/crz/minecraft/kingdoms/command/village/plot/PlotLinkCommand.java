package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Collection;
import java.util.LinkedHashSet;
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
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (!(land.isEmbassy() || res.getVillage().equals(land.getOwnerVillage()))) {
            res.tell(getError("not-your-village"), "state", land.getOwnerVillage().getState().getTag());
        } else if (!land.isPlotOwner(res.getUUID())) {
            getError("not-your-plot").tell(res);
        } else {
            Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(land, search -> {
                if (!search.isPlotOwner(res)) {
                    return LandManager.FilterResponse.SKIP_ENTRY;
                }
                return LandManager.FilterResponse.ACCEPT;
            });


            UUID link = connected.stream()
                    .filter(OwnedLand::hasLink)
                    .map(OwnedLand::getLink)
                    .findAny().orElse(UUID.randomUUID());
            getMessage("success").tell(res, "amount", String.valueOf(connected.size()));
            connected.add(land); // add to save
            Set<OwnedLand> toSave = new LinkedHashSet<>();
            for (OwnedLand c : connected) {
                if (LandManager.getInstance().addLink(c, link)) {
                    toSave.add(c);
                }
            }
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
        }
        return true;
    }
}

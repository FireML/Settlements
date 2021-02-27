package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.ChatMap;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;

import java.util.HashSet;
import java.util.Set;

public class PlotUnlinkCommand extends PlotSubCommand {
    public PlotUnlinkCommand(PlotCommand parent) {
        super(parent, "unlink [map|<x> <z>]", "disconnect");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length == 0) {
            if (!land.hasLink()) {
                getError("none-linked").tell(res, land.getReplacements());
            } else {
                int linked = land.getLinked().size() - 1;
                getMessage("disconnected").tell(res, "amount", String.valueOf(linked));
                if (land.removeLink()) {
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                }
            }
            return true;
        } else if (args.length == 1 && "map".equalsIgnoreCase(args[0])) {
            Utils.runAsync(() -> new ChatMap(res.getPlayer(), land.getMapKey()));
        } else if (args.length >= 2) {
            try {
                int x = Integer.parseInt(args[0]);
                int z = Integer.parseInt(args[1]);

                OwnedLand targetLand = LandManager.getInstance().getLand(new SimpleChunkLocation(land.getWorldName(), x, z));
                if (targetLand != null && (targetLand.isPlotOwner(res) || targetLand.canAdmin(res))) {
                    if (targetLand.isLinked(land)) {
                        int linked = targetLand.getLinked().size() - 1;
                        getMessage("disconnected").tell(res, "amount", String.valueOf(linked));
                        if (targetLand.removeLink()) {
                            Set<OwnedLand> toSave = new HashSet<>();
                            toSave.add(targetLand);
                            if (linked == 1) {
                                land.removeLink();
                                toSave.add(land);
                            }
                            Utils.runAsync(() -> {
                                DatabaseManager.getInstance().saveLand(toSave);
                                if (args.length >= 3 && "map".equalsIgnoreCase(args[2])) {
                                    new ChatMap(res.getPlayer(), land.getMapKey());
                                }
                            });
                        }
                    } else {
                        getError("not-linked").tell(res, "x", String.valueOf(x), "z", String.valueOf(z));
                    }
                } else {
                    getError("not-your-plot").tell(res, "x", String.valueOf(x), "z", String.valueOf(z));
                }
                return true;
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(res, "input", args[0] + " or " + args[1]);
            }
        }
        return false;
    }
}

package de.themoep.settlements.command.plot;

import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.ChatMap;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.world.SimpleChunkLocation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlotLinkCommand extends PlotSubCommand {
    public PlotLinkCommand(PlotCommand parent) {
        super(parent, "link all|map|<x> <z>", "connect");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length == 1 && "map".equalsIgnoreCase(args[0])) {
            Utils.runAsync(() -> new ChatMap(res.getPlayer(), land.getSettlement(), land.getMapKey()));
        } else if (args.length == 1 && "all".equalsIgnoreCase(args[0])) {
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
        } else if (args.length >= 2) {
            try {
                int x = Integer.parseInt(args[0]);
                int z = Integer.parseInt(args[1]);

                OwnedLand targetLand = LandManager.getInstance().getLand(new SimpleChunkLocation(land.getWorldName(), x, z));
                if (targetLand != null && (targetLand.isPlotOwner(res) || targetLand.canAdmin(res))) {
                    Collection<OwnedLand> toSave = new LinkedHashSet<>();
                    if (targetLand.hasLink()) {
                        if (LandManager.getInstance().addLink(land, targetLand.getLink())) {
                            toSave.add(land);
                            land.syncLinked(targetLand);
                        }
                    } else if (land.hasLink()) {
                        if (LandManager.getInstance().addLink(targetLand, land.getLink())) {
                            toSave.add(targetLand);
                            targetLand.syncLinked(land);
                        }
                    } else {
                        UUID link = UUID.randomUUID();
                        LandManager.getInstance().addLink(land, link);
                        toSave.add(land);
                        LandManager.getInstance().addLink(targetLand, link);
                        toSave.add(targetLand);
                        targetLand.syncLinked(land);
                    }

                    getMessage("success-coords").tell(res,
                            "id", land.getLink().toString(),
                            "x", String.valueOf(targetLand.getX()),
                            "z", String.valueOf(targetLand.getZ())
                    );
                    Collection<OwnedLand> linked = land.getLinked();
                    if (linked.size() > 2) {
                        getMessage("success-existing").tell(res,
                                "id", land.getLink().toString(),
                                "amount", String.valueOf(linked.size() - 1),
                                "linked", linked.stream().filter(o -> !o.equals(land)).map(o -> o.getX() + "/" + o.getZ()).collect(Collectors.joining("\n")),
                                "x", String.valueOf(land.getX()),
                                "z", String.valueOf(land.getZ())
                        );
                    }
                    Utils.runAsync(() -> {
                        DatabaseManager.getInstance().saveLand(toSave);
                        if (args.length >= 3 && "map".equalsIgnoreCase(args[2])) {
                            new ChatMap(res.getPlayer(), land.getSettlement(), land.getMapKey());
                        }
                    });
                } else {
                    getError("not-your-plot");
                }
                return true;
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(res, "input", args[0] + " or " + args[1]);
            }
        }
        return false;
    }
}

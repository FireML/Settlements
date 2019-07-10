package me.crz.minecraft.kingdoms.command.village.outpost;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Chunk;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class OutpostUnclaimCommand extends VillageSubCommand {

    private Cache<UUID, Runnable> confirm = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public OutpostUnclaimCommand(VillageSubCommand parent) {
        super(parent, "unclaim");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 1 && "confirm".equalsIgnoreCase(args[0])) {
            Runnable run = confirm.getIfPresent(resident.getUUID());
            if (run != null) {
                confirm.invalidate(resident.getUUID());
                run.run();
            } else {
                getError("nothing-to-confirm").tell(resident);
            }
            return true;
        }

        Chunk currentChunk = resident.getPlayer().getLocation().getChunk();
        OwnedLand land = LandManager.getInstance().getLand(currentChunk);
        if (land == null || !land.isOwnedBy(resident.getVillage())) {
            resident.tell(getError("not-your-village"), "state", resident.getVillage().getState().getTag());
            return true;
        }
        if (!land.isOutpost()) {
            resident.tell(getError("not-an-outpost"));
            return true;
        }

        Collection<OwnedLand> toUnclaim = LandManager.getInstance().getAllConnectedLand(land, search -> {
            if (search.isOutpost()) {
                // skip branch if this is connected to another outpost chunk
                getMessage("connected-to-outpost").tell(resident);
                return LandManager.FilterResponse.SKIP_BRANCH;
            } else if (search.containsHome()) {
                // skip branch if this is connected to the home chunk
                getMessage("connected-to-home").tell(resident);
                return LandManager.FilterResponse.SKIP_BRANCH;
            }
            return LandManager.FilterResponse.ACCEPT;
        });

        toUnclaim.add(land);
        Runnable unclaim = () -> {
            LandManager.getInstance().unclaimLand(toUnclaim);
            MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"),
                    "amount", String.valueOf(toUnclaim.size() - 1),
                    "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                    "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ()));
        };
        if (toUnclaim.size() == 1) {
            unclaim.run();
        } else {
            confirm.put(resident.getUUID(), unclaim);
            getMessage("confirm").tell(resident,
                    "command", base + " " + alias + " confirm",
                    "amount", String.valueOf(toUnclaim.size()),
                    "claims", toUnclaim.stream().map(l -> l.getX() + "-" + l.getZ()).collect(Collectors.joining(", "))
            );
        }
        return true;
    }
}

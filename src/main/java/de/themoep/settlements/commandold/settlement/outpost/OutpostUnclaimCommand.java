package de.themoep.settlements.commandold.settlement.outpost;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.commandold.settlement.SettlementSubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.MessageUtils;
import org.bukkit.Chunk;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class OutpostUnclaimCommand extends SettlementSubCommand {

    private final Cache<UUID, Runnable> confirm = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build();

    public OutpostUnclaimCommand(SettlementSubCommand parent) {
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
        if (land == null || !land.isOwnedBy(resident.getSettlement())) {
            resident.tell(getError("not-your-settlement").getText(), "state", resident.getSettlement().getState().getTag());
            return true;
        }
        if (!land.isOutpost()) {
            resident.tell(getError("not-an-outpost").getText());
            return true;
        }

        AtomicBoolean connectedToOutpost = new AtomicBoolean(false);
        AtomicBoolean connectedToHome = new AtomicBoolean(false);
        Collection<OwnedLand> toUnclaim = LandManager.getInstance().getAllConnectedLand(land, search -> {
            if (search.isOutpost()) {
                // skip branch if this is connected to another outpost chunk
                connectedToOutpost.set(true);
                return LandManager.FilterResponse.SKIP_BRANCH;
            }
            if (search.containsHome()) {
                // skip branch if this is connected to the home chunk
                connectedToHome.set(true);
                return LandManager.FilterResponse.SKIP_BRANCH;
            }
            return LandManager.FilterResponse.ACCEPT;
        });

        if (connectedToHome.get()) {
            getMessage("connected-to-home").tell(resident);
        } else if (connectedToOutpost.get()) {
            getMessage("connected-to-outpost").tell(resident);
        }

        toUnclaim.add(land);
        Runnable unclaim = () -> {
            LandManager.getInstance().unclaimLand(toUnclaim);
            MessageUtils.tellResidents(resident.getSettlement().getOnlineResidents(), getMessage("success").getReplacedComponent(
                    "amount", String.valueOf(toUnclaim.size() - 1),
                    "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                    "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ())
            ));
        };
        if (toUnclaim.size() == 1) {
            unclaim.run();
        }
        return true;
    }
}

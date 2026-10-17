package de.themoep.settlements.command.common;

import de.themoep.settlements.land.Banished;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.gui.BanishedGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public interface BanishCommand extends ConfigMessages, TargetParser {

    default boolean listBanished(Resident resident, Settlement settlement) {
        new BanishedGUI(resident, settlement).open();
        return true;
    }

    default boolean banish(Resident resident, Settlement source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (resident.getName().equalsIgnoreCase(args[0])) {
            resident.tell(getError("cant-banish-yourself").getText());
            return true;
        }
        Utils.runAsync(() -> {
            Targetable target = getTarget(resident, args[0]);
            if (target == null) {
                return;
            }

            if (target.isMember(resident)) {
                getError("cant-banish-yourself").tell(resident);
                return;
            }

            if (source.isChief(target)) {
                getError("cant-banish-leader").tell(resident, "name", target.getName());
                return;
            }

            if (target instanceof Resident
                    && source.isMember((Resident) target)
                    && ((Resident) target).getSettlementRank().atLeast(resident.getSettlementRank())) {
                resident.tell(getError("cant-banish").getText(), "name", target.getName(), "title", ((Resident) target).getSettlementTitle());
                return;
            }
            Utils.runSync(() -> {
                String reason = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                if (!source.addBanished(resident, target, reason)) {
                    getError("already-banished").tell(resident,
                            "type", target instanceof Settlement ? ((Settlement) target).getStateTag() : "Player",
                            "name", target.getName());
                    return;
                }

                getMessage("banished").tell(source.getOnlineResidents(),
                        "type", target instanceof Settlement ? ((Settlement) target).getStateTag() : "Player",
                        "name", target.getName(),
                        "reason", reason);
                for (Resident r : target.getOnlineResidents()) {
                    Player p = r.getPlayer();
                    if (p != null) {
                        OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                        if (land != null && land.isOwnedBy(source)) {
                            Utils.teleportToSpawn(p);
                        }
                    }
                }
                source.removeMember(target);
                source.save();
            });
        });
        return true;
    }

    default boolean unbanish(Resident resident, Settlement source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Targetable target = getTarget(resident, String.join(" ", args));
            if (target == null) {
                return;
            }

            Utils.runSync(() -> {
                if (!source.removeBanished(resident, target)) {
                    getError("not-banished").tell(resident,
                            "type", target instanceof Settlement ? ((Settlement) target).getStateTag() : "Player",
                            "name", target.getName());
                    return;
                }

                getMessage("unbanished").tell(source.getOnlineResidents(),
                        "type", target instanceof Settlement ? ((Settlement) target).getStateTag() : "Player",
                        "target", target.getName());
                source.save();
            });
        });
        return true;
    }
}

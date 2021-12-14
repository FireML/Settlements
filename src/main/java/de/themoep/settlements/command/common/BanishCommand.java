package de.themoep.settlements.command.common;

import de.themoep.settlements.land.Banished;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.gui.BanishedGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public interface BanishCommand extends ConfigMessages, TargetParser {

    default boolean listBanished(Resident resident, LandOwner<?, ?> landOwner) {
        new BanishedGUI(landOwner.getKey(), resident, landOwner).open();
        if (true) return true;
        Utils.runAsync(() -> {
            resident.tell(getMessage("head"));
            if (!landOwner.getBanished().isEmpty()) {
                resident.tell(landOwner.getBanished().stream()
                        .map(Banished::getTargetable)
                        .map(entry -> {
                            if (entry instanceof Resident) {
                                return getMessage("entry").getReplacedText("name", entry.getName(), "title", ((Resident) entry).getSettlementTitle());
                            } else if (entry instanceof LandOwner) {
                                return getMessage("entry").getReplacedText("name", entry.getName(), "title", ((LandOwner<?, ?>) entry).getStateTag());
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(getMessage("separator").getText())));
            }
        });
        return true;
    }

    default boolean banish(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (resident.getName().equalsIgnoreCase(args[0])) {
            resident.tell(getError("cant-banish-yourself"));
            return true;
        }
        Utils.runAsync(() -> {
            Targetable target = getTarget(resident, args);
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
                resident.tell(getError("cant-banish"), "name", target.getName(), "title", ((Resident) target).getSettlementTitle());
                return;
            }
            Utils.runSync(() -> {
                String reason = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
                if (!source.addBanished(resident, target, reason)) {
                    getError("already-banished").tell(resident, "name", target.getName());
                    return;
                }

                getMessage("banished").tell(source.getOnlineResidents(), "name", target.getName(), "reason", reason);
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

    default boolean unbanish(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Targetable target = getTarget(resident, args);
            if (target == null) {
                return;
            }

            Utils.runSync(() -> {
                if (!source.removeBanished(resident, target)) {
                    getError("not-banished").tell(resident, "name", target.getName());
                    return;
                }

                getMessage("unbanished").tell(source.getOnlineResidents(), "target", target.getName());
                source.save();
            });
        });
        return true;
    }
}

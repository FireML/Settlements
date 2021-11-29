package de.themoep.settlements.command.common;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

import java.util.stream.Collectors;

public interface AllyCommand extends LandOwnerCommand {

    default boolean listAllies(Resident resident, LandOwner<?, ?> landOwner) {
        resident.tell(getMessage("head"));
        resident.tell(landOwner.getAllies().stream()
                .map(e -> getMessage("entry").getReplacedText("name", e.getName(), "state", e.getStateTag()))
                .collect(Collectors.joining(getMessage("separator").getText()))
        );
        return true;
    }

    default boolean addAlly(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        LandOwner target = getTarget(resident, args[0]);
        if (target == null) {
            return true;
        }

        if (target.equals(source) || target.equals(resident.getSettlement())) {
            resident.tell(getMessage("cant-ally-yourself"));
        } else if (source.isAlly(target)) {
            resident.tell(getMessage("already-allied"), "name", target.getName(), "state", target.getStateTag());
        } else if (InviteCache.hasPendingAlly(source, target)) {
            MessageUtils.tellResidents(target.getOnlineResidents(), getMessage("now-allied-to"), "name", source.getName(), "state", source.getStateTag());
            MessageUtils.tellResidents(source.getOnlineResidents(), getMessage("now-allied-to"), "name", target.getName(), "state", target.getStateTag());
            InviteCache.removePendingAlly(source, target);
            target.addAlly(source);
            source.addAlly(target);

            Utils.runAsync(() -> {
                DatabaseManager.getInstance().save(target);
                DatabaseManager.getInstance().save(source);
            });
        } else {
            InviteCache.addPendingAlly(target, source);
            MessageUtils.tellResidents(target.getOnlineResidents(), getMessage("invite"), "name", source.getName(), "state", source.getStateTag());
            MessageUtils.tellResidents(target.getOnlineResidents(), Message.get("command." + target.getKey() + ".ally.accept-info"), "name", source.getName(), "state", source.getStateTag());
            resident.tell(getMessage("request-send"), "name", target.getName(), "state", target.getStateTag());
        }
        return true;
    }

    default boolean removeAlly(Resident resident, LandOwner source, String[] args) {
        if (args.length < 1) {
            return false;
        }
        LandOwner target = getTarget(resident, args[0]);
        if (target == null) {
            return true;
        }

        if (target.equals(source) || target.equals(resident.getSettlement())) {
            resident.tell(getMessage("cant-unally-yourself"));
        } else if (!source.isAlly(target)) {
            resident.tell(getMessage("not-allied"), "name", target.getName(), "state", target.getStateTag());
        } else {
            source.removeAlly(target);
            target.removeAlly(source);
            if (!(source instanceof Settlement) || ((Settlement) source).isAllowAllyClaim()) {
                LandManager.getInstance().checkClaimDistance(source, target);
            }

            Utils.runAsync(() -> {
                DatabaseManager.getInstance().save(target);
                DatabaseManager.getInstance().save(source);
            });
            MessageUtils.tellResidents(target.getOnlineResidents(), getMessage("success"), "name", source.getName(), "state", source.getStateTag());
            MessageUtils.tellResidents(source.getOnlineResidents(), getMessage("success"), "name", target.getName(), "state", target.getStateTag());
        }
        return true;
    }
}

package de.themoep.settlements.command.common;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.integration.MailProvider;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.AllyRequest;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.gui.AlliesGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;

public interface AllyCommand extends LandOwnerCommand {

    default boolean listAllies(Resident resident, LandOwner<?, ?> landOwner) {
        if (landOwner.hasUnlocked("allies")) {
            new AlliesGUI(landOwner.getKey(), resident, landOwner).open();
        } else {
            Message.get("gui.unlockables.locked.npc").tell(resident);
        }
        return true;
    }

    default boolean addAlly(Resident resident, LandOwner source, String[] args) {
        if (!source.hasUnlocked("allies")) {
            Message.get("gui.unlockables.locked.npc").tell(resident);
            return true;
        }

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
        } else if (source.hasAllyRequest(target)) {
            AllyRequest request = source.removeAllyRequest(target);
            if (request != null) {
                Utils.runAsync(() -> DatabaseManager.getInstance().delete(request));
            }
            AllyRequest targetRequest = target.removeAllyRequest(source);
            if (targetRequest != null) {
                Utils.runAsync(() -> DatabaseManager.getInstance().delete(targetRequest));
            }
            target.addAlly(source).save();
            source.addAlly(target).save();
            getMessage("now-allied-to").tell(target, "name", source.getName(), "state", source.getStateTag());
            getMessage("now-allied-to").tell(source, "name", target.getName(), "state", target.getStateTag());
            for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                provider.sendMail(source.getStateTag() + " " + source.getName(), target.getChief(), getText("now-allied-to",
                        "name", source.getName(), "state", source.getStateTag()
                ));
                provider.sendMail(target.getStateTag() + " " + target.getName(), source.getChief(), getText("now-allied-to",
                        "name", target.getName(), "state", target.getStateTag()
                ));
            }
        } else {
            AllyRequest request = new AllyRequest(target, source);
            target.addAllyRequest(request);
            request.save();
            getMessage("invite").tell(target, "name", source.getName(), "state", source.getStateTag());
            Message.get("command." + target.getKey() + ".ally.accept-info").tell(target, "name", source.getName(), "state", source.getStateTag());
            getMessage("request-send").tell(resident, "name", target.getName(), "state", target.getStateTag());
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
        } else if (source.hasAllyRequest(target)) {

        } else if (!source.isAlly(target)) {
            getMessage("not-allied").tell(resident, "name", target.getName(), "state", target.getStateTag());
        } else {
            Ally sourceAlly = source.removeAlly(target);
            Ally targetAlly = target.removeAlly(source);
            if (!source.isAllowAllyClaim() || !target.isAllowAllyClaim() || (sourceAlly != null && !sourceAlly.isNearbyClaim()) || (targetAlly != null && !targetAlly.isNearbyClaim())) {
                LandManager.getInstance().checkClaimDistance(source, target);
            }

            if (sourceAlly != null) {
                Utils.runAsync(() -> DatabaseManager.getInstance().delete(source, sourceAlly));
            } else {
                DatabaseManager.getInstance().save(source);
            }
            if (targetAlly != null) {
                Utils.runAsync(() -> DatabaseManager.getInstance().delete(target, targetAlly));
            } else {
                DatabaseManager.getInstance().save(target);
            }
            getMessage("success").tell(target, "name", source.getName(), "state", source.getStateTag());
            getMessage("success").tell(source, "name", target.getName(), "state", target.getStateTag());
            for (MailProvider provider : SettlementsPlugin.getInstance().getMailProviders()) {
                provider.sendMail(source.getStateTag() + " " + source.getName(), target.getChief(), getText("success",
                        "name", source.getName(), "state", source.getStateTag()
                ));
                provider.sendMail(target.getStateTag() + " " + target.getName(), source.getChief(), getText("success",
                        "name", target.getName(), "state", target.getStateTag()
                ));
            }
        }
        return true;
    }
}

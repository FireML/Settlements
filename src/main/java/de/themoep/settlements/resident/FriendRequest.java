package de.themoep.settlements.resident;

import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.Utils.ItemUtils;

import java.util.Map;
import java.util.UUID;

public class FriendRequest extends Invite<Resident, Resident> {

    private final UUID senderUuid;
    private final String senderName;

    public FriendRequest(Resident target, Resident sender) {
        super(target, sender);
        senderUuid = sender.getUUID();
        senderName = sender.getName();
    }

    public FriendRequest(IdObject targetId, IdObject senderId, UUID senderUuid, String senderName, long time) {
        super("resident", targetId, "resident", senderId, time);
        this.senderUuid = senderUuid;
        this.senderName = senderName;
    }

    @Override
    public Resident getTarget() {
        return ResidentManager.getInstance().getResident(targetId);
    }

    @Override
    public Resident getSource() {
        return ResidentManager.getInstance().getResident(targetId);
    }

    @Override
    public String name() {
        return "friendrequest";
    }

    public static FriendRequest deserialize(Map<String, Object> map) {
        IdObject targetId = IdObject.get(map.get("targetId"));

        IdObject sourceId = IdObject.get(map.get("sourceId"));
        UUID senderUuid;
        if (map.get("senderUuid") instanceof UUID) {
            senderUuid = (UUID) map.get("senderUuid");
        } else if (map.get("senderUuid") instanceof String) {
            senderUuid = UUID.fromString((String) map.get("senderUuid"));
        } else {
            senderUuid = null;
        }
        String senderName = (String) map.get("senderName");

        long time = (long) map.get("time");

        return new FriendRequest(targetId, sourceId, senderUuid, senderName, time);
    }


    @Override
    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        return new String[] {
                "name", senderName
        };
    }

    @Override
    public ItemStack getIcon() {
        return ItemUtils.getHead(senderUuid, senderName);
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public String getSenderName() {
        return senderName;
    }
}

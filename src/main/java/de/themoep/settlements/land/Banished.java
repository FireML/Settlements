package de.themoep.settlements.land;

import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Next;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Banished implements DatabaseSerializable {
    private final String type;
    private final IdObject banishedId;
    private final long banishDate;
    private final UUID senderId;
    private final String reason;

    public Banished(String type, IdObject banishedId, long creationDate, UUID senderId, String reason) {
        this.type = type;
        this.banishedId = banishedId;
        this.banishDate = creationDate;
        this.senderId = senderId;
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    @Override
    public IdObject getId() {
        return IdObject.get(type + ":" + banishedId);
    }

    @Override
    public void setId(IdObject id) {
        throw new UnsupportedOperationException("Cannot set the ID of an Banish entry?");
    }

    @Override
    public String getDbDescription() {
        return banishedId.toString();
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Cannot save an individual Banish entry");
    }

    public IdObject getBanishedId() {
        return banishedId;
    }

    public long getBanishDate() {
        return banishDate;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String name() {
        return "banished";
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId());
        map.put("date", getBanishDate());
        map.put("sender", getSenderId());
        map.put("reason", getReason());
        return map;
    }

    public static Banished deserialize(Map<String, Object> map) {
        if (map.size() == 1) {
            return deserialize((Map<String, Object>) map.values().iterator().next());
        }
        String[] allyId = ((String) map.get("id")).split(":");
        IdObject id;
        if ("player".equals(allyId[0])) {
            id = IdObject.get(UUID.fromString(allyId[1]));
        } else {
            id = IdObject.get(Integer.parseInt(allyId[1]));
        }
        return new Banished(
                allyId[0],
                id,
                (Long) map.get("created"),
                UUID.fromString((String) map.get("sender")),
                (String) map.get("reason")
        );
    }

    public int compareTo(Sort sort, Banished other) {
        if (sort == Sort.NAME) {
            Targetable t = getTargetable();
            Targetable o = other.getTargetable();
            return t != null && o != null ? t.getName().compareToIgnoreCase(o.getName()) : 0;
        } else if (sort == Sort.DATE) {
            return Long.compare(other.getBanishDate(), getBanishDate());
        }
        return 0;
    }

    public Targetable getTargetable() {
        switch (type) {
            case "player":
                return ResidentManager.getInstance().getResident((UUID) getBanishedId().value());
            case "settlement":
                return SettlementManager.getInstance().getFromId(getBanishedId());
            case "empire":
                return EmpireManager.getInstance().getEmpireFromId(getBanishedId());
        }
        return null;
    }

    public Unbanished toUnbanish(Resident sender) {
        return new Unbanished(
                getType(),
                getBanishedId(),
                getBanishDate(),
                getSenderId(),
                getReason(),
                sender != null ? sender.getUUID() : Utils.ZERO_UUID,
                System.currentTimeMillis()
        );
    }

    public enum Sort implements NameTag, Next<Sort> {
        DATE,
        NAME;

        @Override
        public Sort[] getValues() {
            return values();
        }
    }
}

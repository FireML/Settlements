package de.themoep.settlements.land;

import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.*;
import org.bukkit.inventory.ItemStack;
import de.themoep.settlements.utils.ItemUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Banished implements DatabaseSerializable, Sortable<Banished> {
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
        map.put("id", getId().value());
        map.put("date", getBanishDate());
        map.put("sender", getSenderId());
        map.put("reason", getReason());
        return map;
    }

    public static Banished deserialize(Map<String, Object> map) {
        if (map.containsKey("banished")) {
            return deserialize((Map<String, Object>) map.get("banished"));
        }
        String[] banishedId = ((String) map.get("id")).split(":");
        IdObject id;
        if ("player".equals(banishedId[0])) {
            id = IdObject.get(UUID.fromString(banishedId[1]));
        } else {
            id = IdObject.get(Integer.parseInt(banishedId[1]));
        }
        return new Banished(
                banishedId[0],
                id,
                Utils.getLong(map.get("date")),
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
        return switch (type) {
            case "player" -> ResidentManager.getInstance().getResident((UUID) getBanishedId().value());
            case "settlement" -> SettlementManager.getInstance().getFromId(getBanishedId());
            case "empire" -> EmpireManager.getInstance().getEmpireFromId(getBanishedId());
            default -> null;
        };
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

    @Override
    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        return initial;
    }

    @Override
    public Sortable.Sort<Banished>[] getSorts() {
        return Sort.values();
    }

    @Override
    public ItemStack getIcon() {
        Targetable t = getTargetable();
        if (t instanceof Resident) {
            return ItemUtils.getHead((UUID) getBanishedId().value(), t.getName());
        } else if (t instanceof LandOwner owner) {
            return ItemUtils.getHead(owner.getChiefId(), owner.getChiefId().toString());
        }
        return null;
    }

    public enum Sort implements Sortable.Sort<Banished> {
        DATE((b1, b2) -> Long.compare(b2.getBanishDate(), b1.getBanishDate())),
        NAME((b1, b2) -> b1.getTargetable().getName().compareToIgnoreCase(b2.getTargetable().getName())),
        ;

        private final Comparator<Banished> comparator;

        Sort(Comparator<Banished> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Sortable.Sort<Banished>[] getValues() {
            return values();
        }

        @Override
        public Comparator<Banished> getComparator() {
            return comparator;
        }
    }
}

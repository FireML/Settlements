package de.themoep.settlements.land;

import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Sortable;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Unbanished implements DatabaseSerializable, Sortable<Unbanished> {
    private final String type;
    private final IdObject banishedId;
    private final long banishDate;
    private final UUID banisherId;
    private final String reason;
    private final UUID unbanisherId;
    private final long unbanishDate;

    public Unbanished(String type, IdObject banishedId, long creationDate, UUID senderId, String reason, UUID unbanisherId, long unbanishDate) {
        this.type = type;
        this.banishedId = banishedId;
        this.banishDate = creationDate;
        this.banisherId = senderId;
        this.reason = reason;
        this.unbanisherId = unbanisherId;
        this.unbanishDate = unbanishDate;
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
        throw new UnsupportedOperationException("Cannot set the ID of an Unbanish entry?");
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Cannot save an individual Unbanish entry");
    }

    public IdObject getBanishedId() {
        return banishedId;
    }

    public long getBanishDate() {
        return banishDate;
    }

    public UUID getBanisherId() {
        return banisherId;
    }

    public UUID getUnbanisherId() {
        return unbanisherId;
    }

    public long getUnbanishDate() {
        return unbanishDate;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String name() {
        return "unbanish";
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId());
        map.put("date", getBanishDate());
        map.put("sender", getBanisherId());
        map.put("reason", getReason());
        map.put("unbanisher", getUnbanisherId());
        map.put("unbanishdate", getUnbanisherId());
        return map;
    }

    public static Unbanished deserialize(Map<String, Object> map) {
        if (map.containsKey("unbanished")) {
            return deserialize((Map<String, Object>) map.get("unbanished"));
        }
        String[] allyId = ((String) map.get("id")).split(":");
        IdObject id;
        if ("player".equals(allyId[0])) {
            id = IdObject.get(UUID.fromString(allyId[1]));
        } else {
            id = IdObject.get(Integer.parseInt(allyId[1]));
        }
        return new Unbanished(
                allyId[0],
                id,
                Utils.getLong(map.get("date")),
                UUID.fromString((String) map.get("sender")),
                (String) map.get("reason"),
                UUID.fromString((String) map.get("unbanisher")),
                Utils.getLong(map.get("unbanishdate"))
        );
    }

    public int compareTo(Sort sort, Unbanished other) {
        if (sort == Sort.NAME) {
            Targetable t = getTargetable();
            Targetable o = other.getTargetable();
            return t != null && o != null ? t.getName().compareToIgnoreCase(o.getName()) : 0;
        } else if (sort == Sort.BANISH_DATE) {
            return Long.compare(other.getBanishDate(), getBanishDate());
        } else if (sort == Sort.UNBANISH_DATE) {
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

    @Override
    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        return initial;
    }

    @Override
    public Sortable.Sort<Unbanished>[] getSorts() {
        return Sort.values();
    }

    @Override
    public ItemStack getIcon() {
        Targetable t = getTargetable();
        if (t instanceof Resident) {
            return ItemUtils.getHead((UUID) getBanishedId().value(), t.getName());
        } else if (t instanceof LandOwner) {
            return ItemUtils.getHead(((LandOwner<?, ?>) t).getChiefId(), ((LandOwner<?, ?>) t).getChiefId().toString());
        }
        return null;
    }

    public enum Sort implements Sortable.Sort<Unbanished> {
        UNBANISH_DATE((b1, b2) -> Long.compare(b2.getUnbanishDate(), b1.getUnbanishDate())),
        BANISH_DATE((b1, b2) -> Long.compare(b2.getBanishDate(), b1.getBanishDate())),
        NAME((b1, b2) -> b1.getTargetable().getName().compareToIgnoreCase(b2.getTargetable().getName())),
        ;

        private final Comparator<Unbanished> comparator;

        Sort(Comparator<Unbanished> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Sortable.Sort<Unbanished>[] getValues() {
            return values();
        }

        @Override
        public Comparator<Unbanished> getComparator() {
            return comparator;
        }
    }
}

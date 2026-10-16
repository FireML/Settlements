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

public record Unbanished(String type, IdObject banishedId, long banishDate, UUID banisherId, String reason,
                         UUID unbanisherId, long unbanishDate) implements DatabaseSerializable, Sortable<Unbanished> {

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

    @Override
    public String name() {
        return "unbanish";
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId());
        map.put("date", banishDate());
        map.put("sender", banisherId());
        map.put("reason", reason());
        map.put("unbanisher", unbanisherId());
        map.put("unbanishdate", unbanishDate());
        return map;
    }

    public static Unbanished deserialize(Map<String, Object> map) {
        if (map.containsKey("unbanished")) {
            return deserialize((Map<String, Object>) map.get("unbanished"));
        }
        String[] unbanishedId = ((String) map.get("id")).split(":");
        IdObject id;
        if ("player".equals(unbanishedId[0])) {
            id = IdObject.get(UUID.fromString(unbanishedId[1]));
        } else {
            id = IdObject.get(Integer.parseInt(unbanishedId[1]));
        }
        return new Unbanished(
            unbanishedId[0],
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
            return Long.compare(other.banishDate(), banishDate());
        } else if (sort == Sort.UNBANISH_DATE) {
            return Long.compare(other.banishDate(), banishDate());
        }
        return 0;
    }

    public Targetable getTargetable() {
        return switch (type) {
            case "player" -> ResidentManager.getInstance().getResident((UUID) banishedId().value());
            case "settlement" -> SettlementManager.getInstance().getFromId(banishedId());
            case "empire" -> EmpireManager.getInstance().getEmpireFromId(banishedId());
            default -> null;
        };
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
            return ItemUtils.getHead((UUID) banishedId().value(), t.getName());
        } else if (t instanceof LandOwner<?, ?> owner) {
            return ItemUtils.getHead(owner.getChiefId(), owner.getChief().getName());
        }
        return null;
    }

    public enum Sort implements Sortable.Sort<Unbanished> {
        UNBANISH_DATE((b1, b2) -> Long.compare(b2.unbanishDate(), b1.unbanishDate())),
        BANISH_DATE((b1, b2) -> Long.compare(b2.banishDate(), b1.banishDate())),
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

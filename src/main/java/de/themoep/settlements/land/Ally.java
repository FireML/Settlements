package de.themoep.settlements.land;

import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Next;

import java.util.LinkedHashMap;
import java.util.Map;

public class Ally implements DatabaseSerializable {
    private final String type;
    private final IdObject id;
    private final long creationDate;
    private boolean nearbyClaim;

    public Ally(String type, IdObject id, long creationDate, boolean nearbyClaim) {
        this.type = type;
        this.id = id;
        this.creationDate = creationDate;
        this.nearbyClaim = nearbyClaim;
    }

    @Override
    public IdObject getId() {
        return IdObject.get(type + ":" + id);
    }
    @Override
    public void setId(IdObject id) {
        throw new UnsupportedOperationException("Cannot set the ID of an Ally");
    }

    @Override
    public void save() {
        DatabaseManager.getInstance().save(getLandOwner());
    }

    public String getType() {
        return type;
    }


    public IdObject getLandOwnerId() {
        return id;
    }

    public LandOwner getLandOwner() {
        switch (type) {
            case "settlement":
                return SettlementManager.getInstance().getFromId(id);
            case "empire":
                return EmpireManager.getInstance().getEmpireFromId(id);
        }
        return null;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public boolean isNearbyClaim() {
        return nearbyClaim;
    }

    public void setNearbyClaim(boolean nearbyClaim) {
        this.nearbyClaim = nearbyClaim;
    }

    @Override
    public String name() {
        return "ally";
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId());
        map.put("created", getCreationDate());
        map.put("nearbyClaim", isNearbyClaim());
        return map;
    }

    public static Ally deserialize(Map<String, Object> map) {
        if (map.containsKey("allies")) {
            return deserialize((Map<String, Object>) map.get("allies"));
        }
        String[] allyId = ((String) map.get("id")).split(":");
        return new Ally(
                allyId[0],
                IdObject.get(Integer.parseInt(allyId[1])),
                (Long) map.get("created"),
                (Boolean) map.get("nearbyClaim"));
    }

    public Ally removeAlly(LandOwner landOwner) {
        LandOwner ally = getLandOwner();
        if (ally != null) {
            return ally.removeAlly(landOwner);
        }
        return null;
    }

    public int compareTo(Sort sort, Ally other) {
        if (getType().equals(other.getType())) {
            LandOwner t = getLandOwner();
            LandOwner o = other.getLandOwner();

            if (sort == Sort.LEVEL) {
                if (t instanceof Empire empire1 && o instanceof Empire empire2) {
                    return empire1.compareTo(empire2);
                } else if (t instanceof Settlement settlement1 && o instanceof Settlement settlement2) {
                    return (settlement1).compareTo(settlement2);
                } else if (o == null) {
                    return 1;
                } else if (t == null) {
                    return -1;
                }
                return getType().equals("empire") ? 1 : -1;
            } else if (sort == Sort.NAME) {
                return t != null && o != null ? t.getName().compareToIgnoreCase(o.getName()) : 0;
            } else if (sort == Sort.ALLY_DATE) {
                return Long.compare(other.getCreationDate(), getCreationDate());
            }
        }
        return getType().equals("empire") ? 1 : -1;
    }

    public enum Sort implements NameTag, Next<Sort> {
        ALLY_DATE,
        NAME,
        LEVEL;

        @Override
        public Sort[] getValues() {
            return values();
        }
    }
}

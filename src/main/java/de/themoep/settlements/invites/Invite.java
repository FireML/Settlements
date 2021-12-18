package de.themoep.settlements.invites;

import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Next;
import de.themoep.settlements.utils.Sortable;
import de.themoep.settlements.utils.Targetable;

import java.util.Comparator;
import java.util.LinkedHashMap;

public abstract class Invite<T extends Targetable & DatabaseSerializable, S extends Targetable & DatabaseSerializable> implements DatabaseSerializable, Sortable<Invite<?, ?>> {
    private IdObject id = IdObject.get(null);

    private final String targetType;
    protected final IdObject targetId;

    private final String sourceType;
    protected final IdObject sourceId;

    private final long time;

    public Invite(T target, S source) {
        this(
                target.getKey(), target.getId(),
                source.getKey(), source.getId(),
                System.currentTimeMillis()
        );
    }

    public Invite(String targetType, IdObject targetId, String sourceType, IdObject sourceId, long time) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    public IdObject getId() {
        return id;
    }

    @Override
    public void setId(IdObject id) {
        this.id = id;
    }

    public abstract T getTarget();

    public boolean isTarget(T target) {
        return target != null && target.getKey().equals(targetType) && target.getId().equals(targetId);
    }

    public String getTargetType() {
        return targetType;
    }

    public IdObject getTargetId() {
        return targetId;
    }

    public abstract S getSource();

    public boolean isSource(S source) {
        return source != null && source.getKey().equals(sourceType) && source.getId().equals(sourceId);
    }

    public String getSourceType() {
        return sourceType;
    }

    public IdObject getSourceId() {
        return sourceId;
    }

    public Sort[] getSorts() {
        return Sort.values();
    }

    public enum Sort implements Sortable.Sort<Invite<?, ?>> {
        TIME((i1, i2) -> Long.compare(i2.getTime(), i1.getTime())),
        NAME((e1, e2) -> e1.getSource().getName().compareToIgnoreCase(e2.getSource().getName())),
        ;

        private final Comparator<Invite<?, ?>> comparator;

        Sort(Comparator<Invite<?, ?>> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Sortable.Sort<Invite<?, ?>>[] getValues() {
            return values();
        }

        @Override
        public Comparator<Invite<?, ?>> getComparator() {
            return comparator;
        }
    }

    @Override
    public void save() {
        DatabaseManager.getInstance().save(this);
    }

    @Override
    public String getDbName() {
        return "invite";
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("type", name());
        map.put("targetType", targetType);
        map.put("targetId", targetId);
        map.put("sourceType", sourceType);
        map.put("sourceId", sourceId);
        map.put("time", time);
        return map;
    }

    @Override
    public String toString() {
        return name() + "{" +
                "id=" + id +
                ", targetType='" + targetType + '\'' +
                ", targetId=" + targetId +
                ", sourceType='" + sourceType + '\'' +
                ", sourceId=" + sourceId +
                ", time=" + time +
                '}';
    }

}

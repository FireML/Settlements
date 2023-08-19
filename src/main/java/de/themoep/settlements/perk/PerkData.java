package de.themoep.settlements.perk;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

public class PerkData implements ConfigurationSerializable {
    private int times;
    private long lastUsed;
    private boolean active;

    public PerkData() {
        this.times = 1;
        this.lastUsed = System.currentTimeMillis();
        this.active = true;
    }

    private PerkData(int times, long lastUsed, boolean active) {
        this.lastUsed = lastUsed;
        this.times = times;
        this.active = active;
    }

    public static PerkData deserialize(Map<String, Object> map) {
        Validate.notNull(map.get("times"));
        Validate.notNull(map.get("lastUsed"));
        Validate.notNull(map.get("active"));
        return new PerkData(
                (Integer) map.get("times"),
                (Long) map.get("lastUsed"),
                map.get("active") instanceof Boolean
                        ? (Boolean) map.get("active")
                        : (Integer) map.get("active") == 1);
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public int getTimes() {
        return times;
    }

    public boolean isActive() {
        return active;
    }

    public void toggle() {
        this.active = !active;
    }

    public void addUse() {
        times++;
        lastUsed = System.currentTimeMillis();
    }

    @Override
    public Map<String, Object> serialize() {
        return ImmutableMap.of(
                "times", times,
                "lastUsed", lastUsed,
                "active", active
        );
    }
}

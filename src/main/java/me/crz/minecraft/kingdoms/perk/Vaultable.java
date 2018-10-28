package me.crz.minecraft.kingdoms.perk;

import java.util.Map;

public interface Vaultable {
    Map<String, Long> getVault();

    long getBank();

    default boolean has(Map<String, Long> requirements) {
        if (requirements == null) {
            return false;
        }
        for (Map.Entry<String, Long> pair : requirements.entrySet()) {
            if (!has(pair.getKey(), pair.getValue())) {
                return false;
            }
        }
        return true;
    }

    default boolean has(String key, long amount) {
        return getAmount(key) >= amount;
    }

    default long getAmount(String key) {
        if ("cash".equalsIgnoreCase(key)) {
            return getBank();
        }
        return getVault().getOrDefault(key, (long) 0);
    }

    default boolean remove(String key, long amount) {
        if (has(key, amount)) {
            setAmount(key, getAmount(key) - amount);
            return true;
        }
        return false;
    }

    default boolean remove(Map<String, Long> toRemove) {
        if (toRemove == null || toRemove.isEmpty()) {
            return false;
        }
        if (!has(toRemove)) {
            return false;
        }
        for (Map.Entry<String, Long> entry : toRemove.entrySet()) {
            remove(entry.getKey(), entry.getValue());
        }
        return true;
    }

    default void setAmount(String key, long amount) {
        if ("cash".equalsIgnoreCase(key)) {
            setBank(amount);
        }
        getVault().put(key, amount);
    }

    void setBank(long amount);
}

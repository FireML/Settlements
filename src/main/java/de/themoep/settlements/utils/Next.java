package de.themoep.settlements.utils;

public interface Next<T> {

    default T next() {
        if (ordinal() + 1 < getValues().length) {
            return getValues()[ordinal() + 1];
        }
        return getValues()[0];
    }

    T[] getValues();

    int ordinal();
}

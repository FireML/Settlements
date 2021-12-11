package de.themoep.settlements.rank;

/**
 * Classes (most often enums) that are able to be ranked.
 * When used with enums the methods correspond with the line placement of the enum constants.
 */
public interface Rankable {

    /**
     * Check if this rankable is at equal or higher than another one
     * @param rankable What to compare this rankable to
     * @return Whether or not the ordinal of this one is equal or below the other one
     */
    default <R extends Rankable> boolean atLeast(R rankable) {
        return rankable == null || rankable.ordinal() >= this.ordinal();
    }

    /**
     * Check if this rankable is higher than another one
     * @param rankable What to compare this rankable to
     * @return Whether or not the ordinal of this one is below the other one
     */
    default <R extends Rankable> boolean above(R rankable) {
        return rankable == null || rankable.ordinal() > this.ordinal();
    }

    /**
     * The opposite of {@link #atLeast(Rankable)}
     * @param rankable What to compare this rankable to
     * @return Whether or not the ordinal of this one is above the other one
     */
    default <R extends Rankable> boolean below(R rankable) {
        return rankable != null && rankable.ordinal() < this.ordinal();
    }

    /**
     * The priority of this rankable (lower number means higher rank)
     * @return The priority
     */
    int ordinal();

    default <R extends Rankable> int compareTo(R r) {
        if (r.below(this)) {
            return 1;
        } else if (r.above(this)) {
            return -1;
        }
        return 0;
    }
}

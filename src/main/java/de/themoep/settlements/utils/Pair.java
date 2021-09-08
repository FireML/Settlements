package de.themoep.settlements.utils;

public class Pair<L, R> {
    private L left;
    private R right;

    public Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public void setLeft(final L left) {
        this.left = left;
    }

    public void setRight(final R right) {
        this.right = right;
    }

    public boolean equals(final Pair<L, R> pair) {
        return pair.left.equals(left) && pair.right.equals(right);
    }
}

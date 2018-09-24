package me.crz.minecraft.kingdoms.utils;

public class Pair<L, R>
{
    private L left;
    private R right;
    
    public Pair() {
        super();
    }
    
    public Pair(final L left, final R right) {
        super();
        left = left;
        right = right;
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
        return left.equals(left) && right.equals(right);
    }
}

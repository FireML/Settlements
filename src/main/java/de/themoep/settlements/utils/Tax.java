package de.themoep.settlements.utils;

public class Tax {

    private final double amount;
    private final String source;

    public Tax(double amount, String source) {
        this.amount = amount;
        this.source = source;
    }

    public double getAmount() {
        return amount;
    }

    public String getSource() {
        return source;
    }
}

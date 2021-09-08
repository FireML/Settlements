package de.themoep.settlements.utils;

public class PathedConfigMessages implements ConfigMessages {
    private final String messagePath;

    public PathedConfigMessages(String messagePath) {
        this.messagePath = messagePath;
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public String name() {
        return messagePath;
    }
}

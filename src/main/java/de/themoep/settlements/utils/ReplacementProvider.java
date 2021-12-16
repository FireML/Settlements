package de.themoep.settlements.utils;

public interface ReplacementProvider {

    String[] getReplacements(ConfigMessages configMessages, String... initial);
}

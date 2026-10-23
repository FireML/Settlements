package de.themoep.settlements.utils;

public interface ReplacementProvider {

    String[] getReplacements(ConfigMessagesOld configMessages, String... initial);
}

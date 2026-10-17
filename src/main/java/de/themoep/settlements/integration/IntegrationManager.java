package de.themoep.settlements.integration;

import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class IntegrationManager {

    private static final IntegrationManager INSTANCE = new IntegrationManager();

    private final Map<String, SettlementsIntegration> integrationMap = new HashMap<>();

    private IntegrationManager() {}

    public static @NonNull IntegrationManager getInstance() {
        return INSTANCE;
    }

    public void load() {
        // Add Integrations
        addIntegration(new PlaceholderApiIntegration());

        // Load Integrations
        integrationMap.values().forEach(SettlementsIntegration::load);
    }

    private void addIntegration(SettlementsIntegration integration) {
        integrationMap.put(integration.name(), integration);
    }

    public void reload() {
        integrationMap.values().forEach(SettlementsIntegration::reload);
    }

}

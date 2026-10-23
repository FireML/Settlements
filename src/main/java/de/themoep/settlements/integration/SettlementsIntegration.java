package de.themoep.settlements.integration;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.utils.ConfigMessagesOld;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.NonNull;

public interface SettlementsIntegration extends ConfigMessagesOld {

    @NonNull String name();

    @Override
    default String type() {
        return "integration";
    }

    @NonNull String pluginName();

    void load();

    void reload();

    default void loadIntegration() {
        PluginManager pm = Bukkit.getPluginManager();
        if (!pm.isPluginEnabled(pluginName())) {
            return;
        }
        load();
        if (this instanceof Listener listener) {
            pm.registerEvents(listener, SettlementsPlugin.getInstance());
        }
    }

}

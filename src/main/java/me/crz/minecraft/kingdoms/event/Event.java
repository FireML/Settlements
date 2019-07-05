package me.crz.minecraft.kingdoms.event;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Event {

    private final String id;
    private final int time;
    private final List<String> rewards;

    private final String title;

    private final Set<String> worlds;

    private final Multimap<Action, String> actions = MultimapBuilder.enumKeys(Action.class).hashSetValues().build();

    public Event(String id, ConfigurationSection config) {
        this.id = id;
        time = config.getInt("time");
        title = config.getString("title");
        rewards = config.getStringList("rewards");
        if (rewards.size() > 9 * 6) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING,
                    "Detected an issue while loading event " + id + "! It had " + rewards.size() + " rewards defined but" +
                            " currently only " + (9 * 6) + " different ones are supported per event!" +
                            "You should group them together.");
        }
        worlds = config.getStringList("worlds").stream().map(String::toLowerCase).collect(Collectors.toSet());
        for (Action action : Action.values()) {
            if (config.isSet("actions." + action.getKey())) {
                config.getStringList("actions." + action.getKey()).stream().map(String::toLowerCase).forEach(s -> actions.put(action, s));
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTime() {
        return time;
    }

    public Set<String> getWorlds() {
        return worlds;
    }

    public Multimap<Action, String> getActions() {
        return actions;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public static enum Action implements NameTag {
        PLACE,
        BREAK,
        CRAFT,
        KILL,
        DISTANCE
    }
}

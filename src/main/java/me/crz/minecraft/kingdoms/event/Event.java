package me.crz.minecraft.kingdoms.event;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;
import java.util.stream.Collectors;

public class Event {

    private final String id;
    private final int time;

    private final String title;

    private final Set<String> worlds;

    private final Multimap<Action, String> actions = MultimapBuilder.enumKeys(Action.class).hashSetValues().build();

    public Event(String id, ConfigurationSection config) {
        this.id = id;
        time = config.getInt("time");
        title = config.getString("title");
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

    public static enum Action implements NameTag {
        PLACE,
        BREAK,
        CRAFT,
        KILL,
        DISTANCE
    }
}

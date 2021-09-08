package de.themoep.settlements.perk;

import de.themoep.settlements.utils.ConfigKey;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.land.LandOwner;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Perk implements NameTag {
    protected static final Map<String, Action> AVAILABLE_ACTIONS = new HashMap<>();
    static {
        registerAction("toggle", null, null);
    }

    private String name;
    private Material icon = Material.LINGERING_POTION;

    private int repeat = 0;
    private long cooldown = 0;
    private List<PerkEffect> effects = new ArrayList<>();
    private int bonusClaims = 0;
    private int bonusOutposts = 0;
    private List<String> commands = new ArrayList<>();
    private Map<String, Long> unlockCost = new HashMap<>();
    private Map<String, Long> repeatCost = new HashMap<>();
    protected Map<String, Action> actions = new LinkedHashMap<>();

    public Perk(ConfigurationSection config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("No config section?");
        }
        this.name = config.getName();
        this.icon = Material.valueOf(config.getString("icon", icon.name()).toUpperCase());
        if (!icon.isItem()) {
            throw new IllegalArgumentException("Icon type " + icon + " is not an obtainable item and cannot be used for the GUI!");
        }
        this.repeat = config.getInt("repeat", repeat);
        this.cooldown = config.getLong("cooldown", cooldown);
        for (Map<?, ?> map : config.getMapList("effects")) {
            try {
                effects.add(new PerkEffect((Map<String, Object>) map, getDefaultEffectLocation()));
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Effect is not valid! " + e.getMessage());
            }
        }
        this.bonusClaims = config.getInt("bonus.claims", bonusClaims);
        this.bonusOutposts = config.getInt("bonus.outposts", bonusOutposts);
        this.commands = config.getStringList("run-command");
        if (config.isConfigurationSection("cost.unlock")) {
            for (String type : config.getConfigurationSection("cost.unlock").getKeys(false)) {
                unlockCost.put(type, config.getLong("cost.unlock." + type));
            }
        }
        if (config.isConfigurationSection("cost.repeat")) {
            for (String type : config.getConfigurationSection("cost.repeat").getKeys(false)) {
                repeatCost.put(type, config.getLong("cost.repeat." + type));
            }
        }

        if (config.isConfigurationSection("actions")) {
            for (String action : config.getConfigurationSection("actions").getKeys(false)) {
                if (config.isConfigurationSection("actions." + action) || config.getBoolean("actions." + action, false)) {
                    if (AVAILABLE_ACTIONS.containsKey(action.toLowerCase())) {
                        actions.put(action.toLowerCase(), AVAILABLE_ACTIONS.get(action.toLowerCase()));
                    } else {
                        throw new IllegalArgumentException("Action " + action + " is not supported!");
                    }
                }
            }
        }
    }

    public static void registerAction(String name, ActionExecutor actionExecutor, StateQuery stateQuery) {
        registerAction(new Action(name, actionExecutor, stateQuery));
    }

    public static void registerAction(Action action) {
        AVAILABLE_ACTIONS.put(action.name().toLowerCase(), action);
    }

    protected PerkEffect.EffectLocation getDefaultEffectLocation() {
        return PerkEffect.EffectLocation.OWN_SETTLEMENT;
    }

    @Override
    public String name() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public int getRepeat() {
        return repeat;
    }

    public long getCooldown() {
        return cooldown;
    }

    public List<PerkEffect> getEffects() {
        return effects;
    }

    public int getBonusClaims() {
        return bonusClaims;
    }

    public int getBonusOutposts() {
        return bonusOutposts;
    }

    public List<String> getCommands() {
        return commands;
    }

    public Map<String, Long> getUnlockCost() {
        return unlockCost;
    }

    public Map<String, Long> getRepeatCost() {
        return repeatCost;
    }

    public boolean hasEffects() {
        if (!effects.isEmpty()) {
            return true;
        }

        for (Action action : actions.values()) {
            if (action.isBoolean()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasActions() {
        return !actions.isEmpty();
    }

    public void toggleActions(Player player, LandOwner landOwner, boolean enable) {
        for (Action action : actions.values()) {
            action.onToggle(player, landOwner, enable);
        }
    }

    public List<String> getActionStates(LandOwner landOwner) {
        List<String> states = new ArrayList<>();
        for (Action action : actions.values()) {
            String state = action.getState(landOwner);
            if (state != null) {
                states.add(action.getKey() + "." + state);
            }
        }
        return states;
    }

    public interface ActionExecutor {

        void onToggle(Player player, LandOwner<?, ?> landOwner, boolean enable);

    }

    public interface StateQuery {

        String getState(LandOwner<?, ?> landOwner);

    }

    public static class Action implements ActionExecutor, StateQuery, ConfigKey {
        private final String name;
        private ActionExecutor actionExecutor;
        private StateQuery stateQuery;

        public Action(String name, ActionExecutor actionExecutor, StateQuery stateQuery) {
            this.name = name;
            this.actionExecutor = actionExecutor;
            this.stateQuery = stateQuery;
        }

        @Override
        public void onToggle(Player player, LandOwner<?, ?> landOwner, boolean enable) {
            if (actionExecutor != null) {
                actionExecutor.onToggle(player, landOwner, enable);
            }
        }

        @Override
        public String getState(LandOwner<?, ?> landOwner) {
            if (stateQuery != null) {
                return stateQuery.getState(landOwner);
            }
            return null;
        }

        public String name() {
            return name;
        }

        public boolean isBoolean() {
            return stateQuery == null;
        }
    }
}

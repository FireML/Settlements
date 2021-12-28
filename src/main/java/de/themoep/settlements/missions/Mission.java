package de.themoep.settlements.missions;

import de.themoep.settlements.empire.EmpireMission;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementMission;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.MaterialUtils;
import de.themoep.settlements.utils.NameTag;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Mission implements NameTag {
    private final String id;
    private final Material icon;
    private final long missionPoints;
    private final long contributionPoints;
    private final Type type;
    private final Map<String, Long> requirements = new LinkedHashMap<>();

    public Mission(ConfigurationSection config) throws IllegalArgumentException {
        id = config.getName();
        icon = MaterialUtils.getMaterial(config.getString("icon"));
        if (icon == null && config.isString("icon")) {
            throw new IllegalArgumentException("Icon type " + config.getString("icon") + " is invalid!");
        }
        if (icon != null && !icon.isItem()) {
            throw new IllegalArgumentException("Icon type " + icon + " is not an obtainable item and cannot be used for the GUI!");
        }
        missionPoints = config.getLong("mission-points", 0);
        contributionPoints = config.getLong("contribution-points", 0);
        type = Type.valueOf(config.getString("type", "missing").toUpperCase(Locale.ROOT));

        ConfigurationSection requirementsConfig = config.getConfigurationSection("requirements");
        if (requirementsConfig != null) {
            for (String requirement : requirementsConfig.getKeys(false)) {
                requirements.put(requirement.toUpperCase(Locale.ROOT), requirementsConfig.getLong(requirement));
            }
        } else {
            throw new IllegalArgumentException("Missing requirements in config");
        }
    }

    public static Mission fromDbId(String dbId) {
        String[] split = dbId.split(":", 2);
        if (split.length != 2) {
            return null;
        }
        if ("empiremission".equalsIgnoreCase(split[0])) {
            return EmpireManager.getInstance().getMission(split[1]);
        }
        if ("settlementmission".equalsIgnoreCase(split[0])) {
            return SettlementManager.getInstance().getMission(split[1]);
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getDbId() {
        return getDbName() + ":" + getId();
    }

    protected abstract String getDbName();

    @Override
    public String name() {
        return getId();
    }

    public long getMissionPoints() {
        return missionPoints;
    }

    public long getContributionPoints() {
        return contributionPoints;
    }

    public Type getType() {
        return type;
    }

    public Map<String, Long> getRequirements() {
        return requirements;
    }

    public boolean isRequirement(String type) {
        return getRequirement(type) > 0;
    }

    public long getRequirement(String type) {
        return getRequirements().getOrDefault(type.toUpperCase(Locale.ROOT), 0L);
    }

    public ItemStack getIcon() {
        if (icon != null) {
            return ItemUtils.toIcon(icon);
        }
        for (String r : getRequirements().keySet()) {
            try {
                Material type = Material.valueOf(r);
                if (type.isItem()) {
                    return ItemUtils.toIcon(type);
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return ItemUtils.toIcon(Material.DIRT);
    }

    public abstract void finish(Resident resident);

    public abstract boolean isValid(Resident resident, MissionProvider provider);

    public enum Type implements NameTag {
        TURN_IN,
        PLACE,
        BREAK,
        HARVEST,
        KILL
    }

    public static String[] getReplacements(ConfigMessages messages, MissionProvider<?> provider, MissionTaker<?> taker) {
        return new String[] {
                "amount", String.valueOf(taker.getMissionData().size()),
                "missions", taker.getMissionData().stream()
                .map(m -> messages.getText("mission-entry",
                        "title", m.getMission().getTag(),
                        "requirements", m.getMission().getRequirements().entrySet().stream().map(e ->
                                messages.getText("requirement." + (m.getProgress(e.getKey()) < e.getValue() ? "todo" : "done"),
                                        "progress", String.valueOf(m.getProgress(e.getKey())),
                                        "required", String.valueOf(e.getValue()),
                                        "type", WordUtils.capitalizeFully(e.getKey().replace('_', ' '))
                                )).collect(Collectors.joining("\n")),
                        "action", (m.isFinished() ? messages.getText("finished") : "")
                                + (m.getMission() instanceof EmpireMission && ((EmpireMission) m.getMission()).onlyInEmpireWorld()
                                        ? messages.getText("only-in-empire", "empire", provider.getName())
                                        : m.getMission() instanceof EmpireMission && ((EmpireMission) m.getMission()).onlyOnEmpireClaim()
                                        ? messages.getText("only-on-claim", "state", provider.getStateTag(), "name", provider.getName())
                                        : m.getMission() instanceof SettlementMission && ((SettlementMission) m.getMission()).onlyOnSettlementClaim()
                                        ? messages.getText("only-on-claim", "state", provider.getStateTag(), "name", provider.getName())
                                        : "")
                                + m.getMission().getType().getTag()
                        ,
                        "missionpoints", String.valueOf(m.getMission().getMissionPoints()),
                        "contributionpoints", String.valueOf(m.getMission().getMissionPoints())
                )).collect(Collectors.joining("\n")),
                "state", provider.getStateTag(),
                "name", provider.getName()
        };
    }
}

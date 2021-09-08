package de.themoep.settlements.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.PlaceholderHook;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.village.VillageState;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PlaceholderApiIntegration implements ConfigMessages {
    private PlaceholderAPIPlugin apiPlugin;

    public PlaceholderApiIntegration() {
        apiPlugin = PlaceholderAPIPlugin.getInstance();
        PlaceholderAPI.registerPlaceholderHook("kingdoms", new PlaceholderHook() {
            @Override
            public String onPlaceholderRequest(Player p, String params) {
                String[] args = params.split("_");
                if (args.length > 1 && "top".equalsIgnoreCase(args[0])) {
                    if ("settlements".equalsIgnoreCase(args[1])) {
                        return getReplacedVillageString(VillageManager.getInstance().getSettlements().stream(), args);
                    } else if ("villages".equalsIgnoreCase(args[1])) {
                        return getReplacedVillageString(VillageManager.getInstance().getSettlements().stream().filter(v -> v.getState() == VillageState.VILLAGE), args);
                    } else if ("kingdoms".equalsIgnoreCase(args[1])) {
                        return getReplacedVillageString(VillageManager.getInstance().getSettlements().stream().filter(v -> v.getState() == VillageState.KINGDOM), args);
                    } else if ("empires".equalsIgnoreCase(args[1])) {
                        return getReplacedEmpireString(EmpireManager.getInstance().getEmpires().stream(), args);
                    }
                }
                return null;
            }
        });
    }

    private String getReplacedVillageString(Stream<Village> villageStream, String[] args) {
        if (args.length > 2) {
            // get specific
            try {
                int place = Integer.parseInt(args[2]);
                Optional<Village> village = villageStream.sorted(Village::compareTo).skip(place - 1).findFirst();
                if (village.isPresent()) {
                    if (args.length > 3) {
                        return Utils.toMap(village.get().getReplacements(PlaceholderApiIntegration.this)).get(args[3].toLowerCase());
                    } else {
                        return ("settlements".equalsIgnoreCase(args[1]) ? (village.get().getStateTag() + " ") : "") + village.get().getName();
                    }
                } else {
                    return getText("none");
                }
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            // get top 10
            List<String> lines = new ArrayList<>();
            villageStream.sorted(Village::compareTo).limit(10)
                    .forEachOrdered(v -> lines.add(getMessage("top-ten-" + args[1].toLowerCase() + ".entry").getReplacedText(
                            "place", String.valueOf(lines.size() + 1),
                            "name", v.getName(),
                            "level", String.valueOf(v.getLevel()),
                            "description", v.getDescription(),
                            "state", v.getStateTag(),
                            "access", v.getAccess() != LandAccess.PRIVATE ? v.getAccess().getTag() : "",
                            "residents", String.valueOf(v.getResidentIds().size())
                    )));
            return String.join("\n", lines);
        }
    }

    private String getReplacedEmpireString(Stream<Empire> empireStream, String[] args) {
        if (args.length > 2) {
            // get specific
            try {
                int place = Integer.parseInt(args[2]);
                Optional<Empire> empire = empireStream.sorted(Empire::compareTo).skip(place - 1).findFirst();
                if (empire.isPresent()) {
                    if (args.length > 3) {
                        return Utils.toMap(empire.get().getReplacements(PlaceholderApiIntegration.this)).get(args[3].toLowerCase());
                    } else {
                        return empire.get().getName();
                    }
                } else {
                    return getText("none");
                }
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            // get top 10
            List<String> lines = new ArrayList<>();
            empireStream.sorted(Empire::compareTo).limit(10)
                    .forEachOrdered(e -> lines.add(getMessage("top-ten-empires.entry").getReplacedText(
                            "place", String.valueOf(lines.size() + 1),
                            "name", e.getName(),
                            "description", e.getDescription(),
                            "state", e.getStateTag(),
                            "access", e.getAccess() != LandAccess.PRIVATE ? e.getAccess().getTag() : "",
                            "members", String.valueOf(e.getMemberIds().size())
                    )));
            return String.join("\n", lines);
        }
    }

    public static String setPlaceholders(Player player, String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    public static List<String> setPlaceholders(Player player, List<String> strings) {
        return PlaceholderAPI.setPlaceholders(player, strings);
    }


    public PlaceholderAPIPlugin getPlaceholderAPI() {
        return apiPlugin;
    }

    @Override
    public String type() {
        return "integration";
    }

    @Override
    public String name() {
        return "placeholder-api";
    }
}

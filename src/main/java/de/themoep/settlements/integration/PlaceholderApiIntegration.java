package de.themoep.settlements.integration;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.village.VillageState;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class PlaceholderApiIntegration implements ConfigMessages {
    private PlaceholderAPIPlugin apiPlugin;

    public PlaceholderApiIntegration() {
        apiPlugin = PlaceholderAPIPlugin.getInstance();

        new SettlementsExpansion("settlements").register();
        new SettlementsExpansion("kingdoms").register();
    }

    public class SettlementsExpansion extends PlaceholderExpansion implements Relational {
        private final String identifier;

        public SettlementsExpansion(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String getAuthor() {
            return String.join(", ", SettlementsPlugin.getInstance().getDescription().getAuthors());
        }

        @Override
        public String getVersion() {
            return SettlementsPlugin.getInstance().getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player viewer, Player target, String params) {
            switch (params.toLowerCase(Locale.ROOT)) {
                case "relationcolor":
                    return getRelationColor(viewer, target);
                case "relation":
                    return getRelation(viewer, target);
            }
            return null;
        }

        @Override
        public String onPlaceholderRequest(Player p, String params) {
            String[] args = params.split("_");
            if (args.length > 0) {
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "relationcolor": {
                        if (args.length > 1) {
                            Player target = p.getServer().getPlayer(args[1]);
                            if (target != null) {
                                return getRelationColor(p, target);
                            }
                        }
                        break;
                    }
                    case "relation": {
                        if (args.length > 1) {
                            Player target = p.getServer().getPlayer(args[1]);
                            if (target != null) {
                                return getRelation(p, target);
                            }
                        }
                        break;
                    }
                    case "top": {
                        if (args.length > 1) {
                            switch (args[1].toLowerCase(Locale.ROOT)) {
                                case "settlements":
                                    return getReplacedVillageString(VillageManager.getInstance().getSettlements().stream(), args);
                                case "villages":
                                    return getReplacedVillageString(VillageManager.getInstance().getSettlements().stream().filter(v -> v.getState() == VillageState.VILLAGE), args);
                                case "kingdoms":
                                    return getReplacedVillageString(VillageManager.getInstance().getSettlements().stream().filter(v -> v.getState() == VillageState.KINGDOM), args);
                                case "empires":
                                    return getReplacedEmpireString(EmpireManager.getInstance().getEmpires().stream(), args);
                            }
                        }
                        break;
                    }
                    case "player": {
                        if (p != null && args.length > 1) {
                            Resident resident = ResidentManager.getInstance().getResident(p);
                            switch (args[1].toLowerCase(Locale.ROOT)) {
                                case "info": {
                                    return resident.getReplacement(args[2]);
                                }
                                case "settlement": {
                                    if (args.length > 2) {
                                        if (resident.hasVillage()) {
                                            return Utils.toMap(resident.getVillage().getReplacements(PlaceholderApiIntegration.this))
                                                    .get(args[2].toLowerCase(Locale.ROOT));
                                        } else {
                                            return "";
                                        }
                                    }
                                    break;
                                }
                                case "empire": {
                                    if (args.length > 2) {
                                        if (resident.hasEmpire()) {
                                            return resident.getVillage().getEmpire().getReplacement(PlaceholderApiIntegration.this, args[2]);
                                        } else {
                                            return "";
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }
                    case "landowner": {
                        if (p != null && args.length > 1) {
                            Village v = VillageManager.getInstance().getAt(p.getLocation());
                            if (v != null) {
                                return Utils.toMap(v.getReplacements(PlaceholderApiIntegration.this))
                                        .get(args[1].toLowerCase(Locale.ROOT));
                            }
                            Empire e = EmpireManager.getInstance().getEmpireFromWorld(p.getWorld());
                            if (e != null) {
                                return e.getReplacement(PlaceholderApiIntegration.this, args[1]);
                            }
                            return "";
                        }
                        break;
                    }
                    case "land": {
                        if (p != null && args.length > 1) {
                            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                            if (land != null && land.isPlot()) {
                                return land.getReplacement(args[1]);
                            } else {
                                return "";
                            }
                        }
                        break;
                    }
                    case "plot": {
                        if (p != null && args.length > 1) {
                            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                            if (land != null) {
                                return land.getReplacement(args[1]);
                            } else {
                                return "";
                            }
                        }
                        break;
                    }
                }
            }
            return null;
        }
    }

    private String getRelationColor(Player viewer, Player target) {
        return getMessage("relation-color." + getRelation(viewer, target)).getText();
    }

    private String getRelation(Player viewer, Player target) {
        Resident viewerResident = ResidentManager.getInstance().getResident(viewer);
        Resident targetResident = ResidentManager.getInstance().getResident(target);

        if (!viewerResident.hasVillage() || !targetResident.hasVillage()) {
            return "other";
        }

        if (viewerResident.getVillage().isMember(targetResident)) {
            return "friendly";
        }

        if (viewerResident.getVillage().isAlly(targetResident)) {
            return "ally";
        }

        if (viewerResident.hasEmpire() && viewerResident.getVillage().getEmpire().isMember(targetResident)) {
            return "empire";
        }

        return null;
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

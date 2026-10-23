package de.themoep.settlements.integration;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.utils.ConfigMessagesOld;
import de.themoep.settlements.utils.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class PlaceholderApiIntegration implements ConfigMessagesOld, SettlementsIntegration {

    private final PlaceholderAPIPlugin apiPlugin = PlaceholderAPIPlugin.getInstance();

    public class SettlementsExpansion extends PlaceholderExpansion implements Relational {
        private final String identifier;

        public SettlementsExpansion(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public @NonNull String getIdentifier() {
            return identifier;
        }

        @Override
        public @NonNull String getAuthor() {
            return String.join(", ", SettlementsPlugin.getInstance().getPluginMeta().getAuthors());
        }

        @Override
        public boolean persist() { return true; }

        @Override
        public @NonNull String getVersion() {
            return SettlementsPlugin.getInstance().getPluginMeta().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player viewer, Player target, String params) {
            return switch (params.toLowerCase(Locale.ROOT)) {
                case "relationcolor" -> getRelationColor(viewer, target);
                case "relation" -> getRelation(viewer, target);
                default -> null;
            };
        }

        @Override
        public String onPlaceholderRequest(Player p, String params) {
            String[] args = params.split("_");
            if (args.length > 0) {
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "relationcolor" -> {
                        if (args.length > 1) {
                            Player target = p.getServer().getPlayer(args[1]);
                            if (target != null) {
                                return getRelationColor(p, target);
                            }
                        }
                    }
                    case "relation" -> {
                        if (args.length > 1) {
                            Player target = p.getServer().getPlayer(args[1]);
                            if (target != null) {
                                return getRelation(p, target);
                            }
                        }
                    }
                    case "top" -> {
                        if (args.length > 1) {
                            switch (args[1].toLowerCase(Locale.ROOT)) {
                                case "settlements" -> {
                                    return getReplacedSettlementString(SettlementManager.getInstance().getSettlements().stream(), args);
                                }
                                case "villages" -> {
                                    return getReplacedSettlementString(SettlementManager.getInstance().getSettlements().stream().filter(v -> v.getState() == SettlementState.VILLAGE), args);
                                }
                                case "kingdoms" -> {
                                    return getReplacedSettlementString(SettlementManager.getInstance().getSettlements().stream().filter(v -> v.getState() == SettlementState.KINGDOM), args);
                                }
                            }
                        }
                    }
                    case "player" -> {
                        if (p != null && args.length > 1) {
                            Resident resident = ResidentManager.getInstance().getResident(p);
                            switch (args[1].toLowerCase(Locale.ROOT)) {
                                case "info" -> {
                                    return resident.getReplacement(args[2]);
                                }
                                case "settlement" -> {
                                    if (args.length > 2) {
                                        if (resident.hasSettlement()) {
                                            return Utils.toMap(resident.getSettlement().getReplacements(PlaceholderApiIntegration.this))
                                                .get(args[2].toLowerCase(Locale.ROOT));
                                        } else {
                                            return "";
                                        }
                                    }
                                }
                            }
                        }
                    }
                    case "landowner" -> {
                        if (p != null && args.length > 1) {
                            Settlement v = SettlementManager.getInstance().getAt(p.getLocation());
                            if (v != null) {
                                return Utils.toMap(v.getReplacements(PlaceholderApiIntegration.this))
                                    .get(args[1].toLowerCase(Locale.ROOT));
                            }
                            return "";
                        }
                    }
                    case "land" -> {
                        if (p != null && args.length > 1) {
                            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                            if (land != null && land.isPlot()) {
                                return land.getReplacement(args[1]);
                            } else {
                                return "";
                            }
                        }
                    }
                    case "plot" -> {
                        if (p != null && args.length > 1) {
                            OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                            if (land != null) {
                                return land.getReplacement(args[1]);
                            } else {
                                return "";
                            }
                        }
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

        if (!viewerResident.hasSettlement() || !targetResident.hasSettlement()) {
            return "other";
        }

        if (viewerResident.getSettlement().isMember(targetResident)) {
            return "friendly";
        }

        if (viewerResident.getSettlement().isAlly(targetResident)) {
            return "ally";
        }

        return "other";
    }

    private String getReplacedSettlementString(Stream<Settlement> settlementStream, String[] args) {
        if (args.length > 2) {
            // get specific
            try {
                int place = Integer.parseInt(args[2]);
                Optional<Settlement> settlement = settlementStream.sorted(Settlement::compareTo).skip(place - 1).findFirst();
                if (settlement.isPresent()) {
                    if (args.length > 3) {
                        return Utils.toMap(settlement.get().getReplacements(PlaceholderApiIntegration.this)).get(args[3].toLowerCase());
                    } else {
                        return ("settlements".equalsIgnoreCase(args[1]) ? (settlement.get().getStateTag() + " ") : "") + settlement.get().getName();
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
            settlementStream.sorted(Settlement::compareTo).limit(10)
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
    public @NonNull String name() {
        return "placeholder-api";
    }

    @Override
    public @NonNull String pluginName() {
        return "PlaceholderAPI";
    }

    @Override
    public void load() {
        new SettlementsExpansion("settlements").register();
    }

    @Override
    public void reload() { /* Does nothing. */ }

}
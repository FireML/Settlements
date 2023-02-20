package de.themoep.settlements.land;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigKey;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.world.NamedLocation;
import de.themoep.settlements.world.SimpleChunkLocation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ChatMap {
    private static final int HEIGHT = 13;
    private static final int WIDTH = 29;
    private final ConfigMessages messages;
    private final List<RowEnd> rowEnds;
    private final LandOwner<?, ?> landOwner;

    public ChatMap(Player player, LandOwner<?, ?> landOwner, ConfigMessages messages) {
        this.messages = messages;
        this.landOwner = landOwner;
        MapGenerator generator = new ChatMap.MapGenerator(player);
        BaseComponent[][][] map = generator.getComponents();
        rowEnds = generator.getRowEnds();
        messages.getMessage("head").tell(player);
        for (int i = 0; i < map.length; ++i) {
            ComponentBuilder cb = new ComponentBuilder("");
            BaseComponent[] rowBase = getRowBase(i, player);
            if (rowBase.length > 0) {
                cb.append(rowBase);
            }
            for (int j = this.getStart(i); j < 29; ++j) {
                if (map[i][j].length > 0) {
                    cb.append(map[i][j], ComponentBuilder.FormatRetention.NONE);
                }
            }
            BaseComponent[] rowEnd = getRowEnd(i);
            if (rowEnd.length > 0) {
                cb.append(rowEnd, ComponentBuilder.FormatRetention.NONE);
            }
            player.spigot().sendMessage(cb.create());
        }
    }


    private String rpGetPlayerDirection(Player playerSelf) {
        float y = playerSelf.getLocation().getYaw();
        y += 22.5f;
        if (y < 0.0f) {
            y += 360.0f;
        }
        y %= 360.0f;
        int i = (int) (y / 45);
        switch (i) {
            case 0:
                return "S";
            case 1:
                return "2/";
            case 2:
                return "W";
            case 3:
                return "0\\\\";
            case 4:
                return "N";
            case 5:
                return "0/";
            case 6:
                return "E";
            case 7:
                return "2\\\\";
            default:
                return "+";
        }
    }

    public List<String> getRowSymbols(Player p) {
        String tmp = this.rpGetPlayerDirection(p);
        List<String> temp = Arrays.asList("&e\\\\&eN&e/&r", "&eW&e+&eE&r", "&e/&eS&e\\\\&r");
        for (int i = 0; i < 3; ++i) {
            if (tmp.contains(String.valueOf(i))) {
                temp.set(i, temp.get(i).replace(tmp.replace(String.valueOf(i), ""), "&c" + tmp.replace(String.valueOf(i), "")));
            } else if (!temp.contains("0") && !temp.contains("2")) {
                temp.set(i, temp.get(i).replace(tmp, "&c" + tmp));
            }
        }
        return temp;
    }

    private BaseComponent[] getRowBase(int row, Player p) {
        List<String> tmp = this.getRowSymbols(p);
        if (row >= 0 && row < tmp.size()) {
            return MessageUtils.toColoredComponents(tmp.get(row));
        }
        return new BaseComponent[0];
    }

    private BaseComponent[] getRowEnd(int row) {
        if (rowEnds.size() > row) {
            return messages.getMessage("explanations." + rowEnds.get(row).getKey()).getComponents();
        }
        return new BaseComponent[0];
    }

    private int getStart(int row) {
        if (row == 0) {
            return 3;
        }
        if (row == 1) {
            return 3;
        }
        if (row == 2) {
            return 3;
        }
        return 0;
    }

    public class MapGenerator {
        private final Player player;
        private final Resident resident;
        private final OwnedLand playerLand;
        private final boolean claimable;

        private Set<SimpleChunkLocation> ownOutposts = new HashSet<>();
        private boolean canClaimCoords = false;
        private boolean canUnclaimCoords = false;
        private boolean canLinkPlot;

        private final Set<RowEnd> legend = EnumSet.of(RowEnd.CURRENT);

        public MapGenerator(Player p) {
            this.playerLand = LandManager.getInstance().getLand(p.getLocation());
            this.claimable = landOwner != null && landOwner.canClaim(p.getWorld());
            this.player = p;
            this.resident = ResidentManager.getInstance().getResident(p);
            this.canLinkPlot = playerLand != null && (playerLand.isPlotOwner(resident) || playerLand.canAdmin(resident));
            if (landOwner != null) {
                this.canClaimCoords = landOwner.hasPermission(resident, "claim.coords");
                this.canUnclaimCoords = landOwner.hasPermission(resident, "unclaim.coords");
                if (landOwner instanceof Settlement) {
                    for (NamedLocation o : ((Settlement) landOwner).getOutpostHomes().values()) {
                        OwnedLand outpostLand = LandManager.getInstance().getLand(o.toChunkLocation());
                        if (outpostLand != null && !outpostLand.containsHome()) {
                            ownOutposts.addAll(LandManager.getInstance().getAllConnectedLand(outpostLand, search -> {
                                if (search.isOutpost()) {
                                    return LandManager.FilterResponse.SKIP_BRANCH;
                                } else if (search.containsHome()) {
                                    return LandManager.FilterResponse.SKIP_BRANCH;
                                }
                                return LandManager.FilterResponse.ACCEPT;
                            }));
                        }
                    }
                }
            }
        }

        private BaseComponent[][][] getComponents() {
            BaseComponent[][][] map = new BaseComponent[HEIGHT][WIDTH][0];
            int x = (player.getLocation().getBlockX() >> 4) - WIDTH / 2;
            int z = (player.getLocation().getBlockZ() >> 4) - HEIGHT / 2;
            for (int countX = 0; countX < WIDTH; countX++) {
                for (int countZ = 0; countZ < HEIGHT; countZ++) {
                    map[countZ][countX] = getSymbol(x + countX, z + countZ, player.getLocation().getWorld(), resident, countX == WIDTH / 2 && countZ == HEIGHT / 2);
                }
            }
            return map;
        }

        private BaseComponent[] getSymbol(int x, int z, World world, Resident resident, boolean current) {
            OwnedLand land = LandManager.getInstance().getLand(x, z, world);
            if (land == null) {
                ComponentBuilder cb = new ComponentBuilder((current ? Symbol.CURRENT : Symbol.NORMAL).getText());
                if (landOwner != null && claimable && canClaimCoords) {
                    cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + landOwner.getKey() + " claim " + x + " " + z + " map"));
                    cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(messages.getMessage("hover.click-to-claim")
                            .getReplacedComponents(
                                    "location", x + "-" + z,
                                    "centerx", String.valueOf(x * 16 + 8),
                                    "centerz", String.valueOf(z * 16 + 8),
                                    "x", String.valueOf(x),
                                    "z", String.valueOf(z)
                            ))));
                }
                legend.add(RowEnd.UNCLAIMED);
                return cb.color(Relation.UNCLAIMED.getColor()).create();
            }
            Symbol symbol = Symbol.NORMAL;
            Relation relation;
            if (land.isOutpost()) {
                legend.add(RowEnd.OUTPOST);
                symbol = Symbol.OUTPOST;
            }

            if (land.isOwnedBy(landOwner)) {
                if (land.isForSale()) {
                    legend.add(RowEnd.FOR_SALE);
                    symbol = Symbol.FOR_SALE;
                }
                if (land.containsHome()) {
                    legend.add(RowEnd.HOME);
                    symbol = Symbol.HOME;
                }
            }
            if (land.getLandOwner().isMember(resident)) {
                if (land.getLandOwner() instanceof Empire) {
                    legend.add(RowEnd.EMPIRE_LAND);
                    relation = Relation.OWN_EMPIRE;
                } else {
                    legend.add(RowEnd.OWN_LAND);
                    relation = Relation.OWN;
                }

            } else if (landOwner != null && landOwner.isAlly(land.getLandOwner())) {
                legend.add(RowEnd.ALLY);
                relation = Relation.ALLY;
            } else if (landOwner != null && landOwner.isTrusted(land.getLandOwner())) {
                legend.add(RowEnd.EMPIRE);
                relation = Relation.EMPIRE;
            } else {
                legend.add(RowEnd.ENEMY);
                relation = Relation.ENEMY;
            }

            if (land.isPlot()) {
                if (land.isPlotOwner(resident)) {
                    if (land.isLinked(playerLand)) {
                        relation = Relation.OWN_PLOT_LINKED_CURRENT;
                        legend.add(RowEnd.LINKED_PLOT);
                    } else if (land.hasLink()) {
                        legend.add(RowEnd.LINKED_PLOT);
                        relation = Relation.OWN_PLOT_LINKED;
                    } else {
                        legend.add(RowEnd.PLAYER_PLOT);
                        relation = Relation.OWN_PLOT;
                    }
                }
                symbol = Symbol.PLOT;
            }

            if (land.isEmbassy()) {
                legend.add(RowEnd.EMBASSY);
                symbol = Symbol.EMBASSY;
            }

            ComponentBuilder hover = new ComponentBuilder();
            hover.append(messages.getMessage("hover.info").getReplacedComponents(
                    "state", land.getLandOwner().getStateTag(),
                    "name", land.getLandOwner().getName()
            ));
            if (land.getLandOwner().isAbandoned()) {
                hover.append("\n").append(messages.getMessage("hover.abandoned").getReplacedComponents(
                        "countdown", UtilTime.format(land.getLandOwner().getCreated()
                                + TimeUnit.MINUTES.toMillis(land.getLandOwner().getAbandonTimeConfig()) - System.currentTimeMillis())
                ));
            } else {
                if (!land.getDescription().isEmpty()) {
                    hover.append("\n").append(land.getDescription());
                }
                if (messages.getMessage("hover." + symbol.getKey()).exists()) {
                    hover.append("\n").append(messages.getMessage("hover." + symbol.getKey()).getReplacedComponents(
                            "owner", land.isPlot() ? ResidentManager.getInstance().getResidentName(land.getPlotOwner()) : "none",
                            "price", land.isForSale() ? String.valueOf(land.getPrice()) : messages.getText("not-for-sale"),
                            "tax", land.isForSale() ? String.valueOf(land.getTax()) : "0",
                            "description", land.getDescription(),
                            "name", land.getOutpostName()
                    ));
                }
            }

            if (current) {
                symbol = Symbol.CURRENT;
            }

            ComponentBuilder cb = new ComponentBuilder(MessageUtils.replaceColorCodes(symbol.getText())).color(relation.getColor());
            if (canLinkPlot && land.isPlotOwner(playerLand.getPlotOwner())) {
                if (land.hasLink()) {
                    hover.append("\n").append(messages.getMessage("hover.linked")
                            .getReplacedComponents(
                                    "location", x + "-" + z,
                                    "x", String.valueOf(x),
                                    "z", String.valueOf(z),
                                    "linked", String.valueOf(land.getLinked().size() - 1),
                                    "linkgroup", Base64.getEncoder().encodeToString(land.getLink().toString().getBytes()).substring(0, 4)
                            ));
                }
                if (!current) {
                    if (land.isLinked(playerLand)) {
                        hover.append("\n").append(messages.getMessage("hover.click-to-unlink")
                                .getReplacedComponents(
                                        "location", x + "-" + z,
                                        "x", String.valueOf(x),
                                        "z", String.valueOf(z)
                                ));
                        cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot unlink " + x + " " + z + " map"));
                    } else {
                        hover.append("\n").append(messages.getMessage("hover.click-to-link")
                                .getReplacedComponents(
                                        "location", x + "-" + z,
                                        "x", String.valueOf(x),
                                        "z", String.valueOf(z)
                                ));
                        cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot link " + x + " " + z + " map"));
                    }
                }
            } else if (claimable && canUnclaimCoords && land.isOwnedBy(landOwner)) {
                hover.append("\n").append(messages.getMessage("hover.click-to-unclaim")
                        .getReplacedComponents(
                                "location", x + "-" + z,
                                "x", String.valueOf(x),
                                "z", String.valueOf(z)
                        ));
                cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + landOwner.getKey() + " unclaim " + x + " " + z + " map"));
            }

            return cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover.create()))).create();
        }

        public List<RowEnd> getRowEnds() {
            List<RowEnd> ends = new ArrayList<>();
            for (RowEnd rowEnd : RowEnd.values()) {
                if (legend.contains(rowEnd)) {
                    ends.add(rowEnd);
                }
            }
            return ends;
        }
    }

    enum Relation implements ConfigKey {
        OWN,
        OWN_EMPIRE,
        OWN_PLOT,
        OWN_PLOT_LINKED,
        OWN_PLOT_LINKED_CURRENT,
        EMPIRE,
        ALLY,
        ENEMY,
        UNCLAIMED,
        CURRENT;

        private final ChatColor color;

        Relation() {
            ChatColor temp;
            try {
                temp = ChatColor.valueOf(ConfigManager.getInstance().getMapConfig().getString("color." + getKey()).toUpperCase());
            } catch (IllegalArgumentException e) {
                temp = ChatColor.GRAY;
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Wrong color string for " + name() + "! " + e.getMessage());
            }
            color = temp;
        }

        public ChatColor getColor() {
            return color;
        }
    }

    enum Symbol implements ConfigKey {
        NORMAL,
        FOR_SALE,
        PLOT,
        EMBASSY,
        HOME,
        OUTPOST,
        UNCLAIMED,
        OTHER,
        CURRENT;

        private final String text;

        Symbol() {
            text = ConfigManager.getInstance().getMapConfig().getString("symbol." + getKey());
        }

        public String getText() {
            return text;
        }
    }

    enum RowEnd implements ConfigKey {
        OWN_LAND,
        EMPIRE_LAND,
        FOR_SALE,
        PLAYER_PLOT,
        LINKED_PLOT,
        EMBASSY,
        HOME,
        OUTPOST,
        EMPIRE,
        ALLY,
        ENEMY,
        UNCLAIMED,
        CURRENT
    }
}

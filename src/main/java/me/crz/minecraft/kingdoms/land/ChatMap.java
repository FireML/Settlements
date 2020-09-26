package me.crz.minecraft.kingdoms.land;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ChatMap {
    private static final int HEIGHT = 13;
    private static final int WIDTH = 29;
    private final ConfigMessages messages;

    public ChatMap(Player player, ConfigMessages messages) {
        this.messages = messages;
        BaseComponent[][][] map = new ChatMap.MapGenerator(player).getComponents();
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
        List<String> lines = messages.getMessage("explanation").getTextList();
        if (lines.size() > row) {
            return TextComponent.fromLegacyText(lines.get(row));
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

        public MapGenerator(Player p) {
            this.playerLand = LandManager.getInstance().getLand(p.getLocation());
            this.claimable = Utils.isWorldClaimable(p.getWorld());
            this.player = p;
            this.resident = ResidentManager.getInstance().getResident(p);
            this.canLinkPlot = playerLand != null && (playerLand.isPlotOwner(resident) || playerLand.canAdmin(resident));
            if (resident.hasVillage()) {
                this.canClaimCoords = resident.hasPermission("kingdoms.command.village.claim.coords");
                this.canUnclaimCoords = resident.hasPermission("kingdoms.command.village.unclaim.coords");
                for (NamedLocation o : resident.getVillage().getOutpostHomes().values()) {
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
                if (claimable && resident.hasVillage() && canClaimCoords) {
                    cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/village claim " + x + " " + z + " map"));
                    cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(messages.getMessage("hover.click-to-claim")
                            .getReplacedComponents(
                                    "location", x + "-" + z,
                                    "centerx", String.valueOf(x * 16 + 8),
                                    "centerz", String.valueOf(z * 16 + 8),
                                    "x", String.valueOf(x),
                                    "z", String.valueOf(z)
                            ))));
                }
                return cb.color(Relation.UNCLAIMED.getColor()).create();
            }
            Symbol symbol = Symbol.NORMAL;
            Relation relation;
            if (land.isOutpost()) {
                symbol = Symbol.OUTPOST;
            }

            if (land.getLandOwner().isMember(resident)) {
                relation = Relation.OWN;
                if (land.isOutpost() || ownOutposts.contains(land)) {
                    relation = Relation.OWN_OUTPOST;
                }
                if (land.isForSale()) {
                    symbol = Symbol.FOR_SALE;
                }
                if (land.containsHome()) {
                    symbol = Symbol.HOME;
                }

            } else if (resident.hasVillage() && resident.getVillage().isAlly(land.getLandOwner())) {
                relation = Relation.ALLY;
            } else if (resident.hasEmpire() && resident.getVillage().isTrusted(land.getLandOwner())) {
                relation = Relation.EMPIRE;
            } else {
                relation = Relation.ENEMY;
            }

            if (land.isPlot()) {
                if (land.isPlotOwner(resident)) {
                    if (land.isLinked(playerLand)) {
                        relation = Relation.OWN_PLOT_LINKED_CURRENT;
                    } else if (land.hasLink()) {
                        relation = Relation.OWN_PLOT_LINKED;
                    } else {
                        relation = Relation.OWN_PLOT;
                    }
                }
                symbol = Symbol.PLOT;
            }

            if (land.isEmbassy()) {
                symbol = Symbol.EMBASSY;
            }

            ComponentBuilder hover = new ComponentBuilder(land.getLandOwner().getName());
            if (land.getLandOwner().isAbandoned()) {
                hover.append("\n").append(messages.getMessage("hover.abandoned").getReplacedComponents(
                        "countdown", UtilTime.format(land.getLandOwner().getCreated() + TimeUnit.MINUTES.toMillis(Config.Empire.Upkeep.ABANDON_TIME) - System.currentTimeMillis())
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
            } else if (claimable && canUnclaimCoords && land.isOwnedBy(resident.getVillage())) {
                hover.append("\n").append(messages.getMessage("hover.click-to-unclaim")
                        .getReplacedComponents(
                                "location", x + "-" + z,
                                "x", String.valueOf(x),
                                "z", String.valueOf(z)
                        ));
                cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/village unclaim " + x + " " + z + " map"));
            }

            return cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover.create()))).create();
        }
    }

    enum Relation implements ConfigKey {
        OWN,
        OWN_OUTPOST,
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
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Wrong color string for " + name() + "! " + e.getMessage());
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
}

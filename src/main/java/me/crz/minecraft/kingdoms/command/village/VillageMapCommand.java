package me.crz.minecraft.kingdoms.command.village;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageMapCommand extends SubCommand {
    private static final int HEIGHT = 13;
    private static final int WIDTH = 29;

    public VillageMapCommand(SubCommand parent) {
        super(parent, "map");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Utils.runAsync(() -> player.spigot().sendMessage(getMap(generateMap(player), player)));
        return true;
    }

    private BaseComponent[][][] generateMap(Player player) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        BaseComponent[][][] map = new BaseComponent[HEIGHT][WIDTH][0];
        int x = (player.getLocation().getBlockX() >> 4) - HEIGHT / 2;
        int z = (player.getLocation().getBlockZ() >> 4) - WIDTH / 2;
        for (int countX = 0; countX < HEIGHT; countX++) {
            for (int countZ = 0; countZ < WIDTH; countZ++) {
                map[countX][countZ] = getSymbol(x + countX, z + countZ, player.getLocation().getWorld(), resident, countX == HEIGHT / 2 && countZ == WIDTH / 2);
            }
            ++countX;
        }
        return map;
    }

    private BaseComponent[] getSymbol(int x, int z, World world, Resident resident, boolean current) {
        OwnedLand land = LandManager.getInstance().getLand(x, z, world);
        if (land == null) {
            ComponentBuilder cb = new ComponentBuilder((current ? Symbol.CURRENT : Symbol.NORMAL).getText());
            if (resident.hasVillage() && resident.getVillageRank().atLeast(VillageRank.ELDER)) {
                cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/village claim " + x + " " + z));
                cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getMessage("hover.click-to-claim")
                        .getReplacedComponents("location", x + "-" + z)));
            }
            return cb.color(Relation.UNCLAIMED.getColor()).create();
        }
        Symbol symbol = Symbol.NORMAL;
        Relation relation;
        if (land.isOutpost()) {
            symbol = Symbol.OUTPOST;
        }
        if (land.getOwnerVillage().equals(resident.getVillage())) {
            relation = Relation.OWN;
            if (land.isPlot()) {
                if (land.isPlotOwner(resident)) {
                    relation = Relation.OWN_PLOT;
                }
                symbol = Symbol.PLOT;
            } else if (land.isForSale()) {
                symbol = Symbol.FOR_SALE;
            }
        } else if (resident.hasVillage() && resident.getVillage().getAllies().contains(land.getOwnerVillage().getId())) {
            relation = Relation.ALLY;
        } else {
            relation = Relation.ENEMY;
        }

        ComponentBuilder hover = new ComponentBuilder(land.getOwnerVillage().getName());
        if (!land.getDescription().isEmpty()) {
            hover.append("\n").append(land.getDescription());
        }
        if (getMessage("hover." + symbol.getKey()).exists()) {
            hover.append("\n").append(getMessage("hover." + symbol.getKey()).getReplacedComponents(
                    "owner", land.isPlot() ? ResidentManager.getInstance().getResidentName(land.getPlotOwner()) : "none",
                    "price", land.isForSale() ? String.valueOf(land.getPrice()) : "not for sale",
                    "name", land.getOutpostName()
            ));
        }

        if (current) {
            symbol = Symbol.CURRENT;
        }
        return new ComponentBuilder(MessageUtils.replaceColorCodes(symbol.getText()))
                .color(relation.getColor())
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()))
                .create();
    }

    private String rpGetPlayerDirection(Player playerSelf) {
        String dir;
        float y = playerSelf.getLocation().getYaw();
        if (y < 0.0f) {
            y += 360.0f;
        }
        y %= 360.0f;
        int i = (int) ((y + 8.0f) / 22.5);
        if (i == 0) {
            dir = "S";
        } else if (i == 2) {
            dir = "0/";
        } else if (i == 4) {
            dir = "W";
        } else if (i == 6) {
            dir = "0\\";
        } else if (i == 8) {
            dir = "N";
        } else if (i == 10) {
            dir = "2\\";
        } else if (i == 12) {
            dir = "E";
        } else if (i == 14) {
            dir = "2/";
        } else {
            dir = "S";
        }
        return dir;
    }

    public List<String> getRowSymbols(Player p) {
        String tmp = this.rpGetPlayerDirection(p);
        List<String> temp = Arrays.asList("&e\\&eN&e/&r", "&eW&e+&eE&r", "&e/&eS&e\\&r");
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
        List<String> lines = getMessage("explanation").getTextList();
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

    private BaseComponent[] getMap(BaseComponent[][][] map, Player p) {
        ComponentBuilder cb = new ComponentBuilder("").append(getMessage("head").getComponents());
        for (int i = 0; i < map.length; ++i) {
            cb.append("\n", ComponentBuilder.FormatRetention.NONE);
            BaseComponent[] rowBase = this.getRowBase(i, p);
            if (rowBase.length > 0) {
                cb.append(rowBase);
            }
            for (int j = this.getStart(i); j < 29; ++j) {
                cb.append(map[i][j], ComponentBuilder.FormatRetention.NONE);
            }
            BaseComponent[] rowEnd = this.getRowEnd(i);
            if (rowEnd.length > 0) {
                cb.append(rowEnd, ComponentBuilder.FormatRetention.NONE);
            }
        }
        return cb.create();
    }

    enum Relation implements ConfigKey {
        OWN,
        OWN_PLOT,
        ALLY,
        ENEMY,
        UNCLAIMED,
        CURRENT;

        private final ChatColor color;

        Relation() {
            ChatColor temp;
            try {
                temp = ChatColor.valueOf(KingdomsPlugin.getInstance().getConfig().getString("map.color." + getKey()).toUpperCase());
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
        HOME,
        OUTPOST,
        UNCLAIMED,
        ALLY,
        OTHER,
        CURRENT;

        private final String text;

        Symbol() {
            text = KingdomsPlugin.getInstance().getConfig().getString("map.symbol." + getKey());
        }

        public String getText() {
            return text;
        }
    }
}

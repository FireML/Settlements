package me.crz.minecraft.kingdoms.command.village;

import java.util.Arrays;
import java.util.List;

import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageMapCommand extends SubCommand {
    private int[] offset1;
    private static int[] offset2 = new int[]{-14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};

    public VillageMapCommand() {
        super("Shows you a map of the surrounding chunks", "map");
        offset1 = new int[]{-6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6};
        this.setPermission("kingdoms.user");
        this.setUsage("map");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Utils.runAsync(() -> player.spigot().sendMessage(getMap(generateMap(player), player)));
    }

    private BaseComponent[][][] generateMap(Player player) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        BaseComponent[][][] map = new BaseComponent[13][29][0];
        int x = player.getLocation().getChunk().getX();
        int z = player.getLocation().getChunk().getZ();
        int countX = 0;
        int[] offset1;
        for (int length = (offset1 = this.offset1).length, k = 0; k < length; ++k) {
            int i = offset1[k];
            int countZ = 0;
            int[] offset2;
            for (int length2 = (offset2 = VillageMapCommand.offset2).length, l = 0; l < length2; ++l) {
                int j = offset2[l];
                map[countX][countZ] = this.getSymbol(x + j, z + i, player.getLocation().getWorld(), village, (countX == 6 && countZ == 14) ? "+" : "-", player);
                ++countZ;
            }
            ++countX;
        }
        return map;
    }

    private BaseComponent[] getSymbol(int x, int z, World world, Village village, String symbol, Player player) {
        OwnedLand land = LandManager.getInstance().getLand(x, z, world);
        if (land == null) {
            return new ComponentBuilder(symbol).color(ChatColor.GRAY).create();
        }
        if (land.getIsOutpost()) {
            symbol = "@";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n&a&lPlot Owner: &7");
        if (land.getPlotOwner() != null) {
            try {
                sb.append(ResidentManager.getInstance().getResident(land.getPlotOwner()).getName());
                sb.append("\n");
            } catch (Exception e) {
                sb.append("none\n");
            }
        } else {
            sb.append("none\n");
        }
        ChatColor color;
        if (land.getOwnerVillage().equals(village)) {
            if (land.isPlotOwner(player)) {
                color = ChatColor.YELLOW;
            } else {
                if (land.isForSale()) {
                    symbol = symbol.equals("-") ? "$" : "+";
                }
                color = ChatColor.GREEN;
            }
        } else {
            if (village != null && village.getAllies().contains(land.getOwnerVillage().getId())) {
                color = ChatColor.AQUA;
            } else {
                color = ChatColor.RED;
            }
        }
        return new ComponentBuilder(symbol)
                .color(color)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(land.getOwnerVillage().getName())
                                .append(MessageUtils.toColoredComponents(sb.toString()))
                                .create()))
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

    private  BaseComponent[] getRowEnd(int row) {
        switch (row) {
            case 0:
                return MessageUtils.toColoredComponents(" &a-&r&6 = your land");
            case 1:
                return MessageUtils.toColoredComponents(" &a$&r&6 = land for sale");
            case 2:
                return MessageUtils.toColoredComponents(" &7-&r&6 = unclaimed land");
            case 3:
                return MessageUtils.toColoredComponents(" &b-&r&6 = ally land");
            case 4:
                return MessageUtils.toColoredComponents(" &c-&r&6 = other land");
            case 5:
                return MessageUtils.toColoredComponents(" &e+&r&6 = current land");
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
        ComponentBuilder cb = new ComponentBuilder("")
                .append(MessageUtils.toColoredComponents("&8---------------- &eSettlement Map &r&8----------------"));
        for (int i = 0; i < 13; ++i) {
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
            cb.create();
        }
        return cb.create();
    }
}

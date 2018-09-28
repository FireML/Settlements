package me.crz.minecraft.kingdoms.command.village;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

import net.md_5.bungee.api.chat.TextComponent;
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
        this.setPermission("village.user");
        this.setUsage("map");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            player.sendMessage(MessageUtils.replaceColorCodes("&7--------------&eSettlement Map&r&7-------------"));
            for (TextComponent component : getMap(generateMap(player), player)) {
                player.spigot().sendMessage(component);
            }
        });
    }

    public TextComponent[][] generateMap(Player player) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        TextComponent[][] map = new TextComponent[13][29];
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

    public TextComponent getSymbol(int x, int z, World world, Village village, String symbol, Player player) {
        OwnedLand land = LandManager.getInstance().getLand(x, z, world);
        if (land == null) {
            TextComponent component = new TextComponent(MessageUtils.replaceColorCodes("&7" + symbol));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()));
            return component;
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
        if (village != null && land.getOwnerVillage().equals(village.getName())) {
            if (land.isPlotOwner(player)) {
                TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&e" + symbol));
                component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
                return component2;
            }
            if (land.isForSale()) {
                TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&a" + ((symbol.equals("-")) ? "$" : ((!symbol.equals("+")) ? "+" : symbol))));
                component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
                return component2;
            }
            TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&a" + symbol));
            component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
            return component2;
        } else {
            if (village != null && village.getAllies().contains(land.getOwnerVillage())) {
                TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&b" + symbol));
                component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
                return component2;
            }
            TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&c" + symbol));
            component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
            return component2;
        }
    }

    public String rpGetPlayerDirection(Player playerSelf) {
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
            if (tmp.contains(new StringBuilder(String.valueOf(i)).toString())) {
                temp.set(i, temp.get(i).replace(tmp.replace(String.valueOf(i), ""), "&c" + tmp.replace(String.valueOf(i), "")));
            } else if (!temp.contains("0") && !temp.contains("2")) {
                temp.set(i, temp.get(i).replace(tmp, "&c" + tmp));
            }
        }
        return temp;
    }

    public TextComponent getRowBase(int row, Player p) {
        List<String> tmp = this.getRowSymbols(p);
        if (row == 0) {
            return new TextComponent(MessageUtils.replaceColorCodes(tmp.get(0)));
        }
        if (row == 1) {
            return new TextComponent(MessageUtils.replaceColorCodes(tmp.get(1)));
        }
        if (row == 2) {
            return new TextComponent(MessageUtils.replaceColorCodes(tmp.get(2)));
        }
        return new TextComponent();
    }

    public TextComponent getRowEnd(int row) {
        if (row == 0) {
            return new TextComponent(MessageUtils.replaceColorCodes(" &a-&r&6 = your land"));
        }
        if (row == 1) {
            return new TextComponent(MessageUtils.replaceColorCodes(" &a$&r&6 = land for sale"));
        }
        if (row == 2) {
            return new TextComponent(MessageUtils.replaceColorCodes(" &7-&r&6 = unclaimed land"));
        }
        if (row == 3) {
            return new TextComponent(MessageUtils.replaceColorCodes(" &b-&r&6 = ally land"));
        }
        if (row == 4) {
            return new TextComponent(MessageUtils.replaceColorCodes(" &c-&r&6 = other land"));
        }
        if (row == 5) {
            return new TextComponent(MessageUtils.replaceColorCodes(" &e+&r&6 = current land"));
        }
        return new TextComponent();
    }

    public int getStart(int row) {
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

    private List<TextComponent> getMap(TextComponent[][] map, Player p) {
        List<TextComponent> components = new ArrayList<>();
        for (int i = 0; i < 13; ++i) {
            TextComponent component = new TextComponent(this.getRowBase(i, p));
            for (int j = this.getStart(i); j < 29; ++j) {
                component.addExtra(new TextComponent(map[i][j]));
            }
            component.addExtra(this.getRowEnd(i));
            components.add(component);
        }
        return components;
    }
}

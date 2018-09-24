package me.crz.minecraft.kingdoms.command.village;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.MojangAPI;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import java.util.Iterator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.KingdomRunnable;
import me.crz.minecraft.kingdoms.utils.ThreadManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageMapCommand extends SubCommand
{
    int[] offset1;
    static int[] offset2;
    
    static {
        offset2 = new int[] { -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
    }
    
    public VillageMapCommand() {
        super("Shows you a map of the surrounding chunks", new String[] { "map" });
        offset1 = new int[] { -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6 };
        this.setPermission("village.user");
        this.setUsage("map");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        ThreadManager.getInstance().execute(new KingdomRunnable() {
            final /* synthetic */ VillageMapCommand this$0;
            private final /* synthetic */ Player val$player;
            
            VillageMapCommand$1() {
                this$0 = this$0;
                super();
            }
            
            @Override
            public void run() {
                player.sendMessage(MessageUtils.replaceColorCodes("&7--------------&eSettlement Map&r&7-------------"));
                for (final TextComponent component : this$0.getMap(this$0.generateMap(player), player)) {
                    player.spigot().sendMessage((BaseComponent)component);
                }
            }
        });
    }
    
    public TextComponent[][] generateMap(final Player player) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        final TextComponent[][] map = new TextComponent[13][29];
        final int x = player.getLocation().getChunk().getX();
        final int z = player.getLocation().getChunk().getZ();
        int countX = 0;
        int[] offset1;
        for (int length = (offset1 = this.offset1).length, k = 0; k < length; ++k) {
            final int i = offset1[k];
            int countZ = 0;
            int[] offset2;
            for (int length2 = (offset2 = VillageMapCommand.offset2).length, l = 0; l < length2; ++l) {
                final int j = offset2[l];
                map[countX][countZ] = this.getSymbol(x + j, z + i, player.getLocation().getWorld().getName(), village, (countX == 6 && countZ == 14) ? "+" : "-", player);
                ++countZ;
            }
            ++countX;
        }
        return map;
    }
    
    public TextComponent getSymbol(final int x, final int z, final String worldName, final Village village, String symbol, final Player player) {
        final OwnedLand land = LandManager.getInstance().getLand(x, z, worldName);
        if (land == null) {
            final TextComponent component = new TextComponent(MessageUtils.replaceColorCodes("&7" + symbol));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").create()));
            return component;
        }
        if (land.getIsOutpost()) {
            symbol = "@";
        }
        final StringBuilder sb = new StringBuilder("");
        sb.append("\n&a&lPlot Owner: &7");
        if (land.getPlotOwner() != "" && land.getPlotOwner() != null && !land.getPlotOwner().isEmpty()) {
            try {
                sb.append(MojangAPI.getInstance().getName(land.getPlotOwner()));
                sb.append("\n");
            }
            catch (Exception e) {
                sb.append("none\n");
            }
        }
        else {
            sb.append("none\n");
        }
        if (village != null && land.getOwnerVillage().equals(village.getName())) {
            if (land.getPlotOwner() == player.getUniqueId().toString()) {
                final TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&e" + symbol));
                component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
                return component2;
            }
            if (land.isForSale()) {
                final TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&a" + ((symbol == "-") ? "$" : ((symbol != "+") ? "+" : symbol))));
                component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
                return component2;
            }
            final TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&a" + symbol));
            component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
            return component2;
        }
        else {
            if (village != null && village.getAllies().contains(land.getOwnerVillage())) {
                final TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&b" + symbol));
                component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
                return component2;
            }
            final TextComponent component2 = new TextComponent(MessageUtils.replaceColorCodes("&c" + symbol));
            component2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.valueOf(land.getOwnerVillage()) + MessageUtils.replaceColorCodes(sb.toString())).create()));
            return component2;
        }
    }
    
    public String rpGetPlayerDirection(final Player playerSelf) {
        String dir = "";
        float y = playerSelf.getLocation().getYaw();
        if (y < 0.0f) {
            y += 360.0f;
        }
        y %= 360.0f;
        final int i = (int)((y + 8.0f) / 22.5);
        if (i == 0) {
            dir = "S";
        }
        else if (i == 2) {
            dir = "0/";
        }
        else if (i == 4) {
            dir = "W";
        }
        else if (i == 6) {
            dir = "0\\";
        }
        else if (i == 8) {
            dir = "N";
        }
        else if (i == 10) {
            dir = "2\\";
        }
        else if (i == 12) {
            dir = "E";
        }
        else if (i == 14) {
            dir = "2/";
        }
        else {
            dir = "S";
        }
        return dir;
    }
    
    public List<String> getRowSymbols(final Player p) {
        final String tmp = this.rpGetPlayerDirection(p);
        final List<String> temp = Arrays.<String>asList("&e\\&eN&e/&r", "&eW&e+&eE&r", "&e/&eS&e\\&r");
        for (int i = 0; i < 3; ++i) {
            if (tmp.contains(new StringBuilder(String.valueOf(i)).toString())) {
                temp.set(i, ((String)temp.get(i)).replace((CharSequence)tmp.replace(new StringBuilder(String.valueOf(i)).toString(), ""), (CharSequence)("&c" + tmp.replace((CharSequence)new StringBuilder(String.valueOf(i)).toString(), (CharSequence)""))));
            }
            else if (!temp.contains("0") && !temp.contains("2")) {
                temp.set(i, ((String)temp.get(i)).replace((CharSequence)tmp, (CharSequence)("&c" + tmp)));
            }
        }
        return temp;
    }
    
    public TextComponent getRowBase(final int row, final Player p) {
        final List<String> tmp = this.getRowSymbols(p);
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
    
    public TextComponent getRowEnd(final int row) {
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
    
    public int getStart(final int row) {
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
    
    private List<TextComponent> getMap(final TextComponent[][] map, final Player p) {
        final List<TextComponent> components = new ArrayList<TextComponent>();
        for (int i = 0; i < 13; ++i) {
            final TextComponent component = new TextComponent(this.getRowBase(i, p));
            for (int j = this.getStart(i); j < 29; ++j) {
                component.addExtra((BaseComponent)new TextComponent(map[i][j]));
            }
            component.addExtra((BaseComponent)this.getRowEnd(i));
            components.add(component);
        }
        return components;
    }
    
    static /* synthetic */ List access$0(final VillageMapCommand villageMapCommand, final TextComponent[][] map, final Player p3) {
        return villageMapCommand.getMap(map, p3);
    }
}

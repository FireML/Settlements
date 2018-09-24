package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Utils;
import java.util.Iterator;
import java.util.List;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Village;
import java.util.Collection;
import java.util.ArrayList;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.landflags.PlotUseGui;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.utils.MojangAPI;
import java.util.Arrays;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillagePlotCommand extends SubCommand
{
    public VillagePlotCommand() {
        super("Plot Commands", new String[] { "plot" });
        this.setPermission("village.user");
        this.setUsage("plot <buy|revoke|sale|mode>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village senderVillage = VillageManager.getInstance().getVillageFromPlayer(player);
        if (senderVillage == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        final OwnedLand land = LandManager.getInstance().getLandFromLocation(player.getLocation());
        if (land == null || !land.getOwnerVillage().equals(senderVillage.getName())) {
            MessageUtils.tell(player, "&c&lError &r&7You need to own the land to make it a plot", "{owner}", (land == null) ? "null" : land.getOwnerVillage());
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        final Village landVillage = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        if (args.length == 0) {
            MessageUtils.tellList(player, Arrays.<String>asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r &8&l--------", "&8[&9*&8] &b/" + base + " plot sale <amount> &7- places a price on the claimed chunk", "&8[&9*&8] &b/" + base + " plot info", "&8[&9*&8] &b/" + base + " plot mode &7- changes mode of the plot your currently standin in", "&8[&9*&8] &b/" + base + " plot buy &7- buys the currently chunk your standing in", "&8[&9*&8] &b/" + base + " plot revoke &7- removes the players owned claims"));
            return;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("info")) {
                final StringBuilder sb = new StringBuilder();
                sb.append("&8&l-------- &r&c&lPlot Info&r &8&l--------");
                sb.append("\n");
                sb.append("&c&lVillage: &7");
                sb.append(landVillage.getName());
                sb.append("\n");
                sb.append("&9&lModes: &7MobSpawning: &6");
                sb.append(land.isMobSpawning());
                sb.append("&r&7 | FireSpread: &6");
                sb.append(land.isFireSpread());
                sb.append("&r&7 | Explosions: &6");
                sb.append(land.isExplosions());
                sb.append("&r&7 | PvP: &6");
                sb.append(land.isPvp());
                sb.append("&r&7 | Build: &6");
                sb.append(land.getBuild());
                sb.append("\n");
                if (land.getPlotOwner() != "" && land.getPlotOwner() != null) {
                    sb.append("&a&lPlot Owner: &7");
                    sb.append(MojangAPI.getInstance().getName(land.getPlotOwner()));
                    sb.append("\n");
                }
                else if (land.isForSale()) {
                    sb.append("&a&lPlot Price: &7");
                    sb.append(land.getPrice());
                    sb.append("\n");
                }
                player.sendMessage(MessageUtils.replaceColorCodes(sb.toString()));
            }
            if (args[0].equalsIgnoreCase("help")) {
                MessageUtils.tellList(player, Arrays.<String>asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r &8&l--------", "&8[&9*&8] &b/" + base + " plot sale <amount> &7- places a price on the claimed chunk", "&8[&9*&8] &b/" + base + " plot info", "&8[&9*&8] &b/" + base + " plot mode &7- changes mode of the plot your currently standin in", "&8[&9*&8] &b/" + base + " plot buy &7- buys the currently chunk your standing in", "&8[&9*&8] &b/" + base + " plot build &7- sets who can build in the current plot", "&8[&9*&8] &b/" + base + " plot revoke &7- removes the players owned claims"));
            }
            if (args[0].equalsIgnoreCase("buy")) {
                if (!this.playerOwnsLand(player, landVillage)) {
                    MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
                    return;
                }
                if (!land.isForSale()) {
                    MessageUtils.tell(player, "&cLand not for sale");
                    return;
                }
                if (Base.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) < land.getPrice()) {
                    MessageUtils.tell(player, "&cYou cannot afford the &6$" + land.getPrice() + "&c that the land costs to buy!");
                    return;
                }
                Base.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), (double)land.getPrice());
                landVillage.setBank(landVillage.getBank() + land.getPrice());
                VillageManager.getInstance().saveVillage(landVillage);
                land.setForSale(false);
                land.setPrice(0);
                land.setPlotOwner(player.getUniqueId().toString());
                LandManager.getInstance().saveLand(land);
                MessageUtils.tell(player, "&aSuccessfully purchased the land.");
                return;
            }
            else if (args[0].equalsIgnoreCase("unsale")) {
                if (!this.playerOwnsLand(player, landVillage)) {
                    MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
                    return;
                }
                if (!land.isForSale()) {
                    MessageUtils.tell(player, "&cLand not for sale");
                    return;
                }
                if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                    MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(senderVillage.getState()) + " or higher&r&c&l!", null, null);
                    return;
                }
                land.setForSale(false);
                land.setPrice(0);
                LandManager.getInstance().saveLand(land);
                MessageUtils.tell(player, "&aThis land is no longer for sale!");
                return;
            }
            else if (args[0].equalsIgnoreCase("revoke")) {
                if (!this.playerIsElderOrChief(player, senderVillage)) {
                    MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
                    return;
                }
                if (!this.landIsPlot(land)) {
                    MessageUtils.tell(player, "&cNobody owns this land.");
                    return;
                }
                land.setPlotOwner("");
                LandManager.getInstance().saveLand(land);
                MessageUtils.tell(player, "&aPlot has been revoked ownership!");
            }
        }
        if (args[0].equalsIgnoreCase("sale")) {
            if (args.length < 2) {
                if (land.isForSale()) {
                    MessageUtils.tell(player, "&7&lThe current price of this land is: &6&l " + land.getPrice());
                }
                else {
                    MessageUtils.tell(player, "&c&lError &r&7You need to specify how much to sell the plot for!");
                }
                return;
            }
            if (!this.playerOwnsLand(player, landVillage)) {
                MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
                return;
            }
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(senderVillage.getState()) + " or higher&r&c&l!");
                return;
            }
            if (this.landIsPlot(land)) {
                MessageUtils.tell(player, "&cLand is already owned");
                return;
            }
            final int price = (int)Integer.valueOf(args[1]);
            land.setPrice(price);
            land.setForSale(true);
            LandManager.getInstance().saveLand(land);
            MessageUtils.tell(player, MessageUtils.SUCCESS_CREATE_SALE_PLOT_MESSAGE, "{price}", String.valueOf(price));
        }
        else {
            if (!args[0].equalsIgnoreCase("build")) {
                if (args[0].equalsIgnoreCase("use")) {
                    if (!land.getPlotOwner().equals(player.getUniqueId().toString())) {
                        MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
                        return;
                    }
                    if (args.length < 3) {
                        PlotUseGui.getInstance().open(player);
                        return;
                    }
                    Use[] values;
                    for (int length = (values = Use.values()).length, i = 0; i < length; ++i) {
                        final Use u = values[i];
                        if (u.name().equalsIgnoreCase(args[0])) {
                            PlotBuildState[] values2;
                            for (int length2 = (values2 = PlotBuildState.values()).length, j = 0; j < length2; ++j) {
                                final PlotBuildState state = values2[j];
                                if (state.name().equalsIgnoreCase(args[1])) {
                                    land.setUseState(u, state);
                                    LandManager.getInstance().saveLand(land);
                                    MessageUtils.tell(player, "&7Plot State of &6" + u.name() + " &7set to &6" + state.name());
                                    return;
                                }
                            }
                        }
                    }
                    MessageUtils.tell(player, "&c/s plot use <name> <buildState>");
                }
                if (args[0].equalsIgnoreCase("mode")) {
                    if (this.landIsPlot(land) && !this.playerOwnsPlot(player, land)) {
                        MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
                        return;
                    }
                    if (!this.landIsPlot(land) && !VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                        MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(senderVillage.getState()) + " or higher&r&c&l!");
                        return;
                    }
                    if (args.length < 2) {
                        MessageUtils.tell(player, "&c&lError &r&7You need to specify what to set the flag to!\n&c/" + base + alias + " mode " + String.join((CharSequence)" | ", (Iterable<? extends CharSequence>)plotMode.modes()));
                        return;
                    }
                    if (plotMode.modes().contains(args[1].toLowerCase())) {
                        final plotMode m = plotMode.valueOf(args[1]);
                        final List<String> optionStrings = new ArrayList<String>(m.getOptions().keySet());
                        if (args.length >= 3) {
                            if (m.getOptions().containsKey(args[2].toLowerCase())) {
                                m.set(m.getOptions().get(args[2].toLowerCase()), land);
                                MessageUtils.tell(player, "&7PlotMode &6" + m.name() + "&7 set to &6" + args[2]);
                            }
                            else {
                                MessageUtils.tell(player, String.valueOf(this.getUsage(base)) + " <" + String.join((CharSequence)" | ", (Iterable<? extends CharSequence>)optionStrings) + ">", null, null);
                            }
                        }
                        else {
                            MessageUtils.tell(player, String.valueOf(this.getUsage(base)) + " <" + optionStrings.get(0) + " | " + optionStrings.get(1) + ">", null, null);
                        }
                    }
                    else {
                        MessageUtils.tellList(player, Arrays.<String>asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "&8[&9*&8] &b/" + base + " plot sale <amdozzunt> &7- places a price on the claimed chunk", "&8[&9*&8] &b/" + base + " plot mode &7- changes mode of the plot your currently standin in", "&8[&9*&8] &b/" + base + " plot buy &7- buys the currently chunk your standing in", "&8[&9*&8] &b/" + base + " plot revoke &7- removes the players owned claims"));
                    }
                }
                return;
            }
            if (args.length < 2) {
                MessageUtils.tellList(player, Arrays.<String>asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "&8[&9*&8] &b/s plot build residents &7- residents and officers to build in claimed land", "&8[&9*&8] &b/s plot build officers &7- only officers are allowed to build in claimed land", "&8[&9*&8] &b/s plot build public &7- allows everyone to build in your claimed land", "&8[&9*&8] &b/s plot build ally &7- allows your allies to build in your claimed land", "&8[&9*&8] &b/s plot build private &7- allows you to build in your claimed land", "&8[&9*&8] &b/s plot build friends &7- allows your friends to build in your claimed land"));
                return;
            }
            if (!land.getPlotOwner().equals(player.getUniqueId().toString())) {
                MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE);
                return;
            }
            if (args[1].equalsIgnoreCase("residents") || args[1].equalsIgnoreCase("officers") || args[1].equalsIgnoreCase("public") || args[1].equalsIgnoreCase("ally") || args[1].equalsIgnoreCase("private") || args[1].equalsIgnoreCase("friends")) {
                final PlotBuildState newState = PlotBuildState.valueOf(args[1].toUpperCase());
                land.setBuildState(newState);
                LandManager.getInstance().saveLand(land);
                MessageUtils.tell(player, MessageUtils.SUCCESS_BUILD_STATE_MESSAGE, "{state}", args[0]);
                return;
            }
            MessageUtils.tellList(player, Arrays.<String>asList("&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------", "&8[&9*&8] &b/s plot build residents &7- residents and officers to build in claimed land", "&8[&9*&8] &b/s plot build officers &7- only officers are allowed to build in claimed land", "&8[&9*&8] &b/s plot build public &7- allows everyone to build in your claimed land", "&8[&9*&8] &b/s plot build ally &7- allows your allies to build in your claimed land", "&8[&9*&8] &b/s plot build private &7- allows you to build in your claimed land", "&8[&9*&8] &b/s plot build friends &7- allows your friends to build in your claimed land"));
        }
    }
    
    private boolean landIsPlot(final OwnedLand land) {
        return !land.getPlotOwner().equals("");
    }
    
    private boolean playerOwnsLand(final Player player, final Village landVillage) {
        for (final Resident res : landVillage.getResidents()) {
            if (res.getUUID().equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean playerOwnsPlot(final Player player, final OwnedLand land) {
        return land.getPlotOwner().equals(player.getUniqueId().toString());
    }
    
    private boolean playerIsElderOrChief(final Player player, final Village village) {
        return Utils.playerIsElder(player) || village.getChief().equals(player.getUniqueId());
    }
}

package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;

import java.util.List;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.landflags.PlotBuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.landflags.PlotUseGui;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.Base;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillagePlotCommand extends SubCommand {
    public VillagePlotCommand() {
        super("Plot Commands", "plot");
        this.setPermission("kingdoms.user");
        this.setUsage("plot <buy|revoke|sale|mode>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village senderVillage = VillageManager.getInstance().getVillageFromPlayer(player);
        if (senderVillage == null) {
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
        if (land == null || !land.getOwnerVillage().equals(senderVillage)) {
            MessageUtils.tell(player, "&c&lError &r&7You need to own the land to make it a plot", "{owner}", (land == null) ? "null" : land.getOwnerVillage().getName());
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        Village landVillage = land.getOwnerVillage();
        if (args.length == 0) {
            MessageUtils.tellList(player, Arrays.asList(
                    "&8&l-------- &r&c&l/" + base + " " + alias + "&r &8&l--------",
                    "&8[&9*&8] &b/" + base + " plot sale <amount> &7- places a price on the claimed chunk",
                    "&8[&9*&8] &b/" + base + " plot info",
                    "&8[&9*&8] &b/" + base + " plot mode &7- changes mode of the plot your currently standin in",
                    "&8[&9*&8] &b/" + base + " plot buy &7- buys the currently chunk your standing in",
                    "&8[&9*&8] &b/" + base + " plot revoke &7- removes the players owned claims"
            ));
        } else if (args[0].equalsIgnoreCase("info")) {
            Utils.runAsync(() -> {
                StringBuilder sb = new StringBuilder();
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
                sb.append(land.getBuildState().toString().toLowerCase());
                sb.append("\n");
                if (land.getPlotOwner() != null) {
                    sb.append("&a&lPlot Owner: &7");
                    sb.append(ResidentManager.getInstance().getResident(land.getPlotOwner()).getName());
                    sb.append("\n");
                } else if (land.isForSale()) {
                    sb.append("&a&lPlot Price: &7");
                    sb.append(land.getPrice());
                    sb.append("\n");
                }
                player.sendMessage(MessageUtils.replaceColorCodes(sb.toString()));
            });
        } else if (args[0].equalsIgnoreCase("help")) {
            MessageUtils.tellList(player, Arrays.asList(
                    "&8&l-------- &r&c&l/" + base + " " + alias + "&r &8&l--------",
                    "&8[&9*&8] &b/" + base + " plot sale <amount> &7- places a price on the claimed chunk",
                    "&8[&9*&8] &b/" + base + " plot info",
                    "&8[&9*&8] &b/" + base + " plot mode &7- changes mode of the plot your currently standin in",
                    "&8[&9*&8] &b/" + base + " plot buy &7- buys the currently chunk your standing in",
                    "&8[&9*&8] &b/" + base + " plot build &7- sets who can build in the current plot",
                    "&8[&9*&8] &b/" + base + " plot revoke &7- removes the players owned claims"));
        } else if (args[0].equalsIgnoreCase("buy")) {
            if (!this.playerOwnsLand(player, landVillage)) {
                MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
                return;
            }
            if (!land.isForSale()) {
                MessageUtils.tell(player, "&cLand not for sale");
                return;
            }
            if (Base.getEconomy().getBalance(player) < land.getPrice()) {
                MessageUtils.tell(player, "&cYou cannot afford the &6$" + land.getPrice() + "&c that the land costs to buy!");
                return;
            }
            Base.getEconomy().withdrawPlayer(player, (double) land.getPrice());
            landVillage.setBank(landVillage.getBank() + land.getPrice());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(landVillage));
            land.setForSale(false);
            land.setPrice(0);
            LandManager.getInstance().setPlotOwner(land, player.getUniqueId());
            MessageUtils.tell(player, "&aSuccessfully purchased the land.");
        } else if (args[0].equalsIgnoreCase("unsale")) {
            if (!this.playerOwnsLand(player, landVillage)) {
                MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
                return;
            }
            if (!land.isForSale()) {
                MessageUtils.tell(player, "&cLand not for sale");
                return;
            }
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.ELDER.getTag(senderVillage.getState()));
                return;
            }
            land.setForSale(false);
            land.setPrice(0);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
            MessageUtils.tell(player, "&aThis land is no longer for sale!");
        } else if (args[0].equalsIgnoreCase("revoke")) {
            if (!this.playerIsElderOrChief(player, senderVillage)) {
                MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
                return;
            }
            if (!this.landIsPlot(land)) {
                MessageUtils.tell(player, "&cNobody owns this land.");
                return;
            }
            LandManager.getInstance().setPlotOwner(land, null);
            MessageUtils.tell(player, "&aPlot has been revoked ownership!");
        } else if (args[0].equalsIgnoreCase("sale")) {
            if (args.length < 2) {
                if (land.isForSale()) {
                    MessageUtils.tell(player, "&7&lThe current price of this land is: &6&l " + land.getPrice());
                } else {
                    MessageUtils.tell(player, "&c&lError &r&7You need to specify how much to sell the plot for!");
                }
                return;
            } else if (!this.playerOwnsLand(player, landVillage)) {
                MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
                return;
            } else if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.ELDER.getTag(senderVillage.getState()));
                return;
            } else if (this.landIsPlot(land)) {
                MessageUtils.tell(player, "&cLand is already owned");
                return;
            }
            int price = Integer.valueOf(args[1]);
            land.setPrice(price);
            land.setForSale(true);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
            MessageUtils.tell(player, MessageUtils.Message.SUCCESS_CREATE_SALE_PLOT, "{price}", String.valueOf(price));
        } else if (args[0].equalsIgnoreCase("use")) {
            if (!land.isPlotOwner(player)) {
                MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
                return;
            }
            if (args.length < 3) {
                PlotUseGui.getInstance().open(player);
                return;
            }
            try {
                Use u = Use.valueOf(args[0].toUpperCase());
                PlotBuildState state = PlotBuildState.valueOf(args[1].toUpperCase());
                land.setUseState(u, state);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                MessageUtils.tell(player, "&7Plot State of &6" + u.name() + " &7set to &6" + state.name());
                return;
            } catch (IllegalArgumentException e) {
                MessageUtils.tell(player, "&c/s plot use <name> <buildState>");
            }
        } else if (args[0].equalsIgnoreCase("mode")) {
            if (this.landIsPlot(land) && !this.playerOwnsPlot(player, land)) {
                MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
                return;
            }
            if (!this.landIsPlot(land) && !VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.ELDER.getTag(senderVillage.getState()));
                return;
            }
            if (args.length < 3) {
                MessageUtils.tell(player, "&c&lError &r&7You need to specify what to set the flag to!\n&c/" + base + alias + " mode " + String.join(" | ", PlotMode.modes()));
                return;
            }
            try {
                PlotMode m = PlotMode.valueOf(args[1].toLowerCase());
                List<String> optionStrings = new ArrayList<>(m.getOptions().keySet());
                if (m.getOptions().containsKey(args[2].toLowerCase())) {
                    m.set(m.getOptions().get(args[2].toLowerCase()), land);
                    MessageUtils.tell(player, "&7PlotMode &6" + m.name() + "&7 set to &6" + args[2]);
                } else {
                    MessageUtils.tell(player, String.valueOf(this.getUsage(base)) + " <" + String.join(" | ", optionStrings) + ">", null, null);
                }
            } catch (IllegalArgumentException e) {
                MessageUtils.tellList(player, Arrays.asList(
                        "&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------",
                        "&8[&9*&8] &b/" + base + " plot sale <amdozzunt> &7- places a price on the claimed chunk",
                        "&8[&9*&8] &b/" + base + " plot mode &7- changes mode of the plot your currently standin in",
                        "&8[&9*&8] &b/" + base + " plot buy &7- buys the currently chunk your standing in",
                        "&8[&9*&8] &b/" + base + " plot revoke &7- removes the players owned claims"));
            }
        } else if (args[0].equalsIgnoreCase("build")) {
            if (args.length < 2) {
                MessageUtils.tellList(player, Arrays.asList(
                        "&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------",
                        "&8[&9*&8] &b/s plot build residents &7- residents and officers to build in claimed land",
                        "&8[&9*&8] &b/s plot build officers &7- only officers are allowed to build in claimed land",
                        "&8[&9*&8] &b/s plot build public &7- allows everyone to build in your claimed land",
                        "&8[&9*&8] &b/s plot build ally &7- allows your allies to build in your claimed land",
                        "&8[&9*&8] &b/s plot build private &7- allows you to build in your claimed land",
                        "&8[&9*&8] &b/s plot build friends &7- allows your friends to build in your claimed land"));
                return;
            }
            if (!player.getUniqueId().equals(land.getPlotOwner())) {
                MessageUtils.tell(player, MessageUtils.Message.NO_PERMISSION);
                return;
            }
            try {
                PlotBuildState newState = PlotBuildState.valueOf(args[1].toUpperCase());
                land.setBuildState(newState);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                MessageUtils.tell(player, MessageUtils.Message.SUCCESS_BUILD_STATE, "{state}", newState.toString());
            } catch (IllegalArgumentException e) {
                MessageUtils.tellList(player, Arrays.asList(
                        "&8&l-------- &r&c&l/" + base + " " + alias + "&r " + "&8&l--------",
                        "&8[&9*&8] &b/s plot build residents &7- residents and officers to build in claimed land",
                        "&8[&9*&8] &b/s plot build officers &7- only officers are allowed to build in claimed land",
                        "&8[&9*&8] &b/s plot build public &7- allows everyone to build in your claimed land",
                        "&8[&9*&8] &b/s plot build ally &7- allows your allies to build in your claimed land",
                        "&8[&9*&8] &b/s plot build private &7- allows you to build in your claimed land",
                        "&8[&9*&8] &b/s plot build friends &7- allows your friends to build in your claimed land"));
            }
        }
    }

    private boolean landIsPlot(OwnedLand land) {
        return land.isPlot();
    }

    private boolean playerOwnsLand(Player player, Village landVillage) {
        return landVillage.getResidentIds().contains(player.getUniqueId());
    }

    private boolean playerOwnsPlot(Player player, OwnedLand land) {
        return player.getUniqueId().equals(land.getPlotOwner());
    }

    private boolean playerIsElderOrChief(Player player, Village village) {
        return Utils.playerIsElder(player) || village.getChief().equals(player.getUniqueId());
    }
}

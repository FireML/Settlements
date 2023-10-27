package de.themoep.settlements.command.admin;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AdminWorldCommand extends PlayerSubCommand {
    public AdminWorldCommand(SubCommand parent) {
        super(parent, "world info|setclaimable");
        registerCommand(new PlayerSubCommand(this, "info") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                player.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&aInfo for world &e" + player.getWorld().getName() + "&a:"));
                Empire empire = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
                player.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&eClaimable: "
                        + (Utils.isWorldClaimable(player.getWorld()) && empire == null ? "&ayes" : "&cno" + (empire != null ? " (empire world)" : ""))));
                    player.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&eEmpire World: "
                            + (empire != null ? "&ayes (" + empire.getName() + ")" : "&cno")));
                for (PlotMode mode : PlotMode.values()) {
                    if (mode != PlotMode.embassy && mode != PlotMode.buff) {
                        if (empire == null) {
                            boolean allowed;
                            if (!Utils.isWorldClaimable(player.getWorld())) {
                                if (Utils.hasPlotMode(mode, "worlds." + player.getWorld().getName())) {
                                    allowed = Utils.getPlotMode(mode, "worlds." + player.getWorld().getName());
                                } else {
                                    allowed = Utils.getPlotMode(mode, "unclaimable");
                                }
                            } else if (Utils.hasPlotMode(mode, "worlds." + player.getWorld().getName())) {
                                allowed = Utils.getPlotMode(mode, "worlds." + player.getWorld().getName());
                            } else {
                                allowed = Utils.getPlotMode(mode, "unclaimed");
                            }
                            player.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&e" + mode.getTag() + ": "
                                    + (allowed ? "&ayes" : "&cno") + (Utils.hasPlotMode(mode, "worlds." + player.getWorld().getName()) ? " (world specific)" : "")));
                        } else {
                            player.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&e" + mode.getTag() + ": " + (mode.get(empire) ? "&ayes" : "&cno") + " (empire settings)"));
                        }
                    }
                }
                return true;
            }
        });
        registerCommand(new PlayerSubCommand(this, "setclaimable <yes|no>", "claimable") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                if (args.length > 0) {
                    try {
                        String option;
                        if (Utils.parseBoolean(args[0])) {
                            option = "enabled";
                            Utils.removeDisabledWorld(player.getWorld());
                        } else {
                            option = "disabled";
                            Utils.addDisabledWorld(player.getWorld());
                        }
                        MessageUtils.tell(player, "&c&lClaiming has been &r&6&l" + option + "&r&c&l for world &r&6&l" + player.getWorld().getName());
                        return true;
                    } catch (IllegalArgumentException ignored) {}
                }
                return false;
            }
        });
    }

    @Override
    public boolean run(final Player player, final String base, final String alias, final String... args) {
        return false;
    }
}

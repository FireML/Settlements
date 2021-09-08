package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class VillageAdminWorldCommand extends PlayerSubCommand {
    public VillageAdminWorldCommand(SubCommand parent) {
        super(parent, "world info|setclaimable");
        registerCommand(new PlayerSubCommand(this, "info") {
            @Override
            public boolean run(Player player, String base, String alias, String... args) {
                player.sendMessage(ChatColor.GREEN + "Info for world " + ChatColor.YELLOW + player.getWorld().getName() + ChatColor.GREEN + ":");
                Empire empire = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
                player.sendMessage(ChatColor.YELLOW + "Claimable: "
                        + (Utils.isWorldClaimable(player.getWorld()) && empire == null ? ChatColor.GREEN + "yes" : ChatColor.RED + "no" + (empire != null ? " (empire world)" : "")));
                player.sendMessage(ChatColor.YELLOW + "Empire World: "
                        + (empire != null ? ChatColor.GREEN + "yes (" + empire.getName() + ")" : ChatColor.RED + "no"));
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
                            player.sendMessage(ChatColor.YELLOW + mode.getTag() + ": "
                                    + (allowed ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") + (Utils.hasPlotMode(mode, "worlds." + player.getWorld().getName()) ? " (world specific)" : ""));
                        } else {
                            player.sendMessage(ChatColor.YELLOW + mode.getTag() + ": " + (mode.get(empire) ? ChatColor.GREEN + "yes" : ChatColor.RED + "no") + " (empire settings)");
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

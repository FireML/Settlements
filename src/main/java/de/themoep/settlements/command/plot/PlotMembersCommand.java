package de.themoep.settlements.command.plot;

import com.google.common.collect.ImmutableMap;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.gui.plot.MembersGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Pair;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlotMembersCommand extends PlotSubCommand {
    public PlotMembersCommand(SubCommand parent) {
        super(parent, "members", "member");
        registerCommand(new PlotSubCommand(this, "list [<page>]") {
            @Override
            public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {int page = 1;
                if (args.length > 0) {
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        getError("not-a-number").tell(res, "input", args[0]);
                        return false;
                    }
                    if (page < 1) {
                        page = 1;
                    }
                }

                int finalPage = page;
                Utils.runAsync(() -> {
                    int pages = (int) Math.ceil(land.getPlotMembers().size() / (double) Config.ENTRIES_PER_PAGE);
                    getMessage("head").tell(res,
                            "page", String.valueOf(finalPage),
                            "maxpage", String.valueOf(pages)
                    );

                    land.getPlotMembers().stream()
                            .map(id -> {
                                String name = id.toString();
                                Player p = Bukkit.getPlayer(id);
                                if (p != null) {
                                    name = p.getName();
                                } else {
                                    Resident resident = ResidentManager.getInstance().getResident(id);
                                    if (resident != null) {
                                        name = resident.getName();
                                    }
                                }
                                return new Pair<>(id, name);
                            })
                            .sorted((p1, p2) -> p1.getRight().compareToIgnoreCase(p2.getRight()))
                            .skip((long) (finalPage - 1) * Config.ENTRIES_PER_PAGE)
                            .limit(Config.ENTRIES_PER_PAGE)
                            .forEach(pair -> getMessage("entry").tell(res, "name", pair.getRight(), "uuid", pair.getLeft().toString()));

                    ComponentBuilder next = new ComponentBuilder("")
                            .append(getMessage("next").getReplacedComponents("next", String.valueOf(finalPage + 1)));
                    if (finalPage + 1 > pages) {
                        next.color(ChatColor.GRAY);
                    } else {
                        next.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalPage + 1)));
                        next.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ChatColor.YELLOW + ">>"))));
                    }
                    ComponentBuilder previous = new ComponentBuilder("")
                            .append(getMessage("previous").getReplacedComponents("previous", String.valueOf(finalPage - 1)));
                    if (finalPage - 1 <= 0) {
                        previous.color(ChatColor.GRAY);
                    } else {
                        previous.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalPage - 1)));
                        previous.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText(ChatColor.YELLOW + "<<"))));
                    }

                    MessageUtils.tell(res.getPlayer(), MessageUtils.replacePlaceholders(getMessage("footer").getReplacedComponents(
                            "page", String.valueOf(finalPage),
                            "maxpage", String.valueOf(pages)
                    ), ImmutableMap.of(
                            "page", TextComponent.fromLegacyText(String.valueOf(finalPage)),
                            "maxpage", TextComponent.fromLegacyText(String.valueOf(pages)),
                            "next", next.create(),
                            "previous", previous.create()
                    )));
                });
                return true;
            }
        });
        registerCommand(new PlotSubCommand(this, "add <name>") {
            @Override
            public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }
                Utils.runAsync(() -> {
                    Resident target;
                    Player player = Bukkit.getPlayer(args[0]);
                    if (player != null) {
                        target = ResidentManager.getInstance().getResident(player);
                    } else {
                        target = ResidentManager.getInstance().getResident(args[0]);
                    }
                    if (target == null) {
                        getError("player-not-found").tell(res, "name", args[0]);
                        return;
                    }

                    if (land.getPlotMembers().add(target.getUUID())) {
                        LandManager.getInstance().syncAndSave(land);
                        getMessage("success").tell(res, "name", target.getName(), "uuid", target.getUUID().toString());
                    } else {
                        getMessage("already-a-member").tell(res, "name", target.getName(), "uuid", target.getUUID().toString());
                    }
                });
                return true;
            }
        });
        registerCommand(new PlotSubCommand(this, "remove <name>") {
            @Override
            public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }
                Utils.runAsync(() -> {
                    Resident target;
                    Player player = Bukkit.getPlayer(args[0]);
                    if (player != null) {
                        target = ResidentManager.getInstance().getResident(player);
                    } else {
                        target = ResidentManager.getInstance().getResident(args[0]);
                    }
                    if (target == null) {
                        getError("player-not-found").tell(res, "name", args[0]);
                        return;
                    }

                    if (land.getPlotMembers().remove(target.getUUID())) {
                        LandManager.getInstance().syncAndSave(land);
                        getMessage("success").tell(res, "name", target.getName(), "uuid", target.getUUID().toString());
                    } else {
                        getMessage("not-a-member").tell(res, "name", target.getName(), "uuid", target.getUUID().toString());
                    }
                });
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length == 0) {
            new MembersGUI(res, land).open();
            return true;
        }
        return false;
    }
}

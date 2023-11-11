package de.themoep.settlements.command.plot;

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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.utils.ComponentUtils;

import java.util.Map;

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

                    Component next = getMessage("next").getReplacedComponent("next", String.valueOf(finalPage + 1));
                    if (finalPage + 1 > pages) {
                        next = next.color(NamedTextColor.GRAY);
                    } else {
                        next = next.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalPage + 1)));
                        next = next.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.parseComponent("&e>>")));
                    }
                    Component previous = getMessage("previous").getReplacedComponent("previous", String.valueOf(finalPage - 1));
                    if (finalPage - 1 <= 0) {
                        previous = previous.color(NamedTextColor.GRAY);
                    } else {
                        previous = previous.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalPage - 1)));
                        previous = previous.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.parseComponent("&e<<")));
                    }

                    Map<String, ?> replacements = Map.of(
                            "page", ComponentUtils.parseComponent(String.valueOf(finalPage)),
                            "maxpage", ComponentUtils.parseComponent(String.valueOf(pages)),
                            "next", next,
                            "previous", previous
                    );

                    MessageUtils.tell(res.getPlayer(), ComponentUtils.parsePlaceholders(getMessage("footer").getComponent(), replacements));
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

package de.themoep.settlements.command.admin;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.event.Event;
import de.themoep.settlements.event.EventManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;

public class AdminEventsCommand extends SubCommand {
    public AdminEventsCommand(SubCommand parent) {
        super(parent, "events [list|info <id>|start <id>]", "event");
        registerCommand(new SubCommand(this, "list") {
            @Override
            public boolean run(CommandSender sender, String base, String alias, String... args) {
                sender.sendMessage("Available Events:");
                for (Event event : EventManager.getInstance().getEvents()) {
                    sender.sendMessage(event.getId() + " - " + event.getTitle() + " - " + event.getTime() + "s");
                }
                return true;
            }
        });
        registerCommand(new SubCommand(this, "info <id>") {
            @Override
            public boolean run(CommandSender sender, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }
                Event event = EventManager.getInstance().getEvent(args[0]);
                if (event != null) {
                    sender.sendMessage("Event " + event.getId() + ":");
                    sender.sendMessage("Title: " + event.getTitle());
                    sender.sendMessage("Time: " + event.getTime() + "s");
                    sender.sendMessage("Worlds: " + String.join(", ", event.getWorlds()));
                    sender.sendMessage("Actions:");
                    for (Map.Entry<Event.Action, Collection<String>> entry : event.getActions().asMap().entrySet()) {
                        sender.sendMessage("  " + entry.getKey().getTag() + ":");
                        sender.sendMessage("    " + String.join(", ", entry.getValue()));
                    }
                } else {
                    sender.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&cNo event with the id " + args[0] + " found."));
                }
                return true;
            }
        });
        registerCommand(new SubCommand(this, "start <id>") {
            @Override
            public boolean run(CommandSender sender, String base, String alias, String... args) {
                if (args.length < 1) {
                    return false;
                }
                Event event = EventManager.getInstance().getEvent(args[0]);
                if (event != null) {
                    Event activeEvent = EventManager.getInstance().getActiveEvent();
                    if (activeEvent != null && (args.length == 1 || !"confirm".equalsIgnoreCase(args[1]))) {
                        sender.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&cThe event &e" + activeEvent.getId() + " &cis already running. " +
                                "Use " + "&e/" + base + " " + alias + " " + args[0] + " confirm" + " &cto replace the running event with the new one (without granting rewards for the current event)"));
                    } else {
                        EventManager.getInstance().startEvent(event);
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&cStarted event " + event.getId()));
                        }
                    }
                } else {
                    sender.sendMessage(SettlementsPlugin.getInstance().messageUtils.toComponent("&cNo event with the id " + args[0] + " found."));
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        return false;
    }
}

package de.themoep.settlements.command.settlement;

import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.event.Event;
import de.themoep.settlements.event.EventManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SettlementEventsCommand extends SubCommand {

    public SettlementEventsCommand(SubCommand parent) {
        super(parent, "events top|rewards", "event");
        registerCommand(new SettlementRewardsCommand(parent));
        registerCommand(new SubCommand(this, "top") {
            @Override
            public boolean run(CommandSender sender, String base, String alias, String... args) {
                Event event = EventManager.getInstance().getActiveEvent();
                if (event == null) {
                    getError("no-event").tell(sender);
                    return true;
                }
                List<EventManager.EventData> top = EventManager.getInstance().getTop(3);
                if (top.isEmpty()) {
                    getError("no-one-participating").tell(sender, "event", event.getTitle());
                    return true;
                }
                getMessage("head").tell(sender, "event", event.getTitle(), "amount", String.valueOf(top.size()));
                for (int i = 0; i < top.size(); i++) {
                    EventManager.EventData eventData = top.get(i);
                    Settlement settlement = eventData.getSettlement();
                    getMessage("entry").tell(sender, settlement.getReplacements(this,
                            "place", String.valueOf(i + 1),
                            "amount", String.valueOf(eventData.getTotalAmount()),
                            "event", event.getTitle()
                    ));
                }
                getMessage("footer").tell(sender, "event", event.getTitle(), "amount", String.valueOf(top.size()));
                return true;
            }
        });
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        return false;
    }
}

package me.crz.minecraft.kingdoms.command.village;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.UtilTime;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.village.Resident;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Date;

public class VillageActiveCommand extends VillageSubCommand {

    public VillageActiveCommand(SubCommand parent) {
        super(parent, "active [<page>]", "activity");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(sender, "input", args[0]);
                return false;
            }
            if (page < 1) {
                page = 1;
            }
        }
        int finalPage = page;
        Utils.runAsync(() -> {
            Collection<Resident> residents = sender.getVillage().getResidents();
            int pages = (int) Math.ceil(residents.size() / (double) Config.ENTRIES_PER_PAGE);
            getMessage("head").tell(sender,
                    "page", String.valueOf(finalPage),
                    "maxpage", String.valueOf(pages)
            );
            residents.stream()
                    .sorted((r1, r2) -> {
                        Player p1 = r1.getPlayer();
                        Player p2 = r2.getPlayer();
                        if (p1 != null && p2 == null) {
                            return -1;
                        } else if (p1 == null && p2 != null) {
                            return 1;
                        }
                        return Long.compare(r1.getLastLogin(), r2.getLastLogin());
                    })
                    .skip((finalPage - 1 )* Config.ENTRIES_PER_PAGE)
                    .limit(Config.ENTRIES_PER_PAGE)
                    .forEachOrdered(r -> getMessage("entry").tell(sender,
                            "title", r.getDisplayTitle(),
                            "rank", r.getVillageRank().getTag(r.getVillage()),
                            "name", r.getName(),
                            "activity", r.getPlayer() != null ? getText("online") : getText("offline", "ago", UtilTime.format(System.currentTimeMillis() - r.getLastLogin())),
                            "timestamp", Utils.TIME_FORMAT.format(new Date(r.getLastLogin()))
                    ));

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

            MessageUtils.tell(sender.getPlayer(), MessageUtils.replacePlaceholders(getMessage("footer").getReplacedComponents(
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
}

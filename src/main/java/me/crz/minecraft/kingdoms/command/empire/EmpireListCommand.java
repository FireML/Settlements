package me.crz.minecraft.kingdoms.command.empire;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class EmpireListCommand extends SubCommand {
    public EmpireListCommand(SubCommand parent) {
        super(parent, "list [<page>]");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException e1) {
                getError("not-a-number").tell(player, "input", args[0]);
                return false;
            }
        }
        if (page < 1) {
            getError("not-a-number").tell(player, "input", String.valueOf(page));
            return false;
        }
        int finalPage = page;
        Utils.runAsync(() -> {
            List<Empire> empires = EmpireManager.getInstance().getEmpires().stream()
                    .sorted(Empire::compareTo)
                    .collect(Collectors.toList());

            int pages = (int) Math.ceil(empires.size() / (double) Utils.getEntriesPerPage());
            getMessage("head").tell(player,
                    "page", String.valueOf(finalPage),
                    "maxpage", String.valueOf(pages)
            );
            for (int i = (finalPage - 1) * Utils.getEntriesPerPage(); i < empires.size() && i < finalPage * Utils.getEntriesPerPage(); i++) {
                Empire e = empires.get(i);
                Village capital = e.getCapital();
                Resident chief = capital.getChief();
                getMessage("entry").tell(player,
                        "name", e.getName(),
                        "level", String.valueOf(e.getLevel()),
                        "description", e.getDescription(),
                        "members", String.valueOf(e.getMemberIds().size()),
                        "capital", capital.getName(),
                        "capitalstate", capital.getState().getTag(),
                        "chief", chief.getName(),
                        "chieftitle", chief.getTitle(),
                        "chiefrank", VillageRank.CHIEF.getTag(capital)
                );
            }

            ComponentBuilder next = new ComponentBuilder("")
                    .append(getMessage("next").getReplacedComponents("next", String.valueOf(finalPage + 1)));
            if (finalPage + 1 > pages) {
                next.color(ChatColor.GRAY);
            } else {
                next.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalPage + 1)));
                next.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + ">>")));
            }
            ComponentBuilder previous = new ComponentBuilder("")
                    .append(getMessage("previous").getReplacedComponents("previous", String.valueOf(finalPage - 1)));
            if (finalPage - 1 <= 0) {
                previous.color(ChatColor.GRAY);
            } else {
                previous.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalPage - 1)));
                previous.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + "<<")));
            }

            MessageUtils.tell(player, MessageUtils.replacePlaceholders(getMessage("footer").getReplacedComponents(
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

package me.crz.minecraft.kingdoms.command.village;

import com.google.common.collect.ImmutableMap;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class VillageListCommand extends SubCommand {
    public VillageListCommand(SubCommand parent) {
        super(parent, "list [open] [<page>]");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        int page = 1;
        VillageAccess access = null;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException e1) {
                if ("open".equalsIgnoreCase(args[0])) {
                    access = VillageAccess.OPEN;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (IllegalArgumentException e2) {
                            getError("not-a-number").tell(player, "input", args[1]);
                            return false;
                        }
                    }
                } else {
                    getError("not-a-number").tell(player, "input", args[0]);
                    return false;
                }
            }
        }
        if (page < 1) {
            getError("not-a-number").tell(player, "input", String.valueOf(page));
            return false;
        }
        VillageAccess finalAccess = access;
        int finalPage = page;
        Utils.runAsync(() -> {
            List<Village> villages = VillageManager.getInstance().getVillages().stream()
                    .filter(v -> v.getAccess().atLeast(finalAccess))
                    .sorted(Village::compareTo)
                    .collect(Collectors.toList());

            int pages = (int) Math.ceil(villages.size() / (double) Utils.getEntriesPerPage());
            getMessage("head").tell(player,
                    "access", finalAccess != null ? finalAccess.getTag() + " " : "",
                    "page", String.valueOf(finalPage),
                    "maxpage", String.valueOf(pages)
            );

            for (int i = (finalPage - 1) * Utils.getEntriesPerPage(); i < villages.size() && i < finalPage * Utils.getEntriesPerPage(); i++) {
                Village v = villages.get(i);
                Resident chief = v.getChief();
                getMessage("entry").tell(player,
                        "name", v.getName(),
                        "level", String.valueOf(v.getLevel()),
                        "description", v.getDescription(),
                        "state", v.getState().getTag(),
                        "access", v.getAccess() != VillageAccess.PRIVATE ? v.getAccess().getTag() : "",
                        "residents", String.valueOf(v.getResidentIds().size()),
                        "chief", chief.getName(),
                        "chieftitle", chief.getTitle(),
                        "chiefrank", VillageRank.CHIEF.getTag(v)
                );
            }

            ComponentBuilder next = new ComponentBuilder("")
                    .append(getMessage("next").getReplacedComponents("next", String.valueOf(finalPage + 1)));
            if (finalPage + 1 > pages) {
                next.color(ChatColor.GRAY);
            } else {
                next.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalAccess != null ? finalAccess.name() + " " : "") + (finalPage + 1)));
                next.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + ">>")));
            }
            ComponentBuilder previous = new ComponentBuilder("")
                    .append(getMessage("previous").getReplacedComponents("previous", String.valueOf(finalPage - 1)));
            if (finalPage - 1 <= 0) {
                previous.color(ChatColor.GRAY);
            } else {
                previous.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + base + " " + alias + " " + (finalAccess != null ? finalAccess.name() + " " : "") + (finalPage - 1)));
                previous.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + "<<")));
            }

            MessageUtils.tell(player, MessageUtils.replacePlaceholders(getMessage("footer").getReplacedComponents(
                    "access", finalAccess != null ? finalAccess.getTag() + " " : "",
                    "page", String.valueOf(finalPage),
                    "maxpage", String.valueOf(pages)
            ), ImmutableMap.of(
                    "next", next.create(),
                    "previous", previous.create()
            )));
        });

        return true;
    }
}
